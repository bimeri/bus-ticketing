package net.gogroups.gowaka.domain.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/20/20 4:31 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {

    private RefundService refundService;

    @Mock
    private RefundPaymentTransactionRepository mockRefundPaymentTransactionRepository;
    @Mock
    private PaymentTransactionRepository mockPaymentTransactionRepository;
    @Mock
    private UserRepository mockUserRepository;

    private ArgumentCaptor<RefundPaymentTransaction> refundPaymentTransactionArgumentCaptor = ArgumentCaptor.forClass(RefundPaymentTransaction.class);

    @BeforeEach
    void setUp() {
        refundService = new RefundServiceImpl(mockPaymentTransactionRepository, mockRefundPaymentTransactionRepository, mockUserRepository);
    }

    @Test
    void requestRefund_calls_CreateNewRefund() {

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setId(3L);
        paymentTransaction.setTransactionStatus("COMPLETED");
        when(mockPaymentTransactionRepository.findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(3L, 2L, "123"))
                .thenReturn(Optional.of(paymentTransaction));
        refundService.requestRefund(new RequestRefundDTO(2L, 3L, "please refund"), "123");
        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundRequestMessage()).isEqualTo("please refund");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRequestedDate()).isNotNull();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getIsRefunded()).isFalse();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getIsRefundApproved()).isFalse();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getPaymentTransaction()).isEqualTo(paymentTransaction);

    }

    @Test
    void requestRefund_throwsException_whenTransactionForUserCantNotBeFound() {

        when(mockPaymentTransactionRepository.findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(3L, 2L, "123"))
                .thenReturn(Optional.empty());
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> refundService.requestRefund(new RequestRefundDTO(2L, 3L, "please refund"), "123"));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("No transaction found.");
        verifyNoInteractions(mockRefundPaymentTransactionRepository);

    }

    @Test
    void requestRefund_throwsException_whenTransactionIsNotCompleted() {

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setId(3L);
        paymentTransaction.setTransactionStatus("WAITING");
        when(mockPaymentTransactionRepository.findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(3L, 2L, "123"))
                .thenReturn(Optional.of(paymentTransaction));
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> refundService.requestRefund(new RequestRefundDTO(2L, 3L, "please refund"), "123"));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("No transaction found.");
        verifyNoInteractions(mockRefundPaymentTransactionRepository);

    }

    @Test
    void requestRefund_throwsException_whenRequestAlreadyCreated() {

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setId(3L);
        paymentTransaction.setTransactionStatus("COMPLETED");
        paymentTransaction.setRefundPaymentTransaction(new RefundPaymentTransaction());
        when(mockPaymentTransactionRepository.findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(3L, 2L, "123"))
                .thenReturn(Optional.of(paymentTransaction));
        ResourceAlreadyExistException resourceAlreadyExistException = assertThrows(ResourceAlreadyExistException.class, () -> refundService.requestRefund(new RequestRefundDTO(2L, 3L, "please refund"), "123"));
        assertThat(resourceAlreadyExistException.getMessage()).isEqualTo("Resource already exist.");
        verifyNoInteractions(mockRefundPaymentTransactionRepository);

    }

    @Test
    void responseRefund_throwsException_whenRefundNotFound() {

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.empty());
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("No transaction found.");

    }

    @Test
    void responseRefund_throwsException_whenUserNotFound() {

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(new RefundPaymentTransaction()));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.empty());
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("No transaction found.");

    }

    @Test
    void responseRefund_throwsException_whenCarNotABus() {

        Journey journey = new Journey();
        journey.setCar(new SharedRide());

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(new User()));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User not in this agency");
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void responseRefund_throwsException_whenUserNotInAnAgency() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);

        User user = new User();
        user.setOfficialAgency(null);

        Journey journey = new Journey();
        journey.setCar(bus);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User not in this agency");
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void responseRefund_throwsException_whenUserInADifferentAgency() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);

        User user = new User();
        OfficialAgency diffOfficialAgency = new OfficialAgency();
        diffOfficialAgency.setId(2L);
        user.setOfficialAgency(diffOfficialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User not in this agency");
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void responseRefund_throwException_whenAlreadyRefunded() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);

        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("email@email.com");
        user.setOfficialAgency(officialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        refundPaymentTransaction.setIsRefunded(true);
        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(apiException.getErrorCode()).isEqualTo("ALREADY_REFUNDED_REQUEST");
        assertThat(apiException.getMessage()).isEqualTo("Request already redunded");
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    }

    @Test
    void responseRefund_approve_requestForRefund() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);

        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("email@email.com");
        user.setOfficialAgency(officialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.responseRefund(2L, new ResponseRefundDTO(true, "I have approved", 1000.00), "123");
        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getIsRefundApproved()).isTrue();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getAmount()).isEqualTo(1000.00);
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRespondedDate()).isNotNull();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getApprovalEmail()).isEqualTo("email@email.com");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getApprovalName()).isEqualTo("John Doe");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundResponseMessage()).isEqualTo("I have approved");

    }

    @Test
    void getUserRefund_throwException_whenRefundNotFound() {

        when(mockRefundPaymentTransactionRepository.findByIdAndPaymentTransaction_BookedJourney_User_UserId(1L, "123"))
                .thenReturn(Optional.empty());
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> refundService.getUserRefund(1L, "123"));
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("Refund request not found.");
    }

    @Test
    void getUserRefund_return_UserRefundRequest() {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setAmount(1000.00);
        when(mockRefundPaymentTransactionRepository.findByIdAndPaymentTransaction_BookedJourney_User_UserId(1L, "123"))
                .thenReturn(Optional.of(refundPaymentTransaction));
        RefundDTO userRefund = refundService.getUserRefund(1L, "123");
        assertThat(userRefund.getAmount()).isEqualTo(1000.00);
    }

    @Test
    void getAllJourneyRefunds_return_ListOfUserRefundRequest() {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setAmount(1000.00);
        when(mockRefundPaymentTransactionRepository.findByPaymentTransaction_BookedJourney_Journey_IdAndPaymentTransaction_BookedJourney_User_UserId(1L, "123"))
                .thenReturn(Collections.singletonList(refundPaymentTransaction));
        List<RefundDTO> userRefunds = refundService.getAllJourneyRefunds(1L, "123");
        assertThat(userRefunds.get(0).getAmount()).isEqualTo(1000.00);
    }

    @Test
    void refunded_refunds_requestForRefund() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);

        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("email@email.com");
        user.setOfficialAgency(officialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.refunded(2L, "123");
        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getIsRefunded()).isTrue();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefunderEmail()).isEqualTo("email@email.com");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefunderName()).isEqualTo("John Doe");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundedDate()).isNotNull();

    }

}
