package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.constant.RefundStatus;
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
import net.gogroups.notification.service.NotificationService;
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
    @Mock
    private PassengerRepository mockPassengerRepository;
    @Mock
    private EmailContentBuilder mockEmailContentBuilder;

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private JourneyRepository mockJourneyRepository;

    private ArgumentCaptor<RefundPaymentTransaction> refundPaymentTransactionArgumentCaptor = ArgumentCaptor.forClass(RefundPaymentTransaction.class);

    private ArgumentCaptor<List<Passenger>> passengerArgumentCaptorList = ArgumentCaptor.forClass(List.class);

    @BeforeEach
    void setUp() {
        refundService = new RefundServiceImpl(mockPaymentTransactionRepository,
                mockRefundPaymentTransactionRepository, mockUserRepository, mockPassengerRepository,
                mockJourneyRepository, mockEmailContentBuilder, mockNotificationService);

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
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundStatus()).isEqualTo("PENDING");
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
        refundPaymentTransaction.setRefundStatus(RefundStatus.REFUNDED.name());
        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "please refund", 1000.00), "123"));
        assertThat(apiException.getErrorCode()).isEqualTo("ALREADY_REFUNDED_REQUEST");
        assertThat(apiException.getMessage()).isEqualTo("Request already refunded");
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
        paymentTransaction.setAgencyAmount(1000.0);
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("PENDING");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(refundPaymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.responseRefund(2L, new ResponseRefundDTO(true, "I have approved", 1000.00), "123");
        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundStatus()).isEqualTo("APPROVED");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getAmount()).isEqualTo(1000.00);
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRespondedDate()).isNotNull();
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getApprovalEmail()).isEqualTo("email@email.com");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getApprovalName()).isEqualTo("John Doe");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundResponseMessage()).isEqualTo("I have approved");

    }


    @Test
    void responseRefund_throwException_whenAmountLimitInvalid() {

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
        paymentTransaction.setAgencyAmount(1000.0);
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("PENDING");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(refundPaymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.responseRefund(2L, new ResponseRefundDTO(true, "I have approved", 1500.00), "123"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("INVALID_AMOUNT_LIMIT");
        assertThat(apiException.getMessage()).isEqualTo("Amount must not be more than ticket fee");
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
        refundPaymentTransaction.setRefundStatus("APPROVED");
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        BookedJourney bookedJourney = new BookedJourney();
        Journey journey = new Journey();
        TransitAndStop transitAndStop = new TransitAndStop();
        Location location = new Location();
        location.setCity("Buea");
        location.setAddress("mile 17");
        location.setState("SW");
        location.setCountry("Cameroon");
        transitAndStop.setLocation(location);
        journey.setDepartureLocation(transitAndStop);
        bookedJourney.setJourney(journey);
        bookedJourney.setDestination(transitAndStop);
        paymentTransaction.setBookedJourney(bookedJourney);
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        when(mockRefundPaymentTransactionRepository.findByIdAndPaymentTransaction_BookedJourney_User_UserId(1L, "123"))
                .thenReturn(Optional.of(refundPaymentTransaction));

        RefundDTO userRefund = refundService.getUserRefund(1L, "123");


        assertThat(userRefund.getAmount()).isEqualTo(1000.00);
        assertThat(userRefund.getBookedJourney().getDeparture()).isEqualTo("mile 17, Buea, SW, Cameroon");
        assertThat(userRefund.getBookedJourney().getDestination()).isEqualTo("mile 17, Buea, SW, Cameroon");
    }

    @Test
    void getAllJourneyRefunds_return_ListOfUserRefundRequest() {

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setAmount(1000.00);
        refundPaymentTransaction.setRefundStatus("PENDING");
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        BookedJourney bookedJourney = new BookedJourney();
        Journey journey = new Journey();
        TransitAndStop transitAndStop = new TransitAndStop();
        Location location = new Location();
        location.setCity("Buea");
        location.setAddress("mile 17");
        location.setState("SW");
        location.setCountry("Cameroon");
        transitAndStop.setLocation(location);
        journey.setDepartureLocation(transitAndStop);
        bookedJourney.setJourney(journey);
        bookedJourney.setDestination(transitAndStop);
        paymentTransaction.setBookedJourney(bookedJourney);
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);
        when(mockRefundPaymentTransactionRepository.findByPaymentTransaction_BookedJourney_Journey_Id(1L))
                .thenReturn(Collections.singletonList(refundPaymentTransaction));

        OfficialAgency officialAgency = new OfficialAgency();
        User user = new User();
        officialAgency.setId(99L);
        user.setOfficialAgency(officialAgency);

        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));

        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency);
        journey.setCar(bus);
        when(mockJourneyRepository.findById(1L))
                .thenReturn(Optional.of(journey));
        List<RefundDTO> userRefunds = refundService.getAllJourneyRefunds(1L, "123");
        assertThat(userRefunds.get(0).getAmount()).isEqualTo(1000.00);
    }

    @Test
    void getAllJourneyRefunds_throwsException_when_userNotFound() {

        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> refundService.getAllJourneyRefunds(1L, "123"));
        assertThat(exception.getMessage()).isEqualTo("User not found.");
    }

    @Test
    void getAllJourneyRefunds_throwsException_when_journeyNotFound() {

        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(new User()));
        when(mockJourneyRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> refundService.getAllJourneyRefunds(1L, "123"));
        assertThat(exception.getMessage()).isEqualTo("Journey not found.");
    }

    @Test
    void getAllJourneyRefunds_throwsException_whenJourneyNotInUserAgency() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(99L);
        OfficialAgency officialAgency2 = new OfficialAgency();
        officialAgency2.setId(98L);

        User user = new User();
        user.setOfficialAgency(officialAgency);

        Journey journey = new Journey();
        Bus bus = new Bus();
        bus.setOfficialAgency(officialAgency2);
        journey.setCar(bus);

        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(1L))
                .thenReturn(Optional.of(journey));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> refundService.getAllJourneyRefunds(1L, "123"));
        assertThat(exception.getMessage()).isEqualTo("Resource not found.");
    }

    @Test
    void getAllJourneyRefunds_throwsException_whenJourneyNotaBusType() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(99L);

        User user = new User();
        user.setOfficialAgency(officialAgency);

        Journey journey = new Journey();
        SharedRide car = new SharedRide();
        journey.setCar(car);

        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(1L))
                .thenReturn(Optional.of(journey));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> refundService.getAllJourneyRefunds(1L, "123"));
        assertThat(exception.getMessage()).isEqualTo("Resource not found.");
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
        bookedJourney.setUser(user);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("APPROVED");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.refunded(2L, "123");
        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundStatus()).isEqualTo("REFUNDED");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefunderEmail()).isEqualTo("email@email.com");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefunderName()).isEqualTo("John Doe");
        assertThat(refundPaymentTransactionArgumentCaptor.getValue().getRefundedDate()).isNotNull();

    }


    @Test
    void refunded_refund_throws_Exception_whenRequestHasNotBeenApproved() {

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
        bookedJourney.setUser(user);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("PENDING");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        ApiException apiException = assertThrows(ApiException.class, () -> refundService.refunded(2L, "123"));
        assertThat(apiException.getMessage()).isEqualTo("Refund request not approved.");
        assertThat(apiException.getErrorCode()).isEqualTo("REFUND_REQUEST_NOT_APPROVED");
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    }


    @Test
    void responseRefund_changeSeatsAndSendMail_whenRequestForRefundApproved() {

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
        bookedJourney.setUser(user);

        Passenger passenger = new Passenger();
        passenger.setSeatNumber(25);
        passenger.setCheckedInCode("SEAT25");
        passenger.setBookedJourney(bookedJourney);
        Passenger passenger1 = new Passenger();
        passenger1.setSeatNumber(30);
        passenger1.setCheckedInCode("SEAT30");
        bookedJourney.addPassenger(passenger);
        bookedJourney.addPassenger(passenger1);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAgencyAmount(1000.0);
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("APPROVED");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(refundPaymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.responseRefund(2L, new ResponseRefundDTO(true, "I have approved", 1000.00), "123");

        verify(mockPassengerRepository).saveAll(passengerArgumentCaptorList.capture());
        List<Passenger> capturedPassengerList = passengerArgumentCaptorList.getValue();
        assertThat(capturedPassengerList.get(0).getSeatNumber()).isEqualTo(-1);
        assertThat(capturedPassengerList.get(0).getCheckedInCode())
                .isEqualTo(CheckInCodeGenerator.generateCode(journey, -1, "REFUNDED"));
        assertThat(capturedPassengerList.get(1).getSeatNumber()).isEqualTo(-1);
        assertThat(capturedPassengerList.get(1).getCheckedInCode())
                .isEqualTo(CheckInCodeGenerator.generateCode(journey, -1, "REFUNDED"));

        verify(mockEmailContentBuilder).buildRefundStatusEmail(
                refundPaymentTransaction,
                refundPaymentTransaction.getPaymentTransaction().getBookedJourney().getUser());

    }

    @Test
    void responseRefund_leaveSeatsAsIsAndSendMail_whenRequestForRefundDeclined() {

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
        bookedJourney.setUser(user);

        Passenger passenger = new Passenger();
        passenger.setSeatNumber(25);
        passenger.setCheckedInCode("SEAT25");
        passenger.setBookedJourney(bookedJourney);
        Passenger passenger1 = new Passenger();
        passenger1.setSeatNumber(30);
        passenger1.setCheckedInCode("SEAT30");
        bookedJourney.addPassenger(passenger);
        bookedJourney.addPassenger(passenger1);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAgencyAmount(1000.0);
        paymentTransaction.setBookedJourney(bookedJourney);

        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("PENDING");
        refundPaymentTransaction.setPaymentTransaction(paymentTransaction);

        paymentTransaction.setRefundPaymentTransaction(refundPaymentTransaction);

        when(mockRefundPaymentTransactionRepository.findById(2L))
                .thenReturn(Optional.of(refundPaymentTransaction));
        when(mockUserRepository.findById("123"))
                .thenReturn(Optional.of(user));
        refundService.responseRefund(2L, new ResponseRefundDTO(false, "I have declined", 1000.00), "123");

        verify(mockRefundPaymentTransactionRepository).save(refundPaymentTransactionArgumentCaptor.capture());
        List<Passenger> capturedPassengerList = refundPaymentTransactionArgumentCaptor.getValue()
                .getPaymentTransaction().getBookedJourney().getPassengers();
        assertThat(capturedPassengerList.get(0).getSeatNumber()).isEqualTo(passenger.getSeatNumber());
        assertThat(capturedPassengerList.get(0).getCheckedInCode()).isEqualTo(passenger.getCheckedInCode());
        assertThat(capturedPassengerList.get(1).getSeatNumber()).isEqualTo(passenger1.getSeatNumber());
        assertThat(capturedPassengerList.get(1).getCheckedInCode()).isEqualTo(passenger1.getCheckedInCode());
        verify(mockEmailContentBuilder).buildRefundStatusEmail(
                refundPaymentTransaction,
                refundPaymentTransaction.getPaymentTransaction().getBookedJourney().getUser());

    }

}
