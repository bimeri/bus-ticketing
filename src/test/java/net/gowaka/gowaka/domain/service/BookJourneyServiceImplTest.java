package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.config.PaymentUrlResponseProps;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.BookedJourneyRepository;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.PaymentTransactionRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.service.BookJourneyService;
import net.gowaka.gowaka.service.NotificationService;
import net.gowaka.gowaka.service.PayAmGoService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.gowaka.gowaka.exception.ErrorCodes.*;
import static net.gowaka.gowaka.network.api.payamgo.PayAmGoPaymentStatus.COMPLETED;
import static net.gowaka.gowaka.network.api.payamgo.PayAmGoPaymentStatus.WAITING;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:51 AM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class BookJourneyServiceImplTest {

    @Mock
    private BookedJourneyRepository mockBookedJourneyRepository;
    @Mock
    private JourneyRepository mockJourneyRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private PaymentTransactionRepository mockPaymentTransactionRepository;
    @Mock
    private UserService mockUserService;
    @Mock
    private PayAmGoService mockPayAmGoService;
    @Mock
    private NotificationService mocKNotificationService;
    @Mock
    private EmailContentBuilder mockEmailContentBuilder;

    private BookJourneyService bookJourneyService;
    private ArgumentCaptor<BookedJourney> bookedJourneyArgumentCaptor = ArgumentCaptor.forClass(BookedJourney.class);
    private ArgumentCaptor<PaymentTransaction> paymentTransactionArgumentCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Journey journey;
    @Before
    public void setUp() {

        PaymentUrlResponseProps paymentUrlResponseProps = new PaymentUrlResponseProps();
        paymentUrlResponseProps.setPayAmGoPaymentCancelUrl("http://localhost/cancel");
        paymentUrlResponseProps.setPayAmGoPaymentRedirectUrl("http://localhost/redirect");
        paymentUrlResponseProps.setPayAmGoPaymentResponseUrl("http://localhost/response");

        bookJourneyService = new BookJourneyServiceImpl(mockBookedJourneyRepository, mockJourneyRepository,
                mockUserRepository, mockPaymentTransactionRepository,
                mockUserService, mockPayAmGoService,
                mocKNotificationService, paymentUrlResponseProps,
                mockEmailContentBuilder);

        Location destinationLocation = new Location();
        destinationLocation.setAddress("Kumba Moto Part");
        destinationLocation.setCity("Kumba");
        destinationLocation.setState("SW");
        destinationLocation.setCountry("Cameroon");

        Location departureLocation = new Location();
        departureLocation.setAddress("Buea Moto Part");
        departureLocation.setCity("Buea");
        departureLocation.setState("SW");
        departureLocation.setCountry("Cameroon");

        TransitAndStop destination = new TransitAndStop();
        destination.setId(99L);
        destination.setLocation(destinationLocation);

        TransitAndStop departure = new TransitAndStop();
        departure.setId(100L);
        departure.setLocation(departureLocation);

        journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        journey.setAmount(2000.00);
        journey.setDestination(destination);
        journey.setDepartureLocation(departure);
        journey.setDepartureTime(LocalDateTime.now());

        Driver driver = new Driver();
        driver.setDriverLicenseNumber("321SW");
        driver.setDriverName("Michael John");
        journey.setDriver(driver);

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        car.setName("Musango 30 Seater Bus");
        journey.setCar(car);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("999999");
        paymentTransaction.setAppUserEmail("email@email.net");


        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setId(2L);
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        bookedJourney.setPassenger(new Passenger("John Doe", "1234001", 8));
        bookedJourney.setDestination(destination);
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setCheckedInCode("1111-1599933993");

        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

    }

    @Test
    public void bookJourney_throwsException_whenJourneyIdNotFound() {

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.NOT_FOUND)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));
        bookJourneyService.bookJourney(11L, new BookJourneyRequest());
        verify(mockBookedJourneyRepository).findById(11L);
        verifyZeroInteractions(mockUserService);
        verifyZeroInteractions(mockUserRepository);
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);
    }

    @Test
    public void bookJourney_throwsException_whenUserNotFound() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Journey()));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.UNPROCESSABLE_ENTITY)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));
        bookJourneyService.bookJourney(11L, new BookJourneyRequest());
        verify(mockBookedJourneyRepository).findById(11L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);

    }

    @Test
    public void bookJourney_throwsException_whenJourneyAlreadyTerminated() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");

        Journey journey = new Journey();
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.UNPROCESSABLE_ENTITY)));
        expectedException.expect(hasProperty("errorCode", is(JOURNEY_ALREADY_TERMINATED.toString())));
        expectedException.expect(hasProperty("message", is(JOURNEY_ALREADY_TERMINATED.getMessage())));

        bookJourneyService.bookJourney(11L, new BookJourneyRequest());
        verify(mockBookedJourneyRepository).findById(11L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);
    }

    @Test
    public void bookJourney_throwsException_whenSeatAlreadyTaken() {

        Journey journey = new Journey();
        journey.setArrivalIndicator(true);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassenger(new Passenger("John", "12345", 3));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));
        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));


        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.UNPROCESSABLE_ENTITY)));
        expectedException.expect(hasProperty("errorCode", is(SEAT_ALREADY_TAKEN.toString())));
        expectedException.expect(hasProperty("message", is(SEAT_ALREADY_TAKEN.getMessage())));

        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setSeatNumber(3);
        bookJourneyService.bookJourney(11L, bookJourneyRequest);
        verify(mockBookedJourneyRepository).findById(11L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);
    }

    @Test
    public void bookJourney_throwsException_whenJourneyAlreadyStarted() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");

        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(true);
        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.UNPROCESSABLE_ENTITY)));
        expectedException.expect(hasProperty("errorCode", is(JOURNEY_ALREADY_STARTED.toString())));
        expectedException.expect(hasProperty("message", is(JOURNEY_ALREADY_STARTED.getMessage())));

        bookJourneyService.bookJourney(11L, new BookJourneyRequest());
        verify(mockBookedJourneyRepository).findById(11L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);
    }

    @Test
    public void bookJourney_throwsException_whenRequestDestinationIndicatorIsFalse_andTransitAndStopNotFound() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(false);

        TransitAndStop destination = new TransitAndStop();
        destination.setId(99L);
        destination.setLocation(new Location());

        Journey journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        journey.setAmount(5000.00);
        journey.setDestination(destination);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.UNPROCESSABLE_ENTITY)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));

        bookJourneyService.bookJourney(11L, bookJourneyRequest);
        verify(mockBookedJourneyRepository).findById(11L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyZeroInteractions(mockPaymentTransactionRepository);
        verifyZeroInteractions(mockPayAmGoService);
    }

    @Test
    public void bookJourney_initiatePayment_whenRequestDestinationIndicatorIsTrue() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(true);
        bookJourneyRequest.setEmail("email@email.com");
        bookJourneyRequest.setPassengerName("Jesus Christ");
        bookJourneyRequest.setPassengerIdNumber("1234567890");
        bookJourneyRequest.setPhoneNumber("676767676");
        bookJourneyRequest.setSeatNumber(9);
        bookJourneyRequest.setTransitAndStopId(33L);

        TransitAndStop destination = new TransitAndStop();
        destination.setId(99L);
        destination.setLocation(new Location());

        Journey journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        journey.setAmount(5000.00);
        journey.setDestination(destination);

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        journey.setCar(car);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setId(101L);
        when(mockBookedJourneyRepository.save(any()))
                .thenReturn(bookedJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAmount(10.0);
        when(mockPaymentTransactionRepository.save(any()))
                .thenReturn(paymentTransaction);

        PayAmGoRequestResponseDTO paymentResponse = new PayAmGoRequestResponseDTO();
        paymentResponse.setPaymentUrl("https://payamgo.com/paymentlink");
        paymentResponse.setProcessingNumber("process002");
        paymentResponse.setAppTransactionNumber("appTxn001");
        when(mockPayAmGoService.initiatePayment(any(PayAmGoRequestDTO.class)))
                .thenReturn(paymentResponse);

        PaymentUrlDTO paymentUrlDTO = bookJourneyService.bookJourney(11L, bookJourneyRequest);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verify(mockBookedJourneyRepository).save(bookedJourneyArgumentCaptor.capture());
        verify(mockPaymentTransactionRepository, times(2)).save(paymentTransactionArgumentCaptor.capture());
        verify(mockPayAmGoService).initiatePayment(any());

        BookedJourney bookedJourneyValue = bookedJourneyArgumentCaptor.getValue();
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getAllValues().get(0);
        PaymentTransaction paymentTransactionAfterResponseValue = paymentTransactionArgumentCaptor.getAllValues().get(1);

        assertThat(paymentUrlDTO.getPaymentUrl()).isEqualTo("https://payamgo.com/paymentlink");
        assertThat(bookedJourneyValue.getAmount()).isEqualTo(5000.0);
        assertThat(bookedJourneyValue.getCheckedInCode()).startsWith("10119-");
        assertThat(bookedJourneyValue.getPassengerCheckedInIndicator()).isEqualTo(false);
        assertThat(bookedJourneyValue.getPassenger()).isEqualTo(new Passenger("Jesus Christ", "1234567890", 9));
        assertThat(bookedJourneyValue.getDestination().getId()).isEqualTo(99L);

        assertThat(paymentTransactionValue.getAmount()).isEqualTo(5000.0);
        assertThat(paymentTransactionValue.getAppTransactionNumber()).isNotEmpty();
        assertThat(paymentTransactionValue.getAppUserEmail()).isEqualTo("email@email.com");
        assertThat(paymentTransactionValue.getAppUserFirstName()).isEqualTo("Jesus");
        assertThat(paymentTransactionValue.getAppUserLastName()).isEqualTo("Christ");
        assertThat(paymentTransactionValue.getAppUserPhoneNumber()).isEqualTo("676767676");
        assertThat(paymentTransactionValue.getCancelRedirectUrl()).isEqualTo("http://localhost/cancel");
        assertThat(paymentTransactionValue.getCurrencyCode()).isEqualTo("XAF");
        assertThat(paymentTransactionValue.getLanguage()).isEqualTo("en");
        assertThat(paymentTransactionValue.getPaymentReason()).isEqualTo("Bus ticket for 123SW");
        assertThat(paymentTransactionValue.getPaymentResponseUrl()).isEqualTo("http://localhost/response/101");
        assertThat(paymentTransactionValue.getReturnRedirectUrl()).isEqualTo("http://localhost/redirect/101");
        assertThat(paymentTransactionValue.getTransactionStatus()).isEqualTo("INITIATED");

        assertThat(paymentTransactionAfterResponseValue.getTransactionStatus()).isEqualTo("WAITING");
        assertThat(paymentTransactionAfterResponseValue.getProcessingNumber()).isEqualTo("process002");

    }

    @Test
    public void bookJourney_initiatePayment_whenRequestDestinationIndicatorIsFalse() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(false);
        bookJourneyRequest.setEmail("email@email.com");
        bookJourneyRequest.setPassengerName("Jesus Christ");
        bookJourneyRequest.setPassengerIdNumber("1234567890");
        bookJourneyRequest.setPhoneNumber("676767676");
        bookJourneyRequest.setSeatNumber(9);
        bookJourneyRequest.setTransitAndStopId(100L);

        TransitAndStop destination = new TransitAndStop();
        destination.setId(99L);
        destination.setLocation(new Location());

        TransitAndStop stopTransitAndStop = new TransitAndStop();
        stopTransitAndStop.setId(100L);
        stopTransitAndStop.setLocation(new Location());

        Journey journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        journey.setAmount(5000.00);
        journey.setDestination(destination);

        JourneyStop stopLocation = new JourneyStop();
        stopLocation.setTransitAndStop(stopTransitAndStop);
        stopLocation.setAmount(2000.00);
        journey.setJourneyStops(Collections.singletonList(stopLocation));

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        journey.setCar(car);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setId(101L);
        when(mockBookedJourneyRepository.save(any()))
                .thenReturn(bookedJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setAmount(10.0);
        when(mockPaymentTransactionRepository.save(any()))
                .thenReturn(paymentTransaction);

        PayAmGoRequestResponseDTO paymentResponse = new PayAmGoRequestResponseDTO();
        paymentResponse.setPaymentUrl("https://payamgo.com/paymentlink");
        paymentResponse.setProcessingNumber("process002");
        paymentResponse.setAppTransactionNumber("appTxn001");
        when(mockPayAmGoService.initiatePayment(any(PayAmGoRequestDTO.class)))
                .thenReturn(paymentResponse);

        PaymentUrlDTO paymentUrlDTO = bookJourneyService.bookJourney(11L, bookJourneyRequest);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verify(mockBookedJourneyRepository).save(bookedJourneyArgumentCaptor.capture());
        verify(mockPaymentTransactionRepository, times(2)).save(paymentTransactionArgumentCaptor.capture());
        verify(mockPayAmGoService).initiatePayment(any());

        BookedJourney bookedJourneyValue = bookedJourneyArgumentCaptor.getValue();
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getAllValues().get(0);
        PaymentTransaction paymentTransactionAfterResponseValue = paymentTransactionArgumentCaptor.getAllValues().get(1);

        assertThat(paymentUrlDTO.getPaymentUrl()).isEqualTo("https://payamgo.com/paymentlink");
        assertThat(bookedJourneyValue.getAmount()).isEqualTo(2000.0);
        assertThat(bookedJourneyValue.getCheckedInCode()).startsWith("10119-");
        assertThat(bookedJourneyValue.getPassengerCheckedInIndicator()).isEqualTo(false);
        assertThat(bookedJourneyValue.getPassenger()).isEqualTo(new Passenger("Jesus Christ", "1234567890", 9));
        assertThat(bookedJourneyValue.getDestination().getId()).isEqualTo(100L);

        assertThat(paymentTransactionValue.getAmount()).isEqualTo(2000.0);
        assertThat(paymentTransactionValue.getAppTransactionNumber()).isNotEmpty();
        assertThat(paymentTransactionValue.getAppUserEmail()).isEqualTo("email@email.com");
        assertThat(paymentTransactionValue.getAppUserFirstName()).isEqualTo("Jesus");
        assertThat(paymentTransactionValue.getAppUserLastName()).isEqualTo("Christ");
        assertThat(paymentTransactionValue.getAppUserPhoneNumber()).isEqualTo("676767676");
        assertThat(paymentTransactionValue.getCancelRedirectUrl()).isEqualTo("http://localhost/cancel");
        assertThat(paymentTransactionValue.getCurrencyCode()).isEqualTo("XAF");
        assertThat(paymentTransactionValue.getLanguage()).isEqualTo("en");
        assertThat(paymentTransactionValue.getPaymentReason()).isEqualTo("Bus ticket for 123SW");
        assertThat(paymentTransactionValue.getPaymentResponseUrl()).isEqualTo("http://localhost/response/101");
        assertThat(paymentTransactionValue.getReturnRedirectUrl()).isEqualTo("http://localhost/redirect/101");

        assertThat(paymentTransactionAfterResponseValue.getTransactionStatus()).isEqualTo("WAITING");
        assertThat(paymentTransactionAfterResponseValue.getProcessingNumber()).isEqualTo("process002");

    }

    @Test
    public void getAllBookedSeats_throwsException_whenJourneyIdNotFound() {

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.NOT_FOUND)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));
        bookJourneyService.getAllBookedSeats(11L);

    }

    @Test
    public void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsCOMPLETED() {

        Journey journey = new Journey();
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassenger(new Passenger("John", "123423", 10));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        assertThat(allBookedSeats.get(0)).isEqualTo(10);

    }

    @Test
    public void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsWAITING_AndPaymentWaitTimeLessThanLimit() {

        Journey journey = new Journey();
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassenger(new Passenger("John", "123423", 10));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setCreateAt(LocalDateTime.now().minusMinutes(5));

        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        assertThat(allBookedSeats.get(0)).isEqualTo(10);

    }

    @Test
    public void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsWAITING_AndPaymentWaitTimeGreaterThanLimit() {

        Journey journey = new Journey();
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setPassenger(new Passenger("John", "123423", 10));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setCreateAt(LocalDateTime.now().minusMinutes(11));

        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        assertThat(allBookedSeats.size()).isEqualTo(0);

    }

    @Test
    public void getBookJourneyStatus_throwException_whenBookJourneyIdDontExit() {


        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.NOT_FOUND)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));

        bookJourneyService.getBookJourneyStatus(2L);

    }

    @Test
    public void getBookJourneyStatus_returnStatusResponse_whenBookJourneyIdExit() {

        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.of(bookJourney));
        BookedJourneyStatusDTO bookJourneyStatus = bookJourneyService.getBookJourneyStatus(2L);
        assertThat(bookJourneyStatus.getAmount()).isEqualTo(2000.00);
        assertThat(bookJourneyStatus.getCurrencyCode()).isEqualTo("XAF");
        assertThat(bookJourneyStatus.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(bookJourneyStatus.getCheckedInCode()).isEqualTo("1111-1599933993");
        assertThat(bookJourneyStatus.getQRCheckedInImage()).isEqualTo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA10lEQVR42u3XSw7EIAgGYFx5DG/q46Yeg1UdATvJTOzavwnGRdtvQ0TQ0nga5OLi8g5hmiOMUSlVeUxIIo+NKXBq9yuOzHibRm0OJ0yFUWV0TNFspyuO/T44KVolEvK+fk7KGr089Z2DIlFLwq/Ys0wkkT1IRSvY0g4l2ZLMNEOuhCVB0WZgJJnFESfqWrIca0BiQz+Xb62AyN1dqqT6d0XPixaHxCszE5boSSvdhWbgceCJ3gL+so0iSZe2BzCxZrxwcz84KKtKrLW0TQWfE/+fc3F5qXwAkHCU9h+9LrYAAAAASUVORK5CYII=");
        assertThat(bookJourneyStatus.getPaymentReason()).isEqualTo("Bus ticket");
        assertThat(bookJourneyStatus.getPaymentChannel()).isEqualTo("MTN_MOBILE_MONEY");
        assertThat(bookJourneyStatus.getPaymentDate()).isNotNull();
        assertThat(bookJourneyStatus.isCheckedIn()).isFalse();
        assertThat(bookJourneyStatus.getPassengerName()).isEqualTo("John Doe");
        assertThat(bookJourneyStatus.getPassengerIdNumber()).isEqualTo("1234001");
        assertThat(bookJourneyStatus.getPassengerSeatNumber()).isEqualTo(8);
        assertThat(bookJourneyStatus.getCarName()).isEqualTo("Musango 30 Seater Bus");
        assertThat(bookJourneyStatus.getCarDriverName()).isEqualTo("Michael John");
        assertThat(bookJourneyStatus.getCarLicenseNumber()).isEqualTo("123SW");
        assertThat(bookJourneyStatus.getDepartureLocation()).isEqualTo("Buea Moto Part, Buea SW, Cameroon");
        assertThat(bookJourneyStatus.getDestinationLocation()).isEqualTo("Kumba Moto Part, Kumba SW, Cameroon");
        assertThat(bookJourneyStatus.getId()).isEqualTo(2L);
        assertThat(bookJourneyStatus.getDepartureTime()).isNotNull();
        assertThat(bookJourneyStatus.getPassengerEmail()).isEqualTo("email@email.net");
        assertThat(bookJourneyStatus.getPassengerPhoneNumber()).isEqualTo("999999");

    }

    @Test
    public void handlePaymentResponse_throwException_whenBookJourneyIdDontExit() {

        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("httpStatus", is(HttpStatus.NOT_FOUND)));
        expectedException.expect(hasProperty("errorCode", is(RESOURCE_NOT_FOUND.toString())));
        expectedException.expect(hasProperty("message", is(RESOURCE_NOT_FOUND.getMessage())));

        bookJourneyService.handlePaymentResponse(2L, new PaymentStatusResponseDTO());

    }

    @Test
    public void handlePaymentResponse_UpdateTransactionStatus_whenBookJourneyIdExitAndTransactionValid() {

        PaymentStatusResponseDTO paymentStatusResponseDTO = new PaymentStatusResponseDTO();
        paymentStatusResponseDTO.setAppTransactionNumber("AppTxnNumber");
        paymentStatusResponseDTO.setPaymentChannelCode("MTN_MOBILE_MONEY");
        paymentStatusResponseDTO.setPaymentChannelTransactionNumber("PcTxnNumber");
        paymentStatusResponseDTO.setTransactionStatus("COMPLETED");
        paymentStatusResponseDTO.setProcessingNumber("processingNumber001");

        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
        bookedJourney.setPassenger(new Passenger("John", "123423", 10));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setProcessingNumber("processingNumber001");
        paymentTransaction.setAppTransactionNumber("AppTxnNumber");

        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.of(bookedJourney));

        bookJourneyService.handlePaymentResponse(2L, paymentStatusResponseDTO);
        verify(mockPaymentTransactionRepository).save(paymentTransactionArgumentCaptor.capture());
        verify(mockEmailContentBuilder).buildTicketEmail(any());
        verify(mocKNotificationService).sendEmail(any());
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getValue();

        assertThat(paymentTransactionValue.getTransactionStatus()).isEqualTo("COMPLETED");
        assertThat(paymentTransactionValue.getPaymentChannelTransactionNumber()).isEqualTo("PcTxnNumber");
        assertThat(paymentTransactionValue.getPaymentChannel()).isEqualTo("MTN_MOBILE_MONEY");

    }

}
