package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.constant.notification.EmailFields;
import net.gowaka.gowaka.domain.config.PaymentUrlResponseProps;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.BookedJourneyRepository;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.PaymentTransactionRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.domain.service.utilities.QRCodeProvider;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.network.api.notification.model.EmailAddress;
import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static net.gowaka.gowaka.exception.ErrorCodes.*;
import static net.gowaka.gowaka.network.api.payamgo.PayAmGoPaymentStatus.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:48 AM <br/>
 */
@Service
public class BookJourneyServiceImpl implements BookJourneyService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BookedJourneyRepository bookedJourneyRepository;
    private JourneyRepository journeyRepository;
    private UserRepository userRepository;
    private PaymentTransactionRepository paymentTransactionRepository;
    private UserService userService;
    private PayAmGoService payAmGoService;
    private NotificationService notificationService;
    private FileStorageService fileStorageService;
    private PaymentUrlResponseProps paymentUrlResponseProps;
    private JourneyService journeyService;

    private EmailContentBuilder emailContentBuilder;

    @Autowired
    public BookJourneyServiceImpl(BookedJourneyRepository bookedJourneyRepository, JourneyRepository journeyRepository, UserRepository userRepository, PaymentTransactionRepository paymentTransactionRepository, UserService userService, PayAmGoService payAmGoService, NotificationService notificationService, FileStorageService fileStorageService, PaymentUrlResponseProps paymentUrlResponseProps, EmailContentBuilder emailContentBuilder) {
        this.bookedJourneyRepository = bookedJourneyRepository;
        this.journeyRepository = journeyRepository;
        this.userRepository = userRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.userService = userService;
        this.payAmGoService = payAmGoService;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
        this.paymentUrlResponseProps = paymentUrlResponseProps;
        this.emailContentBuilder = emailContentBuilder;
    }

    @Autowired
    public void setJourneyService(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @Override
    public PaymentUrlDTO bookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest) {

        Journey journey = getJourney(journeyId);

        Optional<BookedJourney> bookedJourneyOptional = journey.getBookedJourneys().stream()
                .filter(bookedJourney -> bookedJourney.getPassenger() != null)
                .filter(bookedJourney -> bookedJourney.getPassenger().getSeatNumber() != null)
                .filter(bookedJourney -> bookedJourney.getPassenger().getSeatNumber().equals(bookJourneyRequest.getSeatNumber()))
                .findFirst();
        if (bookedJourneyOptional.isPresent()) {
            throw new ApiException(SEAT_ALREADY_TAKEN.getMessage(), SEAT_ALREADY_TAKEN.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User user = getUser();
        verifyJourneyStatus(journey);
        Passenger passenger = getPassenger(bookJourneyRequest);

        Double amount;
        TransitAndStop transitAndStop;
        if (bookJourneyRequest.isDestinationIndicator()) {
            amount = journey.getAmount();
            transitAndStop = journey.getDestination();
        } else {
            Optional<JourneyStop> journeyStopOptional = journey.getJourneyStops().stream()
                    .filter(stop -> stop.getTransitAndStop().getId().equals(bookJourneyRequest.getTransitAndStopId()))
                    .findFirst();
            if (!journeyStopOptional.isPresent()) {
                logger.info("Transit and stop not found transitAndStopId: {}", bookJourneyRequest.getTransitAndStopId());
                throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
            JourneyStop journeyStop = journeyStopOptional.get();
            amount = journeyStop.getAmount();
            transitAndStop = journeyStop.getTransitAndStop();
        }

        BookedJourney bookedJourney = getBookedJourney(passenger, user, journey, amount, transitAndStop);
        BookedJourney savedBookedJourney = bookedJourneyRepository.save(bookedJourney);
        String qrCodeText = user.getUserId() + journey.getId().toString() + bookJourneyRequest.getSeatNumber().toString() + "-" + new Date().getTime();
        bookedJourney.setCheckedInCode(qrCodeText);

        String[] names = bookJourneyRequest.getPassengerName().split(" ");
        String firstName = names[0] != null ? names[0] : "Anonymous";
        String lastName = names[names.length - 1] != null ? names[names.length - 1] : "Anonymous";

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(savedBookedJourney);

        paymentTransaction.setAmount(amount);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket for " + journey.getCar().getLicensePlateNumber());

        paymentTransaction.setAppTransactionNumber(UUID.randomUUID().toString());
        paymentTransaction.setAppUserEmail(bookJourneyRequest.getEmail());
        paymentTransaction.setAppUserFirstName(firstName);
        paymentTransaction.setAppUserLastName(lastName);
        paymentTransaction.setAppUserPhoneNumber(bookJourneyRequest.getPhoneNumber());

        paymentTransaction.setCancelRedirectUrl(paymentUrlResponseProps.getPayAmGoPaymentCancelUrl());
        paymentTransaction.setPaymentResponseUrl(paymentUrlResponseProps.getPayAmGoPaymentResponseUrl() + "/" + savedBookedJourney.getId());
        paymentTransaction.setReturnRedirectUrl(paymentUrlResponseProps.getPayAmGoPaymentRedirectUrl() + "/" + savedBookedJourney.getId());

        paymentTransaction.setLanguage("en");
        paymentTransaction.setTransactionStatus(INITIATED.toString());

        PaymentTransaction savedPaymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        PayAmGoRequestDTO payAmGoRequestDTO = getPayAmGoRequestDTO(savedPaymentTransaction);
        PayAmGoRequestResponseDTO payAmGoRequestResponseDTO = payAmGoService.initiatePayment(payAmGoRequestDTO);

        savedPaymentTransaction.setTransactionStatus(WAITING.toString());
        savedPaymentTransaction.setProcessingNumber(payAmGoRequestResponseDTO.getProcessingNumber());
        paymentTransactionRepository.save(savedPaymentTransaction);


        return new PaymentUrlDTO(payAmGoRequestResponseDTO.getPaymentUrl());
    }

    @Override
    public List<Integer> getAllBookedSeats(Long journeyId) {

        int MIM_TIME_TO_WAIT_FOR_PAYMENT = 10;
        Journey journey = getJourney(journeyId);
        return journey.getBookedJourneys().stream()
                .filter(bookedJourney -> bookedJourney.getPassenger() != null)
                .filter(bookedJourney -> bookedJourney.getPassenger().getSeatNumber() != null)
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction() != null)
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equals(INITIATED.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(WAITING.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(COMPLETED.toString())
                )
                .map(bookedJourney -> {
                    PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
                    String transactionStatus = paymentTransaction.getTransactionStatus();
                    long untilMinute = paymentTransaction.getCreateAt().until(LocalDateTime.now(), ChronoUnit.MINUTES);
                    if ((transactionStatus.equals(WAITING.toString()) || transactionStatus.equals(INITIATED.toString()))
                            && untilMinute > MIM_TIME_TO_WAIT_FOR_PAYMENT) {
                        bookedJourney.getPassenger().setSeatNumber(0);
                        bookedJourneyRepository.save(bookedJourney);
                        return 0;
                    }
                    return bookedJourney.getPassenger().getSeatNumber();
                })
                .filter(seat -> !seat.equals(0))
                .collect(Collectors.toList());
    }

    @Override
    public BookedJourneyStatusDTO getBookJourneyStatus(Long bookedJourneyId) {

        Optional<BookedJourney> bookedJourneyOptional = bookedJourneyRepository.findById(bookedJourneyId);
        if (!bookedJourneyOptional.isPresent()) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        BookedJourney bookedJourney = bookedJourneyOptional.get();
        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        if (currentAuthUser == null
                || currentAuthUser.getId() == null
                || bookedJourney.getUser() == null
                || !currentAuthUser.getId().equals(bookedJourney.getUser().getUserId())) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }

        BookedJourneyStatusDTO bookedJourneyStatusDTO = getBookedJourneyStatusDTO(bookedJourney);
        addQRCheckedInImageUrl(bookedJourneyStatusDTO);
        return bookedJourneyStatusDTO;
    }

    @Override
    public List<BookedJourneyStatusDTO> getUserBookedJourneyHistory() {
        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        if (currentAuthUser == null || currentAuthUser.getId() == null) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return bookedJourneyRepository.findAllByUserUserId(currentAuthUser.getId()).stream()
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction() != null)
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equals(COMPLETED.toString()))
                .map(this::getBookedJourneyStatusDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void handlePaymentResponse(Long bookedJourneyId, PaymentStatusResponseDTO paymentStatusResponseDTO) {

        Optional<BookedJourney> bookedJourneyOptional = bookedJourneyRepository.findById(bookedJourneyId);
        if (!bookedJourneyOptional.isPresent()) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        BookedJourney bookedJourney = bookedJourneyOptional.get();
        PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
        if (paymentTransaction.getTransactionStatus().equals(WAITING.toString()) &&
                paymentTransaction.getProcessingNumber().equals(paymentStatusResponseDTO.getProcessingNumber())
                && paymentTransaction.getAppTransactionNumber().equals(paymentStatusResponseDTO.getAppTransactionNumber())) {

            boolean isCompleted = paymentStatusResponseDTO.getTransactionStatus().equals(COMPLETED.toString());

            paymentTransaction.setTransactionStatus(isCompleted ? COMPLETED.toString() : DECLINED.toString());
            paymentTransaction.setPaymentChannelTransactionNumber(paymentStatusResponseDTO.getPaymentChannelTransactionNumber());
            paymentTransaction.setPaymentChannel(paymentStatusResponseDTO.getPaymentChannelCode());
            paymentTransaction.setPaymentDate(LocalDateTime.now());
            paymentTransactionRepository.save(paymentTransaction);

            if (!isCompleted) {
                bookedJourney.getPassenger().setSeatNumber(0);
                bookedJourneyRepository.save(bookedJourney);
            }

            if (isCompleted) {
                String storageFolder = QRCodeProvider.STORAGE_FOLDER;
                String filename = bookedJourney.getCheckedInCode() + "." + QRCodeProvider.STORAGE_FILE_FORMAT;
                try {
                    byte[] qrCodeBytes = getQRCodeBytes(bookedJourney);
                    fileStorageService.savePublicFile(filename, qrCodeBytes, storageFolder);
                } catch (IOException e) {
                    logger.info("could not generate QR Code");
                    e.printStackTrace();
                }
                String publicFilePath = fileStorageService.getPublicFilePath(filename, storageFolder);

                BookedJourneyStatusDTO bookedJourneyStatusDTO = getBookedJourneyStatusDTO(bookedJourney);
                bookedJourneyStatusDTO.setQRCheckedInImageUrl(publicFilePath);
                sendTicketEmail(bookedJourneyStatusDTO);
            }
        }


    }

    @Override
    public OnBoardingInfoDTO getPassengerOnBoardingInfo(String checkedInCode) {
        BookedJourney bookedJourney = getBookedJourneyByCheckedInCode(checkedInCode);
        Journey journey = bookedJourney.getJourney();
        // check if the journey's car is in user's official agency
        // borrow this method from journeyService
        journeyService.checkJourneyCarInOfficialAgency(journey);
        if (journeyNotStartedOrElseReject(journey) &&
                journeyNotTerminatedOrElseReject(journey) /* check journey state */
                && paymentAcceptedOrElseReject(bookedJourney.getPaymentTransaction()) /* check payment status */
        ) {
            return new OnBoardingInfoDTO(bookedJourney);
        }
        throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
    }

    @Override
    public void checkInPassengerByCode(String checkedInCode) {
        BookedJourney bookedJourney = getBookedJourneyByCheckedInCode(checkedInCode);
        Journey journey = bookedJourney.getJourney();
        // check if the journey's car is in user's official agency
        // borrow this method from journeyService
        journeyService.checkJourneyCarInOfficialAgency(journey);
        if (journeyNotStartedOrElseReject(journey) &&
                journeyNotTerminatedOrElseReject(journey) &&
                paymentAcceptedOrElseReject(bookedJourney.getPaymentTransaction())
                && passengerNotCheckedInOrElseReject(bookedJourney.getPassengerCheckedInIndicator())) {
            bookedJourney.setPassengerCheckedInIndicator(true);
            bookedJourneyRepository.save(bookedJourney);
        }
    }

    @Override
    public String getHtmlReceipt(Long bookedJourneyId) {

        BookedJourneyStatusDTO bookedJourneyStatusDTO = getBookJourneyStatus(bookedJourneyId);
        return emailContentBuilder.buildTicketPdfHtml(bookedJourneyStatusDTO);
    }

    @Override
    public List<OnBoardingInfoDTO> getAllPassengerOnBoardingInfo(Long journeyId) {
        journeyService.checkJourneyCarInOfficialAgency(getJourney(journeyId));
        return bookedJourneyRepository.findAllByJourneyId(journeyId).stream().map(
                OnBoardingInfoDTO::new
        ).collect(Collectors.toList());
    }

    private void sendTicketEmail(BookedJourneyStatusDTO bookedJourneyStatusDTO) {

        String message = emailContentBuilder.buildTicketEmail(bookedJourneyStatusDTO);

        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setSubject(EmailFields.TICKET_SUBJECT.getMessage());
        emailDTO.setMessage(message);

        emailDTO.setToAddresses(Collections.singletonList(new EmailAddress(
                bookedJourneyStatusDTO.getPassengerEmail(),
                bookedJourneyStatusDTO.getPassengerName()
        )));
        // setting cc and bcc to empty lists
        emailDTO.setCcAddresses(Collections.emptyList());
        emailDTO.setBccAddresses(Collections.emptyList());
        notificationService.sendEmail(emailDTO);
    }

    private BookedJourneyStatusDTO getBookedJourneyStatusDTO(BookedJourney bookedJourney) {
        BookedJourneyStatusDTO bookedJourneyStatusDTO = new BookedJourneyStatusDTO();
        bookedJourneyStatusDTO.setId(bookedJourney.getId());
        bookedJourneyStatusDTO.setAmount(bookedJourney.getPaymentTransaction().getAmount());
        bookedJourneyStatusDTO.setPaymentDate(bookedJourney.getPaymentTransaction().getPaymentDate());
        bookedJourneyStatusDTO.setCurrencyCode(bookedJourney.getPaymentTransaction().getCurrencyCode());
        bookedJourneyStatusDTO.setPaymentReason(bookedJourney.getPaymentTransaction().getPaymentReason());
        bookedJourneyStatusDTO.setPaymentStatus(bookedJourney.getPaymentTransaction().getTransactionStatus());
        bookedJourneyStatusDTO.setCheckedIn(bookedJourney.getPassengerCheckedInIndicator());
        bookedJourneyStatusDTO.setPaymentChannelTransactionNumber(bookedJourney.getPaymentTransaction().getPaymentChannelTransactionNumber());

        bookedJourneyStatusDTO.setCheckedInCode(bookedJourney.getCheckedInCode());
        bookedJourneyStatusDTO.setPaymentChannel(bookedJourney.getPaymentTransaction().getPaymentChannel());

        bookedJourneyStatusDTO.setCarDriverName(bookedJourney.getJourney().getDriver().getDriverName());
        bookedJourneyStatusDTO.setCarLicenseNumber(bookedJourney.getJourney().getCar().getLicensePlateNumber());
        bookedJourneyStatusDTO.setCarName(bookedJourney.getJourney().getCar().getName());

        Location departureLocation = bookedJourney.getJourney().getDepartureLocation().getLocation();
        bookedJourneyStatusDTO.setDepartureLocation(departureLocation.getAddress() + ", " + departureLocation.getCity() + " " + departureLocation.getState() + ", " + departureLocation.getCountry());
        bookedJourneyStatusDTO.setDepartureTime(bookedJourney.getJourney().getDepartureTime());
        bookedJourneyStatusDTO.setEstimatedArrivalTime(bookedJourney.getJourney().getEstimatedArrivalTime());

        Location destinationLocation = bookedJourney.getDestination().getLocation();
        bookedJourneyStatusDTO.setDestinationLocation(destinationLocation.getAddress() + ", " + destinationLocation.getCity() + " " + destinationLocation.getState() + ", " + destinationLocation.getCountry());

        bookedJourneyStatusDTO.setPassengerIdNumber(bookedJourney.getPassenger().getPassengerIdNumber());
        bookedJourneyStatusDTO.setPassengerSeatNumber(bookedJourney.getPassenger().getSeatNumber());
        bookedJourneyStatusDTO.setPassengerName(bookedJourney.getPassenger().getPassengerName());
        bookedJourneyStatusDTO.setPassengerEmail(bookedJourney.getPaymentTransaction().getAppUserEmail());
        bookedJourneyStatusDTO.setPassengerPhoneNumber(bookedJourney.getPaymentTransaction().getAppUserPhoneNumber());

        String encoding = null;
        try {
            encoding = getQREncodedImage(bookedJourney);
        } catch (IOException e) {
            logger.info("could not generate QR Code");
            e.printStackTrace();
        }
        bookedJourneyStatusDTO.setQRCheckedInImage(encoding);
        return bookedJourneyStatusDTO;
    }

    private String getQREncodedImage(BookedJourney bookedJourney) throws IOException {
        byte[] imageBytes = getQRCodeBytes(bookedJourney);

        Base64.Encoder encoder = Base64.getEncoder();
        return "data:image/png;base64," + encoder.encodeToString(imageBytes);
    }

    private byte[] getQRCodeBytes(BookedJourney bookedJourney) throws IOException {
        BufferedImage bufferedImage = QRCodeProvider.generateQRCodeImage(bookedJourney.getCheckedInCode());
        String pathname = "image" + bookedJourney.getId() + ".png";
        File file = new File(pathname);
        ImageIO.write(bufferedImage, "png", file);
        byte[] imageBytes = Files.readAllBytes(Paths.get(pathname));
        file.delete();
        return imageBytes;
    }

    private void addQRCheckedInImageUrl(BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        String storageFolder = QRCodeProvider.STORAGE_FOLDER;
        String filename = bookedJourneyStatusDTO.getCheckedInCode() + "." + QRCodeProvider.STORAGE_FILE_FORMAT;
        String publicFilePath = fileStorageService.getPublicFilePath(filename, storageFolder);

        bookedJourneyStatusDTO.setQRCheckedInImageUrl(publicFilePath);
    }

    private PayAmGoRequestDTO getPayAmGoRequestDTO(PaymentTransaction savedPaymentTransaction) {
        PayAmGoRequestDTO payAmGoRequestDTO = new PayAmGoRequestDTO();
        payAmGoRequestDTO.setAmount(savedPaymentTransaction.getAmount().toString());
        payAmGoRequestDTO.setCurrencyCode(savedPaymentTransaction.getCurrencyCode());
        payAmGoRequestDTO.setPaymentReason(savedPaymentTransaction.getPaymentReason());
        payAmGoRequestDTO.setLanguage(savedPaymentTransaction.getLanguage());

        payAmGoRequestDTO.setAppUserPhoneNumber(savedPaymentTransaction.getAppUserPhoneNumber());
        payAmGoRequestDTO.setAppUserLastName(savedPaymentTransaction.getAppUserLastName());
        payAmGoRequestDTO.setAppUserFirstName(savedPaymentTransaction.getAppUserFirstName());
        payAmGoRequestDTO.setAppUserEmail(savedPaymentTransaction.getAppUserEmail());
        payAmGoRequestDTO.setAppTransactionNumber(savedPaymentTransaction.getAppTransactionNumber());

        payAmGoRequestDTO.setReturnRedirectUrl(savedPaymentTransaction.getReturnRedirectUrl());
        payAmGoRequestDTO.setPaymentResponseUrl(savedPaymentTransaction.getPaymentResponseUrl());
        payAmGoRequestDTO.setCancelRedirectUrl(savedPaymentTransaction.getCancelRedirectUrl());
        return payAmGoRequestDTO;
    }

    private BookedJourney getBookedJourney(Passenger passenger, User user, Journey journey, Double amount, TransitAndStop transitAndStop) {
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setPassenger(passenger);
        bookedJourney.setUser(user);
        bookedJourney.setJourney(journey);
        bookedJourney.setDestination(transitAndStop);
        bookedJourney.setAmount(amount);
        return bookedJourney;
    }

    private Passenger getPassenger(BookJourneyRequest bookJourneyRequest) {
        Passenger passenger = new Passenger();
        passenger.setPassengerIdNumber(bookJourneyRequest.getPassengerIdNumber());
        passenger.setPassengerName(bookJourneyRequest.getPassengerName());
        passenger.setSeatNumber(bookJourneyRequest.getSeatNumber());
        passenger.setPassengerEmail(bookJourneyRequest.getEmail());
        passenger.setPassengerPhoneNumber(bookJourneyRequest.getPhoneNumber());
        return passenger;
    }

    private User getUser() {
        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return currentAuthUserOptional.get();
    }

    private void verifyJourneyStatus(Journey journey) {
        if (journey.getArrivalIndicator()) {
            throw new ApiException(ErrorCodes.JOURNEY_ALREADY_TERMINATED.getMessage(), ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (journey.getDepartureIndicator()) {
            throw new ApiException(ErrorCodes.JOURNEY_ALREADY_STARTED.getMessage(), ErrorCodes.JOURNEY_ALREADY_STARTED.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private Journey getJourney(Long id) {
        Optional<Journey> journeyOptional = journeyRepository.findById(id);
        if (!journeyOptional.isPresent()) {
            logger.info("Journey not found journeyId: {}", id);
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return journeyOptional.get();
    }

    private BookedJourney getBookedJourneyByCheckedInCode(String code) {
        Optional<BookedJourney> optional = bookedJourneyRepository.findFirstByCheckedInCode(code);
        if (!optional.isPresent()) {
            logger.info("CheckedInCode not found code: {}", code);
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optional.get();
    }

    /**
     * throw exception of journey is terminated
     *
     * @param journey
     * @return boolean
     */
    private boolean journeyNotTerminatedOrElseReject(Journey journey) {
        if (journey != null && journey.getArrivalIndicator() != null && journey.getArrivalIndicator()) {
            throw new ApiException(ErrorCodes.JOURNEY_ALREADY_TERMINATED.getMessage(), ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }


    /**
     * throw exception of journey is not started
     *
     * @param journey
     * @return boolean
     */
    private boolean journeyNotStartedOrElseReject(Journey journey) {
        if (journey != null && journey.getDepartureIndicator() != null && journey.getDepartureIndicator()) {
            throw new ApiException(JOURNEY_ALREADY_STARTED.getMessage(), JOURNEY_ALREADY_STARTED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }


    private boolean paymentAcceptedOrElseReject(PaymentTransaction paymentTransaction) {
        if (!paymentTransaction.getTransactionStatus().equals(COMPLETED.toString())) {
            logger.info("payment transaction declined: {}", paymentTransaction.getAppTransactionNumber());
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return true;
    }

    private boolean passengerNotCheckedInOrElseReject(boolean indicator) {
        if (indicator) {
            throw new ApiException(PASSENGER_ALREADY_CHECKED_IN.getMessage(), PASSENGER_ALREADY_CHECKED_IN.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }



}
