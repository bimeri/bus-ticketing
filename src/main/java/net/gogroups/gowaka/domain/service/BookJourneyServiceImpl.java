package net.gogroups.gowaka.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.constant.RefundStatus;
import net.gogroups.gowaka.constant.notification.EmailFields;
import net.gogroups.gowaka.domain.config.PaymentUrlResponseProps;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.domain.service.utilities.CheckInCodeGenerator;
import net.gogroups.gowaka.domain.service.utilities.QRCodeProvider;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.service.BookJourneyService;
import net.gogroups.gowaka.service.JourneyService;
import net.gogroups.gowaka.service.ServiceChargeService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.notification.model.EmailAddress;
import net.gogroups.notification.model.SendEmailDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.payamgo.model.PayAmGoRequestDTO;
import net.gogroups.payamgo.model.PayAmGoRequestResponseDTO;
import net.gogroups.payamgo.service.PayAmGoService;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.exception.ErrorCodes.*;
import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:48 AM <br/>
 */
@Service
@Slf4j
public class BookJourneyServiceImpl implements BookJourneyService {

    private static final String SMS_NOTIF = "SMS_NOTIF";
    private static final String PLATFORM_SERVICE_CHARGE = "PLATFORM_SERVICE_CHARGE";
    private static final String CASHIER = "CASHIER";

    private BookedJourneyRepository bookedJourneyRepository;
    private JourneyRepository journeyRepository;
    private UserRepository userRepository;
    private PaymentTransactionRepository paymentTransactionRepository;
    private PassengerRepository passengerRepository;
    private UserService userService;
    private PayAmGoService payAmGoService;
    private NotificationService notificationService;
    private FileStorageService fileStorageService;
    private PaymentUrlResponseProps paymentUrlResponseProps;
    private JourneyService journeyService;
    private ServiceChargeService serviceChargeService;

    private EmailContentBuilder emailContentBuilder;

    @Autowired
    public BookJourneyServiceImpl(BookedJourneyRepository bookedJourneyRepository, JourneyRepository journeyRepository, UserRepository userRepository, PaymentTransactionRepository paymentTransactionRepository, PassengerRepository passengerRepository, UserService userService, PayAmGoService payAmGoService, NotificationService notificationService, FileStorageService fileStorageService, PaymentUrlResponseProps paymentUrlResponseProps, JourneyService journeyService, ServiceChargeService serviceChargeService, EmailContentBuilder emailContentBuilder) {
        this.bookedJourneyRepository = bookedJourneyRepository;
        this.journeyRepository = journeyRepository;
        this.userRepository = userRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.passengerRepository = passengerRepository;
        this.userService = userService;
        this.payAmGoService = payAmGoService;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
        this.paymentUrlResponseProps = paymentUrlResponseProps;
        this.journeyService = journeyService;
        this.serviceChargeService = serviceChargeService;
        this.emailContentBuilder = emailContentBuilder;
    }


    @Override
    @Transactional
    public PaymentUrlDTO bookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest) {

        Journey journey = getJourney(journeyId);
        User user = getUser();
        PaymentTransaction paymentTransaction = getPaymentTransaction(journey, user, bookJourneyRequest, false);

        Double chargeAmount = 0.0;
        List<ServiceChargeDTO> serviceCharges = serviceChargeService.getServiceCharges();
        if (!serviceCharges.isEmpty()) {
            for (ServiceChargeDTO sCharge : serviceCharges) {
                if (sCharge.getId().equals(SMS_NOTIF) && bookJourneyRequest.getSubscribeToSMSNotification()) {
                    chargeAmount += getSMSChargeAmount(bookJourneyRequest, sCharge);
                } else if (sCharge.getId().equals(PLATFORM_SERVICE_CHARGE)) {
                    chargeAmount += paymentTransaction.getAmount() * (sCharge.getPercentageCharge() / 100);
                }
            }
            Double amountWithoutCharge = paymentTransaction.getAmount();
            paymentTransaction.setAmount(amountWithoutCharge + chargeAmount);
            paymentTransaction.setAgencyAmount(amountWithoutCharge);
            paymentTransaction.setServiceChargeAmount(chargeAmount);
        }
        PaymentTransaction savedPaymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        PayAmGoRequestDTO payAmGoRequestDTO = getPayAmGoRequestDTO(savedPaymentTransaction);
        PayAmGoRequestResponseDTO payAmGoRequestResponseDTO = payAmGoService.initiatePayment(payAmGoRequestDTO);

        savedPaymentTransaction.setTransactionStatus(WAITING.toString());
        savedPaymentTransaction.setProcessingNumber(payAmGoRequestResponseDTO.getProcessingNumber());
        paymentTransactionRepository.save(savedPaymentTransaction);

        return new PaymentUrlDTO(payAmGoRequestResponseDTO.getPaymentUrl());
    }

    @Override
    public void agencyUserBookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest) {

        Journey journey = getJourney(journeyId);
        User user = getUser();
        OfficialAgency agency = ((Bus) journey.getCar()).getOfficialAgency();

        if (user.getOfficialAgency() == null || !user.getOfficialAgency().getId().equals(agency.getId())) {
            throw new ApiException(USER_NOT_IN_AGENCY.getMessage(), USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        PaymentTransaction paymentTransaction = getPaymentTransaction(journey, user, bookJourneyRequest, true);
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setPaymentChannelTransactionNumber(UUID.randomUUID().toString());
        paymentTransaction.setPaymentChannel(CASHIER);
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setAgencyAmount(paymentTransaction.getAmount());

        Double chargeAmount = 0.0;
//        TODO: Allow agency user to add sms notification service to user
//        List<ServiceChargeDTO> serviceCharges = serviceChargeService.getServiceCharges();
//        if (!serviceCharges.isEmpty()) {
//            for (ServiceChargeDTO sCharge : serviceCharges) {
//                if (sCharge.getId().equals(SMS_NOTIF) && bookJourneyRequest.getSubscribeToSMSNotification()) {
//                    chargeAmount += getSMSChargeAmount(bookJourneyRequest, sCharge);
//                    break;
//                }
//            }
//            Double amountWithoutCharge = paymentTransaction.getAmount();
//            paymentTransaction.setAmount(amountWithoutCharge + chargeAmount);
//            paymentTransaction.setAgencyAmount(amountWithoutCharge);
//            paymentTransaction.setServiceChargeAmount(chargeAmount);
//        }

        paymentTransaction.setServiceChargeAmount(chargeAmount);
        PaymentTransaction savedTxn = paymentTransactionRepository.save(paymentTransaction);
        savedTxn.getBookedJourney().setPaymentTransaction(savedTxn);

        BookedJourneyStatusDTO bookedJourneyStatusDTO = getBookedJourneyStatusDTO(savedTxn.getBookedJourney());
        notifyPassengers(savedTxn.getBookedJourney(), bookedJourneyStatusDTO);

    }

    @Override
    public List<Integer> getAllBookedSeats(Long journeyId) {

        int MIM_TIME_TO_WAIT_FOR_PAYMENT = 5;
        Journey journey = getJourney(journeyId);
        Set<Integer> seats = new HashSet<>();
        journey.getBookedJourneys().stream()
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction() != null)
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equals(INITIATED.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(WAITING.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(COMPLETED.toString())
                ).forEach(bookedJourney -> {
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            String transactionStatus = paymentTransaction.getTransactionStatus();
            long untilMinute = paymentTransaction.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.MINUTES);
            if ((transactionStatus.equals(WAITING.toString()) || transactionStatus.equals(INITIATED.toString()))
                    && untilMinute > MIM_TIME_TO_WAIT_FOR_PAYMENT) {
                //TODO: should look for the next available seat and set
                List<Passenger> passengers = bookedJourney.getPassengers().stream().map(passenger -> {
                    passenger.setSeatNumber(0);
                    return passenger;
                }).collect(Collectors.toList());
                // update seatNumber if more than waiting limit,
                passengerRepository.saveAll(passengers);
            } else {
                seats.addAll(bookedJourney.getPassengers().stream().map(Passenger::getSeatNumber).collect(Collectors.toList()));
            }
        });

        return new ArrayList<>(seats);
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

        return getBookedJourneyStatusDTO(bookedJourney);
    }

    @Override
    public PaginatedResponse<BookedJourneyStatusDTO> getUserBookedJourneyHistory(Integer pageNumber, Integer limit) {

        Pageable paging = PageRequest.of(pageNumber < 1 ? 0 : pageNumber - 1, limit);

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        if (currentAuthUser == null || currentAuthUser.getId() == null) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }

        Page<BookedJourney> bookedJourneyPage = bookedJourneyRepository.findAllByPaymentTransaction_TransactionStatusAndUserUserIdOrderByCreatedAtDesc(COMPLETED.toString(), currentAuthUser.getId(), paging);

        List<BookedJourneyStatusDTO> journeyStatusDTOS = bookedJourneyPage.stream()
                .map(this::getBookedJourneyStatusDTO)
                .collect(Collectors.toList());

        return PaginatedResponse.<BookedJourneyStatusDTO>builder()
                .items(journeyStatusDTOS)
                .count(journeyStatusDTOS.size())
                .total((int) bookedJourneyPage.getTotalElements())
                .totalPages(bookedJourneyPage.getTotalPages())
                .limit(limit)
                .offset((int) paging.getOffset())
                .pageNumber(pageNumber)
                .build();
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
                List<Passenger> passengers = bookedJourney.getPassengers().stream().map(passenger -> {
                    passenger.setSeatNumber(-1);
                    return passenger;
                }).collect(Collectors.toList());
                passengerRepository.saveAll(passengers);
            }

            BookedJourneyStatusDTO bookedJourneyStatusDTO = getBookedJourneyStatusDTO(bookedJourney);
            if (isCompleted) {
                notifyPassengers(bookedJourney, bookedJourneyStatusDTO);
            }
        }
    }

    private void notifyPassengers(BookedJourney bookedJourney, BookedJourneyStatusDTO bookedJourneyStatusDTO) {
        bookedJourney.getPassengers().forEach(passenger -> {
            String filename = passenger.getCheckedInCode() + "." + QRCodeProvider.STORAGE_FILE_FORMAT;
            try {
                byte[] qrCodeBytes = getQRCodeBytes(bookedJourney, passenger.getCheckedInCode());
                fileStorageService.saveFile(filename, qrCodeBytes, QRCodeProvider.STORAGE_FOLDER, FileAccessType.PROTECTED);
            } catch (IOException e) {
                log.info("could not generate QR Code");
                e.printStackTrace();
            }
        });
        sendTicketEmail(bookedJourneyStatusDTO);
    }

    @Override
    public OnBoardingInfoDTO getPassengerOnBoardingInfo(String checkedInCode) {
        Passenger passenger = getBookedJourneyByCheckedInCode(checkedInCode);
        BookedJourney bookedJourney = passenger.getBookedJourney();
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
        Passenger passenger = getBookedJourneyByCheckedInCode(checkedInCode);
        BookedJourney bookedJourney = passenger.getBookedJourney();
        Journey journey = bookedJourney.getJourney();
        // check if the journey's car is in user's official agency
        // borrow this method from journeyService
        journeyService.checkJourneyCarInOfficialAgency(journey);
        if (journeyNotStartedOrElseReject(journey) &&
                journeyNotTerminatedOrElseReject(journey) &&
                paymentAcceptedOrElseReject(bookedJourney.getPaymentTransaction())
                && passengerNotCheckedInOrElseReject(passenger.getPassengerCheckedInIndicator())) {
            passenger.setPassengerCheckedInIndicator(true);
            passengerRepository.save(passenger);
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
        return bookedJourneyRepository.findAllByJourneyId(journeyId).stream().filter(
                bookedJourney -> bookedJourney != null &&
                        filterCompletedPayments(bookedJourney.getPaymentTransaction())
        ).map(
                OnBoardingInfoDTO::new
        ).collect(Collectors.toList());
    }

    @Override
    public void changeSeatNumber(List<ChangeSeatDTO> changeSeatList, Long bookJourneyId) {
        // get the bookedJourney
        BookedJourney bookedJourney = getBookedJourneyById(bookJourneyId);
        Journey journey = bookedJourney.getJourney();
        if (journey != null) {
            // if journey already started throw exception
            if (journey.getDepartureIndicator()) {
                throw new ApiException(JOURNEY_ALREADY_STARTED.getMessage(),
                        JOURNEY_ALREADY_STARTED.toString(), HttpStatus.CONFLICT);
            }
            // if journey already terminated throw exception
            if (journey.getArrivalIndicator()) {
                throw new ApiException(JOURNEY_ALREADY_TERMINATED.getMessage(),
                        JOURNEY_ALREADY_TERMINATED.toString(), HttpStatus.CONFLICT);
            }
            // if journey payment not completed throw exception
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            if (paymentTransaction == null ||
                    !COMPLETED.toString()
                            .equals(paymentTransaction.getTransactionStatus())) {
                throw new ApiException(PAYMENT_NOT_COMPLETED.getMessage(),
                        PAYMENT_NOT_COMPLETED.toString(), HttpStatus.NOT_FOUND);
            }
            // map passenger to seat number
            Map<String, Passenger> bookedPassengerSeatMap = new HashMap<>();
            List<Passenger> passengers = bookedJourney.getPassengers();
            List<Passenger> notifiablePassengers = new ArrayList<>();
            List<Integer> myBookedSeats = new ArrayList<>();
            passengers.forEach(p -> {
                bookedPassengerSeatMap.put(String.valueOf(p.getSeatNumber()), p);
                myBookedSeats.add(p.getSeatNumber());
            });
            List<Integer> journeyBookedSeats = getAllBookedSeats(bookedJourney.getJourney().getId());
            List<ChangeSeatDTO> failureList = new ArrayList<>();
            List<ChangeSeatDTO> successList = new ArrayList<>();
            User user = bookedJourney.getUser();
            for (ChangeSeatDTO seatDTO : changeSeatList) {
                // check if old seat is in bookedJourney
                // if not ignore it
                Integer currSeat = seatDTO.getCurrentSeatNumber();
                Integer newSeat = seatDTO.getNewSeatNumber();
                if (myBookedSeats.contains(currSeat)) {
                    Passenger curr = bookedPassengerSeatMap.get(String.valueOf(currSeat));
                    int currIndex = passengers.indexOf(curr);
                    // check if new seat is in bookedJourney
                    // if yes, swap
                    if (myBookedSeats.contains(newSeat)) {// swap
                        Passenger newP = bookedPassengerSeatMap.get(String.valueOf(newSeat));
                        int newIndex = passengers.indexOf(newP);
                        curr.setSeatNumber(newSeat);
                        newP.setSeatNumber(currSeat);
                        // change QRCode
                        curr.setCheckedInCode(generateCode(user.getUserId(), journey, newSeat));
                        newP.setCheckedInCode(generateCode(user.getUserId(), journey, currSeat));
                        notifiablePassengers.add(curr);
                        notifiablePassengers.add(newP);
                        // change the bookedJourney passengers too
                        passengers.set(currIndex, curr);
                        passengers.set(newIndex, newP);
                        successList.add(seatDTO);
                    } else { // if not,
                        // check if a seat is already taken
                        if (journeyBookedSeats.contains(newSeat)) {// if a seat is already taken add to failure list
                            failureList.add(seatDTO);
                        } else {// change seat
                            curr.setSeatNumber(newSeat);
                            // change QRCode
                            curr.setCheckedInCode(generateCode(user.getUserId(), journey, newSeat));
                            notifiablePassengers.add(curr);
                            // change the bookedJourney passengers too
                            passengers.set(currIndex, curr);
                            successList.add(seatDTO);
                        }

                    }
                } else {
                    // throw exception
                    throw new ApiException(
                            RESOURCE_NOT_FOUND.getMessage(),
                            RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
                }
            }
            if (!notifiablePassengers.isEmpty()) {
                passengerRepository.saveAll(notifiablePassengers);
                // notify passengers
                bookedJourney.setPassengers(passengers);
                notifyPassengers(bookedJourney, getBookedJourneyStatusDTO(bookedJourney), notifiablePassengers);
            }
            if (!failureList.isEmpty()) {
                throw new ApiException(
                        SEAT_ALREADY_TAKEN.getMessage(),
                        SEAT_ALREADY_TAKEN.toString(), HttpStatus.CONFLICT);
            }
        }
    }

    private Double getSMSChargeAmount(BookJourneyRequest bookJourneyRequest, ServiceChargeDTO sCharge) {
        Set<String> phoneNumbers = new HashSet<>();
        for (BookJourneyRequest.Passenger passenger : bookJourneyRequest.getPassengers()) {
            phoneNumbers.add(passenger.getPhoneNumber().trim());
        }
        return sCharge.getFlatCharge() * phoneNumbers.size();
    }

    private void notifyPassengers(BookedJourney bookedJourney, BookedJourneyStatusDTO dto, List<Passenger> passengers) {
        passengers.forEach(passenger -> {
            String filename = passenger.getCheckedInCode() + "." + QRCodeProvider.STORAGE_FILE_FORMAT;
            try {
                byte[] qrCodeBytes = getQRCodeBytes(bookedJourney, passenger.getCheckedInCode());
                fileStorageService.saveFile(filename, qrCodeBytes, QRCodeProvider.STORAGE_FOLDER, FileAccessType.PROTECTED);
            } catch (IOException e) {
                log.info("could not generate QR Code");
                e.printStackTrace();
            }
        });
        sendChangeSeatTicketEmail(dto, passengers);
    }

    private PaymentTransaction getPaymentTransaction(Journey journey, User user, BookJourneyRequest bookJourneyRequest, Boolean isAgencyBooking) {
        List<Integer> bookSeats = bookJourneyRequest.getPassengers().stream()
                .map(BookJourneyRequest.Passenger::getSeatNumber)
                .collect(Collectors.toList());

        List<Passenger> bookPassengers = passengerRepository.findByBookedJourney_Journey_Id(journey.getId());
        bookPassengers.forEach(passenger -> {
            if (bookSeats.contains(passenger.getSeatNumber())) {
                throw new ApiException(SEAT_ALREADY_TAKEN.getMessage(), SEAT_ALREADY_TAKEN.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
        });

        verifyJourneyStatus(journey);
        List<Passenger> passengers = getPassenger(bookJourneyRequest, user.getUserId(), journey);

        Double amount;
        TransitAndStop transitAndStop;
        if (bookJourneyRequest.isDestinationIndicator()) {
            amount = journey.getAmount() * bookJourneyRequest.getPassengers().size();
            transitAndStop = journey.getDestination();
        } else {
            Optional<JourneyStop> journeyStopOptional = journey.getJourneyStops().stream()
                    .filter(stop -> stop.getTransitAndStop().getId().equals(bookJourneyRequest.getTransitAndStopId()))
                    .findFirst();
            if (!journeyStopOptional.isPresent()) {
                log.info("Transit and stop not found transitAndStopId: {}", bookJourneyRequest.getTransitAndStopId());
                throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
            JourneyStop journeyStop = journeyStopOptional.get();
            amount = journeyStop.getAmount();
            transitAndStop = journeyStop.getTransitAndStop();
        }

        BookedJourney bookedJourney;
        if (isAgencyBooking) {
            try {
                User proxy = getUserByEmail(bookJourneyRequest.getDirectToAccount());
                bookedJourney = getBookedJourney(passengers, proxy, user, journey, amount, transitAndStop);
            } catch (ApiException e) {
                bookedJourney = getBookedJourney(passengers, user, user, journey, amount, transitAndStop);
            }
        } else {
            bookedJourney = getBookedJourney(passengers, user, journey, amount, transitAndStop);
        }
        bookedJourney.setIsAgencyBooking(isAgencyBooking);
        bookedJourney.setSmsNotification(bookJourneyRequest.getSubscribeToSMSNotification());
        BookedJourney savedBookedJourney = bookedJourneyRepository.save(bookedJourney);
        passengers.forEach(passenger -> passenger.setBookedJourney(savedBookedJourney));
        passengerRepository.saveAll(passengers);

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        String[] names = currentAuthUser.getFullName().split(" ");
        String firstName = "Anonymous";
        String lastName = "Anonymous";
        if (names.length > 1) {
            firstName = names[0];
            lastName = names[names.length - 1];
        }
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(savedBookedJourney);

        paymentTransaction.setAmount(amount);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket for " + journey.getCar().getLicensePlateNumber());

        paymentTransaction.setAppTransactionNumber(UUID.randomUUID().toString());
        paymentTransaction.setAppUserEmail(currentAuthUser.getEmail());
        paymentTransaction.setAppUserFirstName(firstName);
        paymentTransaction.setAppUserLastName(lastName);
        paymentTransaction.setAppUserPhoneNumber(StringUtils.isEmpty(currentAuthUser.getPhoneNumber()) ? "670000000" : currentAuthUser.getPhoneNumber());

        paymentTransaction.setCancelRedirectUrl(paymentUrlResponseProps.getPayAmGoPaymentCancelUrl());
        paymentTransaction.setPaymentResponseUrl(paymentUrlResponseProps.getPayAmGoPaymentResponseUrl() + "/" + savedBookedJourney.getId());
        paymentTransaction.setReturnRedirectUrl(paymentUrlResponseProps.getPayAmGoPaymentRedirectUrl() + "/" + savedBookedJourney.getId());

        paymentTransaction.setLanguage("en");
        paymentTransaction.setTransactionStatus(INITIATED.toString());
        return paymentTransaction;
    }

    private void sendTicketEmail(BookedJourneyStatusDTO bookedJourneyStatusDTO) {

        String message = emailContentBuilder.buildTicketEmail(bookedJourneyStatusDTO);

        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setSubject(EmailFields.TICKET_SUBJECT.getMessage());
        emailDTO.setMessage(message);
        Set<EmailAddress> emailAddresses = bookedJourneyStatusDTO.getPassengers().stream()
                .map(passenger -> new EmailAddress(passenger.getPassengerEmail(), passenger.getPassengerEmail()))
                .collect(Collectors.toSet());

        emailDTO.setToAddresses(new ArrayList<>(emailAddresses));
        // setting cc and bcc to empty lists
        emailDTO.setCcAddresses(Collections.emptyList());
        emailDTO.setBccAddresses(Collections.emptyList());
        notificationService.sendEmail(emailDTO);
    }

    private void sendChangeSeatTicketEmail(BookedJourneyStatusDTO bookedJourneyStatusDTO, List<Passenger> passengers) {

        String message = emailContentBuilder.buildTicketEmail(bookedJourneyStatusDTO);

        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setSubject(EmailFields.UPDATED_TICKET_SUBJECT.getMessage());
        emailDTO.setMessage(message);
        Set<EmailAddress> emailAddresses = passengers.stream()
                .map(passenger -> new EmailAddress(passenger.getEmail(), passenger.getName()))
                .collect(Collectors.toSet());

        emailDTO.setToAddresses(new ArrayList<>(emailAddresses));
        // setting cc and bcc to empty lists
        emailDTO.setCcAddresses(Collections.emptyList());
        emailDTO.setBccAddresses(Collections.emptyList());
        notificationService.sendEmail(emailDTO);
    }

    private BookedJourneyStatusDTO getBookedJourneyStatusDTO(BookedJourney bookedJourney) {

        BookedJourneyStatusDTO bookedJourneyStatusDTO = new BookedJourneyStatusDTO();
        bookedJourneyStatusDTO.setId(bookedJourney.getId());
        bookedJourneyStatusDTO.setJourneyId(bookedJourney.getJourney().getId());
        bookedJourneyStatusDTO.setSubscribeToSMSNotification(bookedJourney.getSmsNotification());
        PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();

        bookedJourneyStatusDTO.setAmount(paymentTransaction.getAmount());
        bookedJourneyStatusDTO.setServiceChargeAmount(paymentTransaction.getServiceChargeAmount());
        bookedJourneyStatusDTO.setAgencyAmount(paymentTransaction.getAgencyAmount());

        bookedJourneyStatusDTO.setPaymentDate(paymentTransaction.getPaymentDate());
        bookedJourneyStatusDTO.setCurrencyCode(paymentTransaction.getCurrencyCode());
        bookedJourneyStatusDTO.setPaymentReason(paymentTransaction.getPaymentReason());
        bookedJourneyStatusDTO.setPaymentStatus(paymentTransaction.getTransactionStatus());
        bookedJourneyStatusDTO.setTransactionId(paymentTransaction.getId());
        bookedJourneyStatusDTO.setDepartureIndicator(bookedJourney.getJourney().getDepartureIndicator());
        bookedJourneyStatusDTO.setArrivalIndicator(bookedJourney.getJourney().getArrivalIndicator());

        bookedJourneyStatusDTO.setPaymentChannelTransactionNumber(paymentTransaction.getPaymentChannelTransactionNumber());
        bookedJourneyStatusDTO.setPaymentChannel(paymentTransaction.getPaymentChannel());

        RefundPaymentTransaction refundPaymentTransaction = paymentTransaction.getRefundPaymentTransaction();
        bookedJourneyStatusDTO.setHasRefundRequest(true);
        bookedJourneyStatusDTO.setRefundStatus(RefundStatus.DECLINED);

        if (refundPaymentTransaction == null) {
            bookedJourneyStatusDTO.setHasRefundRequest(false);
            bookedJourneyStatusDTO.setRefundStatus(null);
        } else {
            bookedJourneyStatusDTO.setRefundStatus(RefundStatus.valueOf(refundPaymentTransaction.getRefundStatus()));
            bookedJourneyStatusDTO.setRefundAmount(refundPaymentTransaction.getAmount());
            bookedJourneyStatusDTO.setRefundId(refundPaymentTransaction.getId());
        }

        bookedJourneyStatusDTO.setCarDriverName(bookedJourney.getJourney().getDriver().getDriverName());
        bookedJourneyStatusDTO.setCarLicenseNumber(bookedJourney.getJourney().getCar().getLicensePlateNumber());
        bookedJourneyStatusDTO.setCarName(bookedJourney.getJourney().getCar().getName());
        if (bookedJourney.getJourney().getCar().getIsOfficialAgencyIndicator()) {
            Bus bus = (Bus) bookedJourney.getJourney().getCar();
            bookedJourneyStatusDTO.setAgencyName(bus.getOfficialAgency().getAgencyName());
            bookedJourneyStatusDTO.setAgencyLogo(fileStorageService.getFilePath(bus.getOfficialAgency().getLogo(), "", FileAccessType.PROTECTED));
        }

        Location departureLocation = bookedJourney.getJourney().getDepartureLocation().getLocation();
        bookedJourneyStatusDTO.setDepartureLocation(departureLocation.getAddress() + ", " + departureLocation.getCity() + ", " + departureLocation.getState() + ", " + departureLocation.getCountry());
        bookedJourneyStatusDTO.setDepartureTime(bookedJourney.getJourney().getDepartureTime());
        bookedJourneyStatusDTO.setEstimatedArrivalTime(bookedJourney.getJourney().getEstimatedArrivalTime());

        Location destinationLocation = bookedJourney.getDestination().getLocation();
        bookedJourneyStatusDTO.setDestinationLocation(destinationLocation.getAddress() + ", " + destinationLocation.getCity() + ", " + destinationLocation.getState() + ", " + destinationLocation.getCountry());

        List<PassengerDTO> passengers = bookedJourney.getPassengers().stream()
                .map(pge -> {
                    PassengerDTO passenger = new PassengerDTO();
                    passenger.setPassengerEmail(pge.getEmail());
                    passenger.setPassengerName(pge.getName());
                    passenger.setPassengerIdNumber(pge.getIdNumber());
                    passenger.setPassengerPhoneNumber(pge.getPhoneNumber());
                    passenger.setPassengerSeatNumber(pge.getSeatNumber());
                    passenger.setCheckedInCode(pge.getCheckedInCode());
                    passenger.setCheckedIn(pge.getPassengerCheckedInIndicator());
                    String encoding = null;
                    try {
                        encoding = getQREncodedImage(bookedJourney, pge.getCheckedInCode());
                    } catch (IOException e) {
                        log.info("could not generate QR Code");
                        e.printStackTrace();
                    }
                    passenger.setQRCheckedInImage(encoding);
                    String filename = passenger.getCheckedInCode() + "." + QRCodeProvider.STORAGE_FILE_FORMAT;
                    String filePath = fileStorageService.getFilePath(filename, QRCodeProvider.STORAGE_FOLDER, FileAccessType.PROTECTED);
                    passenger.setQRCheckedInImageUrl(filePath);
                    return passenger;
                }).collect(Collectors.toList());

        bookedJourneyStatusDTO.setPassengers(passengers);

        return bookedJourneyStatusDTO;
    }

    private String getQREncodedImage(BookedJourney bookedJourney, String code) throws IOException {
        byte[] imageBytes = getQRCodeBytes(bookedJourney, code);

        Base64.Encoder encoder = Base64.getEncoder();
        return "data:image/png;base64," + encoder.encodeToString(imageBytes);
    }

    private byte[] getQRCodeBytes(BookedJourney bookedJourney, String code) throws IOException {
        BufferedImage bufferedImage = QRCodeProvider.generateQRCodeImage(code);
        String pathname = "image" + bookedJourney.getId() + ".png";
        File file = new File(pathname);
        ImageIO.write(bufferedImage, "png", file);
        byte[] imageBytes = Files.readAllBytes(Paths.get(pathname));
        file.delete();
        return imageBytes;
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

    private BookedJourney getBookedJourney(List<Passenger> passengers, User user, Journey journey, Double amount, TransitAndStop transitAndStop) {
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassengers(passengers);
        bookedJourney.setUser(user);
        bookedJourney.setJourney(journey);
        bookedJourney.setDestination(transitAndStop);
        bookedJourney.setAmount(amount);
        return bookedJourney;
    }

    private BookedJourney getBookedJourney(
            List<Passenger> passengers, User user, User agencyUser,
            Journey journey, Double amount, TransitAndStop transitAndStop) {
        BookedJourney bookedJourney = getBookedJourney(passengers, user, journey, amount, transitAndStop);
        bookedJourney.setAgencyUser(agencyUser);
        return bookedJourney;
    }

    private List<Passenger> getPassenger(BookJourneyRequest bookJourneyRequest, String userId, Journey journey) {
        return bookJourneyRequest.getPassengers().stream()
                .map(psngr -> {
                    Passenger passenger = new Passenger();
                    passenger.setIdNumber(psngr.getPassengerIdNumber());
                    passenger.setName(psngr.getPassengerName());
                    passenger.setSeatNumber(psngr.getSeatNumber());
                    passenger.setEmail(psngr.getEmail());
                    passenger.setPhoneNumber(psngr.getPhoneNumber());
                    passenger.setPassengerCheckedInIndicator(false);
                    passenger.setCheckedInCode(generateCode(userId, journey, psngr.getSeatNumber()));
                    return passenger;
                }).collect(Collectors.toList());

    }

    private User getUser() {
        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return currentAuthUserOptional.get();
    }

    private User getUserByEmail(String email) {
        Optional<User> optional = userRepository.findFirstByEmail(email);
        if (!optional.isPresent()) {
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return optional.get();
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
            log.info("Journey not found journeyId: {}", id);
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return journeyOptional.get();
    }

    private Passenger getBookedJourneyByCheckedInCode(String code) {
        Optional<Passenger> optional = passengerRepository.findByCheckedInCode(code);
        if (!optional.isPresent()) {
            log.info("CheckedInCode not found code: {}", code);
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optional.get();
    }

    private BookedJourney getBookedJourneyById(Long bookJourneyId) {
        Optional<BookedJourney> optional = bookedJourneyRepository.findById(bookJourneyId);
        if (!optional.isPresent()) {
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
        if (paymentTransaction == null) {
            log.info("No transaction");
            throw new ApiException(RESOURCE_NOT_FOUND.getMessage(), RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        } else if (!paymentTransaction.getTransactionStatus().equals(COMPLETED.toString())) {
            log.info("payment transaction declined: {}", paymentTransaction.getAppTransactionNumber());
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

    private boolean filterCompletedPayments(PaymentTransaction paymentTransaction) {
        return paymentTransaction != null && paymentTransaction.getTransactionStatus().equals(COMPLETED.toString());
    }

    private String generateCode(String userId, Journey journey, Integer seat) {
        return CheckInCodeGenerator.generateCode(userId, journey, seat);
    }
}
