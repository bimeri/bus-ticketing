package net.gogroups.gowaka.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.PaymentTransactionRepository;
import net.gogroups.gowaka.domain.repository.RefundPaymentTransactionRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ResourceAlreadyExistException;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.RefundService;
import net.gogroups.payamgo.constants.PayAmGoPaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
public class RefundServiceImpl implements RefundService {


    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundPaymentTransactionRepository refundPaymentTransactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public RefundServiceImpl(PaymentTransactionRepository paymentTransactionRepository, RefundPaymentTransactionRepository refundPaymentTransactionRepository, UserRepository userRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.refundPaymentTransactionRepository = refundPaymentTransactionRepository;
        this.userRepository = userRepository;
    }

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
        refundPaymentTransaction.setIsRefunded(false);
        refundPaymentTransaction.setIsRefundApproved(false);
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
            refundPaymentTransaction.setIsRefundApproved(responseRefundDTO.getIsRefundApproved());
            refundPaymentTransaction.setAmount(responseRefundDTO.getAmount());
            refundPaymentTransaction.setApprovalName(user.getFullName());
            refundPaymentTransaction.setApprovalEmail(user.getEmail());
            refundPaymentTransaction.setRespondedDate(LocalDateTime.now());
            refundPaymentTransaction.setRefundResponseMessage(responseRefundDTO.getMessage());
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

        return refundPaymentTransactionRepository.findByPaymentTransaction_BookedJourney_Journey_IdAndPaymentTransaction_BookedJourney_User_UserId(journeyId, userId).stream()
                .map(this::getRefundDTO)
                .collect(Collectors.toList());

    }

    @Override
    public void refunded(Long refundId, String userId) {

        handleApprovalRefundFlow(refundId, userId, (refundPaymentTransaction, user) -> {
            refundPaymentTransaction.setIsRefunded(true);
            refundPaymentTransaction.setRefunderName(user.getFullName());
            refundPaymentTransaction.setRefunderEmail(user.getEmail());
            refundPaymentTransaction.setRefundedDate(LocalDateTime.now());
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
                if (refundPaymentTransaction.getIsRefunded()) {
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
        refundDTO.setIsRefundApproved(refundPaymentTransaction.getIsRefundApproved());
        refundDTO.setIsRefunded(refundPaymentTransaction.getIsRefunded());
        refundDTO.setApprovalName(refundPaymentTransaction.getApprovalName());
        refundDTO.setApprovalEmail(refundPaymentTransaction.getApprovalEmail());
        refundDTO.setRefunderName(refundPaymentTransaction.getRefunderName());
        refundDTO.setRefunderEmail(refundPaymentTransaction.getRefunderEmail());
        refundDTO.setRefundedDate(refundPaymentTransaction.getRefundedDate());
        return refundDTO;
    }
}
