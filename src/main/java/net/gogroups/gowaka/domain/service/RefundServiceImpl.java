package net.gogroups.gowaka.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.constant.RefundStatus;
import net.gogroups.gowaka.constant.notification.EmailFields;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.domain.service.utilities.CheckInCodeGenerator;
import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ResourceAlreadyExistException;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.RefundService;
import net.gogroups.notification.model.EmailAddress;
import net.gogroups.notification.model.SendEmailDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.payamgo.constants.PayAmGoPaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.exception.ErrorCodes.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:24 AM <br/>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {


    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundPaymentTransactionRepository refundPaymentTransactionRepository;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final JourneyRepository journeyRepository;
    private final EmailContentBuilder emailContentBuilder;
    private final NotificationService notificationService;


    @Override
    public void requestRefund(RequestRefundDTO requestRefundDTO, String userId) {

        Optional<PaymentTransaction> paymentTransactionOptional = paymentTransactionRepository.findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(requestRefundDTO.getTransactionId(), requestRefundDTO.getBookedJourneyId(), userId);
        if (!paymentTransactionOptional.isPresent()) {
            log.info("Can not create refund request. " +
                    "No transaction found for paymentTransactionId: '{}',  bookJourneyId: '{}' for user : '{}'", requestRefundDTO.getTransactionId(), requestRefundDTO.getBookedJourneyId(), userId);
            throw new ResourceNotFoundException("No transaction found.");
        }
        if (!PayAmGoPaymentStatus.COMPLETED.toString().equals(paymentTransactionOptional.get().getTransactionStatus())) {
            log.info("Transaction is not completed. paymentTransactionId: '{}'", requestRefundDTO.getTransactionId());
            throw new ResourceNotFoundException("No transaction found.");
        }
        if (paymentTransactionOptional.get().getRefundPaymentTransaction() != null) {
            log.info("User already created request for refund. paymentTransactionId: '{}', refundId : '{}'", requestRefundDTO.getTransactionId(), paymentTransactionOptional.get().getRefundPaymentTransaction().getId());
            throw new ResourceAlreadyExistException(RESOURCE_ALREADY_EXIST.getMessage());
        }

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransactionOptional.get());
        refundPaymentTransaction.setRefundStatus(RefundStatus.PENDING.name());
        refundPaymentTransaction.setRefundRequestMessage(requestRefundDTO.getMessage());
        refundPaymentTransaction.setRequestedDate(LocalDateTime.now());

        refundPaymentTransactionRepository.save(refundPaymentTransaction);

    }

    @Override
    public void responseRefund(Long refundId, ResponseRefundDTO responseRefundDTO, String userId) {

        handleApprovalRefundFlow(refundId, userId, (refundPaymentTransaction, user) -> {
            if (responseRefundDTO.getAmount() > refundPaymentTransaction.getPaymentTransaction().getAgencyAmount()) {
                throw new ApiException(INVALID_AMOUNT_LIMIT.getMessage(), INVALID_AMOUNT_LIMIT.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
            refundPaymentTransaction.setRefundStatus(responseRefundDTO.getIsRefundApproved() ? RefundStatus.APPROVED.name() : RefundStatus.DECLINED.name());
            refundPaymentTransaction.setAmount(responseRefundDTO.getAmount());
            refundPaymentTransaction.setApprovalName(user.getFullName());
            refundPaymentTransaction.setApprovalEmail(user.getEmail());
            refundPaymentTransaction.setRespondedDate(LocalDateTime.now());
            refundPaymentTransaction.setRefundResponseMessage(responseRefundDTO.getMessage());

            // change seat and checkInCode if request approval is true
            if (RefundStatus.valueOf(refundPaymentTransaction.getRefundStatus()) == RefundStatus.APPROVED) {
                BookedJourney bookedJourney = refundPaymentTransaction.getPaymentTransaction().getBookedJourney();
                List<Passenger> passengerList = new ArrayList<>();
                for (Passenger passenger : bookedJourney.getPassengers()) {
                    passenger.setSeatNumber(-1);
                    passenger.setCheckedInCode(CheckInCodeGenerator
                            .generateCode(bookedJourney.getJourney(), -1, "CHECK_IN"));
                    passengerList.add(passenger);
                }
                passengerRepository.saveAll(passengerList);
            }
            // send email
            sendRefundEmail(refundPaymentTransaction);
            refundPaymentTransactionRepository.save(refundPaymentTransaction);
        });
    }

    @Override
    public RefundDTO getUserRefund(Long refundId, String userId) {
        Optional<RefundPaymentTransaction> refundPaymentTransactionOptional = refundPaymentTransactionRepository.findByIdAndPaymentTransaction_BookedJourney_User_UserId(refundId, userId);
        if (!refundPaymentTransactionOptional.isPresent()) {
            log.info("request response not found. userId: {}, refundId: {}", userId, refundId);
            throw new ResourceNotFoundException("Refund request not found.");
        }
        return getRefundDTO(refundPaymentTransactionOptional.get());
    }

    @Override
    public List<RefundDTO> getAllJourneyRefunds(Long journeyId, String userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            log.info("User not found userId: '{}'", userId);
            throw new ResourceNotFoundException("User not found.");
        }
        Optional<Journey> journeyOptional = journeyRepository.findById(journeyId);
        if (!journeyOptional.isPresent()) {
            log.info("Journey not found journeyId: '{}'", journeyId);
            throw new ResourceNotFoundException("Journey not found.");
        }
        Journey journey = journeyOptional.get();
        OfficialAgency officialAgency = userOptional.get().getOfficialAgency();
        if (journey.getCar() instanceof Bus && officialAgency.getId().equals(((Bus) journey.getCar()).getOfficialAgency().getId())) {
            return refundPaymentTransactionRepository.findByPaymentTransaction_BookedJourney_Journey_Id(journeyId).stream()
                    .map(this::getRefundDTO)
                    .collect(Collectors.toList());
        }
        log.info("user agency do not own this journey. journeyId: '{}', userId: {}", journeyId, userId);
        throw new ResourceNotFoundException("Resource not found.");

    }

    @Override
    public void refunded(Long refundId, String userId) {

        handleApprovalRefundFlow(refundId, userId, (refundPaymentTransaction, user) -> {
            if (RefundStatus.valueOf(refundPaymentTransaction.getRefundStatus()) != RefundStatus.APPROVED) {
                log.info("refund request has not been approved: refundId: {}", refundId);
                throw new ApiException(REFUND_REQUEST_NOT_APPROVED.getMessage(), REFUND_REQUEST_NOT_APPROVED.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
            refundPaymentTransaction.setRefundStatus(RefundStatus.REFUNDED.name());
            refundPaymentTransaction.setRefunderName(user.getFullName());
            refundPaymentTransaction.setRefunderEmail(user.getEmail());
            refundPaymentTransaction.setRefundedDate(LocalDateTime.now());
            // send email
            sendRefundEmail(refundPaymentTransaction);
            refundPaymentTransactionRepository.save(refundPaymentTransaction);
        });
    }

    private interface ApprovalRefundFlowHandler {
        void handle(RefundPaymentTransaction refundPaymentTransaction, User user);
    }

    private void handleApprovalRefundFlow(Long refundId, String userId, ApprovalRefundFlowHandler approvalRefundFlowHandler) {
        Optional<RefundPaymentTransaction> refundPaymentTransactionOptional = refundPaymentTransactionRepository.findById(refundId);
        if (!refundPaymentTransactionOptional.isPresent()) {
            log.info("Can not find transaction: '{}'", refundId);
            throw new ResourceNotFoundException("No transaction found.");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            log.info("User not found userId: '{}' to approve refund: '{}'", userId, refundId);
            throw new ResourceNotFoundException("No transaction found.");
        }
        RefundPaymentTransaction refundPaymentTransaction = refundPaymentTransactionOptional.get();
        User user = userOptional.get();
        Car car = refundPaymentTransaction.getPaymentTransaction().getBookedJourney().getJourney().getCar();

        if (car instanceof Bus) {
            OfficialAgency officialAgency = ((Bus) car).getOfficialAgency();
            if (user.getOfficialAgency() != null && user.getOfficialAgency().getId().equals(officialAgency.getId())) {
                if (RefundStatus.valueOf(refundPaymentTransaction.getRefundStatus()) == RefundStatus.REFUNDED) {
                    throw new ApiException(ALREADY_REFUNDED_REQUEST.getMessage(), ALREADY_REFUNDED_REQUEST.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
                }
                approvalRefundFlowHandler.handle(refundPaymentTransaction, user);
            } else {
                log.info("User not in agency. userId: '{}' to approve refund: '{}'", userId, refundId);
                throw new ApiException(USER_NOT_IN_AGENCY.getMessage(), USER_NOT_IN_AGENCY.toString(), HttpStatus.NOT_FOUND);
            }
        } else {
            log.info("Car not a bus in an agency. userid: '{}', car: '{}'", userId, car.getId());
            throw new ApiException(USER_NOT_IN_AGENCY.getMessage(), USER_NOT_IN_AGENCY.toString(), HttpStatus.NOT_FOUND);
        }
    }

    private RefundDTO getRefundDTO(RefundPaymentTransaction refundPaymentTransaction) {

        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setId(refundPaymentTransaction.getId());
        refundDTO.setAmount(refundPaymentTransaction.getAmount());
        refundDTO.setRefundRequestMessage(refundPaymentTransaction.getRefundRequestMessage());
        refundDTO.setRequestedDate(refundPaymentTransaction.getRequestedDate());
        refundDTO.setRefundResponseMessage(refundPaymentTransaction.getRefundResponseMessage());
        refundDTO.setRespondedDate(refundPaymentTransaction.getRespondedDate());
        refundDTO.setApprovalName(refundPaymentTransaction.getApprovalName());
        refundDTO.setApprovalEmail(refundPaymentTransaction.getApprovalEmail());
        refundDTO.setRefunderName(refundPaymentTransaction.getRefunderName());
        refundDTO.setRefunderEmail(refundPaymentTransaction.getRefunderEmail());
        refundDTO.setRefundedDate(refundPaymentTransaction.getRefundedDate());

        refundDTO.setStatus(RefundStatus.valueOf(refundPaymentTransaction.getRefundStatus()));
        PaymentTransaction paymentTransaction = refundPaymentTransaction.getPaymentTransaction();
        BookedJourney bookedJourneyEntity = paymentTransaction.getBookedJourney();

        RefundDTO.BookedJourney bookedJourney = new RefundDTO.BookedJourney();
        bookedJourney.setId(bookedJourneyEntity.getId());
        bookedJourney.setAgencyCharge(paymentTransaction.getAgencyAmount());
        bookedJourney.setDepartureTime(bookedJourneyEntity.getJourney().getDepartureTime());

        bookedJourney.setDestination(getLocationStr(bookedJourneyEntity.getDestination()));
        bookedJourney.setDeparture(getLocationStr(bookedJourneyEntity.getJourney().getDepartureLocation()));
        refundDTO.setBookedJourney(bookedJourney);

        try {
            User user = bookedJourneyEntity.getUser();
            refundDTO.setUser(new RefundDTO.User(user.getFullName(), user.getEmail(), user.getPhoneNumber()));
        } catch (NullPointerException exception) {
            log.info("a null pointer was thrown while getting user info for refund: {}", refundPaymentTransaction.getId());
        }

        return refundDTO;
    }

    private String getLocationStr(TransitAndStop destination) {
        return destination.getLocation().getAddress()+", "+ destination.getLocation().getCity()
                +", "+ destination.getLocation().getState()+", "+ destination.getLocation().getCountry();
    }

    private void sendRefundEmail(RefundPaymentTransaction refundPaymentTransaction) {
        User user = refundPaymentTransaction.getPaymentTransaction().getBookedJourney().getUser();
        if (user != null) {
            String message = emailContentBuilder.buildRefundStatusEmail(refundPaymentTransaction, user);
            SendEmailDTO emailDTO = new SendEmailDTO();
            emailDTO.setSubject(EmailFields.REFUND_UPDATE_SUBJECT.getMessage());
            emailDTO.setMessage(message);
            EmailAddress emailAddress = new EmailAddress(user.getEmail(), user.getFullName());
            emailDTO.setToAddresses(Collections.singletonList(emailAddress));
            // setting cc and bcc to empty lists
            emailDTO.setCcAddresses(Collections.emptyList());
            emailDTO.setBccAddresses(Collections.emptyList());
            notificationService.sendEmail(emailDTO);
        }
    }
}
