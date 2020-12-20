package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.config.PaymentUrlResponseProps;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.service.BookJourneyService;
import net.gogroups.gowaka.service.JourneyService;
import net.gogroups.gowaka.service.ServiceChargeService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.payamgo.model.PayAmGoRequestDTO;
import net.gogroups.payamgo.model.PayAmGoRequestResponseDTO;
import net.gogroups.payamgo.service.PayAmGoService;
import net.gogroups.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.*;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:51 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
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
    private PassengerRepository mockPassengerRepository;
    @Mock
    private UserService mockUserService;
    @Mock
    private PayAmGoService mockPayAmGoService;
    @Mock
    private NotificationService mocKNotificationService;
    @Mock
    private FileStorageService mockFileStorageService;
    @Mock
    private EmailContentBuilder mockEmailContentBuilder;
    @Mock
    private JourneyService mockJourneyService;
    @Mock
    private ServiceChargeService mockServiceChargeService;

    private BookJourneyService bookJourneyService;
    private ArgumentCaptor<BookedJourney> bookedJourneyArgumentCaptor = ArgumentCaptor.forClass(BookedJourney.class);
    private ArgumentCaptor<PaymentTransaction> paymentTransactionArgumentCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
    private ArgumentCaptor<Passenger> passengerArgumentCaptor = ArgumentCaptor.forClass(Passenger.class);

    private Journey journey;

    @BeforeEach
    void setUp() {

        PaymentUrlResponseProps paymentUrlResponseProps = new PaymentUrlResponseProps();
        paymentUrlResponseProps.setPayAmGoPaymentCancelUrl("http://localhost/cancel");
        paymentUrlResponseProps.setPayAmGoPaymentRedirectUrl("http://localhost/redirect");
        paymentUrlResponseProps.setPayAmGoPaymentResponseUrl("http://localhost/response");

        bookJourneyService = new BookJourneyServiceImpl(mockBookedJourneyRepository, mockJourneyRepository,
                mockUserRepository, mockPaymentTransactionRepository, mockPassengerRepository,
                mockUserService, mockPayAmGoService,
                mocKNotificationService, mockFileStorageService, paymentUrlResponseProps,
                mockJourneyService, mockServiceChargeService, mockEmailContentBuilder);

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
        car.setIsOfficialAgencyIndicator(true);
        car.setOfficialAgency(new OfficialAgency());
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
        paymentTransaction.setCreatedAt(LocalDateTime.now());


        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setId(2L);
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);

        Passenger passenger = new Passenger();
        passenger.setName("John Doe");
        passenger.setSeatNumber(8);
        passenger.setPhoneNumber("67676767");
        passenger.setIdNumber("1234001");
        passenger.setEmail("email@email.net");
        passenger.setCheckedInCode("1111-1599933993");
        passenger.setPassengerCheckedInIndicator(false);

        bookedJourney.getPassengers().add(passenger);
        bookedJourney.setDestination(destination);
        User user = new User();
        user.setUserId("10");
        bookedJourney.setUser(user);

        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

    }

    @Test
    void bookJourney_throwsException_whenJourneyIdNotFound() {

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, new BookJourneyRequest()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

        verifyNoInteractions(mockUserService);
        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void bookJourney_throwsException_whenUserNotFound() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Journey()));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, new BookJourneyRequest()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);

    }

    @Test
    void bookJourney_throwsException_whenJourneyAlreadyTerminated() {

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

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, new BookJourneyRequest()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_TERMINATED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_TERMINATED.getMessage());

        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void bookJourney_throwsException_whenSeatAlreadyTaken() {
        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");

        Journey journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        Passenger passenger = new Passenger();
        passenger.setSeatNumber(3);
        when(mockPassengerRepository.findByBookedJourney_Journey_Id(11L))
                .thenReturn(Collections.singletonList(passenger));

        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        BookJourneyRequest.Passenger passengerDTO = new BookJourneyRequest.Passenger();
        passengerDTO.setSeatNumber(3);
        bookJourneyRequest.getPassengers().add(passengerDTO);

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, bookJourneyRequest));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("SEAT_ALREADY_TAKEN");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.SEAT_ALREADY_TAKEN.getMessage());

        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void bookJourney_throwsException_whenJourneyAlreadyStarted() {

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

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, new BookJourneyRequest()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_STARTED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_STARTED.getMessage());

        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void bookJourney_throwsException_whenRequestDestinationIndicatorIsFalse_andTransitAndStopNotFound() {

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

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.bookJourney(11L, bookJourneyRequest));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void bookJourney_initiatePayment_whenRequestDestinationIndicatorIsTrue() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        userDto.setFullName("John Doe");
        userDto.setEmail("email@email.com");
        userDto.setPhoneNumber("676767676");
        User user = new User();
        user.setUserId("10");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(true);
        bookJourneyRequest.setTransitAndStopId(33L);

        BookJourneyRequest.Passenger passenger = new BookJourneyRequest.Passenger();
        passenger.setSeatNumber(9);
        passenger.setEmail("email@email.com");
        passenger.setPassengerName("Jesus Christ");
        passenger.setPassengerIdNumber("1234567890");
        passenger.setPhoneNumber("676767676");

        bookJourneyRequest.getPassengers().add(passenger);

        TransitAndStop destination = new TransitAndStop();
        destination.setId(99L);
        destination.setLocation(new Location());

        Journey journey = new Journey();
        journey.setId(11L);
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(false);
        journey.setAmount(5000.00);
        journey.setDestination(destination);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setCode("VT");

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        car.setIsOfficialAgencyIndicator(true);
        car.setOfficialAgency(officialAgency);
        journey.setCar(car);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));
        when(mockServiceChargeService.getServiceCharges())
                .thenReturn(Collections.singletonList(new ServiceChargeDTO("sc-id", 10.0)));
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
        verify(mockUserService, times(2)).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verify(mockBookedJourneyRepository).save(bookedJourneyArgumentCaptor.capture());
        verify(mockPaymentTransactionRepository, times(2)).save(paymentTransactionArgumentCaptor.capture());
        verify(mockPayAmGoService).initiatePayment(any());

        BookedJourney bookedJourneyValue = bookedJourneyArgumentCaptor.getValue();
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getAllValues().get(0);
        PaymentTransaction paymentTransactionAfterResponseValue = paymentTransactionArgumentCaptor.getAllValues().get(1);

        assertThat(paymentUrlDTO.getPaymentUrl()).isEqualTo("https://payamgo.com/paymentlink");
        assertThat(bookedJourneyValue.getAmount()).isEqualTo(5000.0);

        assertThat(bookedJourneyValue.getPassengers().get(0).getEmail()).isEqualTo("email@email.com");
        assertThat(bookedJourneyValue.getPassengers().get(0).getName()).isEqualTo("Jesus Christ");
        assertThat(bookedJourneyValue.getPassengers().get(0).getPhoneNumber()).isEqualTo("676767676");
        assertThat(bookedJourneyValue.getPassengers().get(0).getCheckedInCode()).isEqualTo("VT11-123SW-9-10");
        assertThat(bookedJourneyValue.getPassengers().get(0).getPassengerCheckedInIndicator()).isEqualTo(false);

        assertThat(bookedJourneyValue.getDestination().getId()).isEqualTo(99L);
        assertThat(paymentTransactionValue.getAmount()).isEqualTo(5500.0);
        assertThat(paymentTransactionValue.getAppTransactionNumber()).isNotEmpty();
        assertThat(paymentTransactionValue.getAppUserEmail()).isEqualTo("email@email.com");
        assertThat(paymentTransactionValue.getAppUserFirstName()).isEqualTo("John");
        assertThat(paymentTransactionValue.getAppUserLastName()).isEqualTo("Doe");
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
    void bookJourney_initiatePayment_whenRequestDestinationIndicatorIsFalse() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(false);

        BookJourneyRequest.Passenger passenger = new BookJourneyRequest.Passenger();
        passenger.setSeatNumber(9);
        passenger.setEmail("email@email.com");
        passenger.setPassengerName("Jesus Christ");
        passenger.setPassengerIdNumber("1234567890");
        passenger.setPhoneNumber("676767676");
        bookJourneyRequest.getPassengers().add(passenger);

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

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setCode("VT");
        Bus car = new Bus();
        car.setIsOfficialAgencyIndicator(true);
        car.setLicensePlateNumber("123SW");
        car.setOfficialAgency(officialAgency);
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
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("John Doe");
        userDTO.setEmail("email@email.com");
        userDTO.setPhoneNumber("676767676");
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

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
        verify(mockUserService, times(2)).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verify(mockBookedJourneyRepository).save(bookedJourneyArgumentCaptor.capture());
        verify(mockPaymentTransactionRepository, times(2)).save(paymentTransactionArgumentCaptor.capture());
        verify(mockPayAmGoService).initiatePayment(any());

        BookedJourney bookedJourneyValue = bookedJourneyArgumentCaptor.getValue();
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getAllValues().get(0);
        PaymentTransaction paymentTransactionAfterResponseValue = paymentTransactionArgumentCaptor.getAllValues().get(1);

        assertThat(paymentUrlDTO.getPaymentUrl()).isEqualTo("https://payamgo.com/paymentlink");
        assertThat(bookedJourneyValue.getAmount()).isEqualTo(2000.0);
        assertThat(bookedJourneyValue.getPassengers().get(0).getCheckedInCode()).isEqualTo("VT11-123SW-9-10");
        assertThat(bookedJourneyValue.getPassengers().get(0).getPassengerCheckedInIndicator()).isEqualTo(false);

        assertThat(bookedJourneyValue.getDestination().getId()).isEqualTo(100L);

        assertThat(paymentTransactionValue.getAmount()).isEqualTo(2000.0);
        assertThat(paymentTransactionValue.getAppTransactionNumber()).isNotEmpty();
        assertThat(paymentTransactionValue.getAppUserEmail()).isEqualTo("email@email.com");
        assertThat(paymentTransactionValue.getAppUserFirstName()).isEqualTo("John");
        assertThat(paymentTransactionValue.getAppUserLastName()).isEqualTo("Doe");
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
    void agencyUserBookJourney_makeManualPayment_whenRequestDestinationIndicatorIsFalse() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(111L);
        officialAgency.setCode("VT");
        user.setOfficialAgency(officialAgency);
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setDestinationIndicator(false);

        BookJourneyRequest.Passenger passenger = new BookJourneyRequest.Passenger();
        passenger.setSeatNumber(9);
        passenger.setEmail("email@email.com");
        passenger.setPassengerName("Jesus Christ");
        passenger.setPassengerIdNumber("1234567890");
        passenger.setPhoneNumber("676767676");
        bookJourneyRequest.getPassengers().add(passenger);

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
        journey.setDepartureLocation(destination);
        journey.setDriver(new Driver());

        JourneyStop stopLocation = new JourneyStop();
        stopLocation.setTransitAndStop(stopTransitAndStop);
        stopLocation.setAmount(2000.00);
        journey.setJourneyStops(Collections.singletonList(stopLocation));

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        car.setOfficialAgency(officialAgency);
        car.setIsOfficialAgencyIndicator(true);
        journey.setCar(car);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setDestination(destination);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setBookedJourney(bookedJourney);

        bookedJourney.setPaymentTransaction(paymentTransaction);
        bookedJourney.setJourney(journey);


        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));
        bookedJourney.setId(101L);
        when(mockBookedJourneyRepository.save(any()))
                .thenReturn(bookedJourney);
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("John Doe");
        userDTO.setEmail("email@email.com");
        userDTO.setPhoneNumber("676767676");
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        paymentTransaction.setAmount(10.0);
        when(mockPaymentTransactionRepository.save(any()))
                .thenReturn(paymentTransaction);

        bookJourneyService.agencyUserBookJourney(11L, bookJourneyRequest);

        verify(mockUserService, times(2)).getCurrentAuthUser();
        verify(mockUserRepository).findById("10");
        verify(mockBookedJourneyRepository).save(bookedJourneyArgumentCaptor.capture());
        verify(mockPaymentTransactionRepository).save(paymentTransactionArgumentCaptor.capture());

        BookedJourney bookedJourneyValue = bookedJourneyArgumentCaptor.getValue();
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getValue();

        assertThat(bookedJourneyValue.getAmount()).isEqualTo(2000.0);
        assertThat(bookedJourneyValue.getPassengers().get(0).getCheckedInCode()).isEqualTo("VT11-123SW-9-10");
        assertThat(bookedJourneyValue.getPassengers().get(0).getPassengerCheckedInIndicator()).isEqualTo(false);

        assertThat(bookedJourneyValue.getDestination().getId()).isEqualTo(100L);

        assertThat(paymentTransactionValue.getAmount()).isEqualTo(2000.0);
        assertThat(paymentTransactionValue.getAppTransactionNumber()).isNotEmpty();
        assertThat(paymentTransactionValue.getAppUserEmail()).isEqualTo("email@email.com");
        assertThat(paymentTransactionValue.getAppUserFirstName()).isEqualTo("John");
        assertThat(paymentTransactionValue.getAppUserLastName()).isEqualTo("Doe");
        assertThat(paymentTransactionValue.getAppUserPhoneNumber()).isEqualTo("676767676");
        assertThat(paymentTransactionValue.getCancelRedirectUrl()).isEqualTo("http://localhost/cancel");
        assertThat(paymentTransactionValue.getCurrencyCode()).isEqualTo("XAF");
        assertThat(paymentTransactionValue.getLanguage()).isEqualTo("en");
        assertThat(paymentTransactionValue.getPaymentReason()).isEqualTo("Bus ticket for 123SW");
        assertThat(paymentTransactionValue.getPaymentResponseUrl()).isEqualTo("http://localhost/response/101");
        assertThat(paymentTransactionValue.getReturnRedirectUrl()).isEqualTo("http://localhost/redirect/101");

        assertThat(paymentTransactionValue.getTransactionStatus()).isEqualTo("COMPLETED");
        assertThat(paymentTransactionValue.getPaymentChannel()).isEqualTo("CASHIER");
        assertThat(paymentTransactionValue.getProcessingNumber()).isEqualTo(null);

    }

    @Test
    void agencyUserBookJourney_throwsException_whenUserNotInAgency() {

        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        User user = new User();
        user.setUserId("10");

        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        journey.setDepartureIndicator(true);
        journey.setCar(new Bus());
        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.agencyUserBookJourney(11L, new BookJourneyRequest()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.USER_NOT_IN_AGENCY.getMessage());

        verify(mockUserRepository).findById("10");
        verifyNoInteractions(mockPaymentTransactionRepository);
        verifyNoInteractions(mockPayAmGoService);
    }

    @Test
    void getAllBookedSeats_throwsException_whenJourneyIdNotFound() {

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getAllBookedSeats(11L));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

    }

    @Test
    void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsCOMPLETED() {

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        assertThat(allBookedSeats.get(0)).isEqualTo(8);

    }

    @Test
    void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsWAITING_AndPaymentWaitTimeLessThanLimit() {

        Journey journey = new Journey();
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.getPassengers().add(new Passenger("John", "123423", 10, "email@example.com", "123423", "1101-0001", false));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        assertThat(allBookedSeats.get(0)).isEqualTo(10);

    }

    @Test
    void getAllBookedSeats_returnAListOfBookedSeatNumber_whenJourneyIdExist_AndPaymentIsWAITING_AndPaymentWaitTimeGreaterThanLimit() {

        Journey journey = new Journey();
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.getPassengers().add(new Passenger("John", "123423", 10, "email@example.com", "123423", "1101-0001", false));
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setCreatedAt(LocalDateTime.now().minusMinutes(6));

        bookedJourney.setPaymentTransaction(paymentTransaction);

        when(mockJourneyRepository.findById(anyLong()))
                .thenReturn(Optional.of(journey));

        List<Integer> allBookedSeats = bookJourneyService.getAllBookedSeats(11L);
        verify(mockJourneyRepository).findById(11L);
        mockPassengerRepository.saveAll(any(List.class));
        assertThat(allBookedSeats.size()).isEqualTo(0);

    }

    @Test
    void getBookJourneyStatus_throwException_whenBookJourneyIdDontExit() {


        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getBookJourneyStatus(2L));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

    }

    @Test
    void getUserBookedJourneyHistory_throwException_whenUserNotLogin() {
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(null);

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getUserBookedJourneyHistory());
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("Resource Not Found");

    }

    @Test
    void getUserBookedJourneyHistory_returnListOfStatus_whenUserLogin() {
        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        when(mockBookedJourneyRepository.findAllByUserUserId(anyString()))
                .thenReturn(Collections.singletonList(journey.getBookedJourneys().get(0)));

        BookedJourneyStatusDTO userBookedJourneyHistory = bookJourneyService.getUserBookedJourneyHistory().get(0);
        assertThat(userBookedJourneyHistory.getAmount()).isEqualTo(2000.00);
        assertThat(userBookedJourneyHistory.getCurrencyCode()).isEqualTo("XAF");
        assertThat(userBookedJourneyHistory.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getQRCheckedInImage()).isEqualTo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA10lEQVR42u3XSw7EIAgGYFx5DG/q46Yeg1UdATvJTOzavwnGRdtvQ0TQ0nga5OLi8g5hmiOMUSlVeUxIIo+NKXBq9yuOzHibRm0OJ0yFUWV0TNFspyuO/T44KVolEvK+fk7KGr089Z2DIlFLwq/Ys0wkkT1IRSvY0g4l2ZLMNEOuhCVB0WZgJJnFESfqWrIca0BiQz+Xb62AyN1dqqT6d0XPixaHxCszE5boSSvdhWbgceCJ3gL+so0iSZe2BzCxZrxwcz84KKtKrLW0TQWfE/+fc3F5qXwAkHCU9h+9LrYAAAAASUVORK5CYII=");
        assertThat(userBookedJourneyHistory.getPaymentReason()).isEqualTo("Bus ticket");
        assertThat(userBookedJourneyHistory.getPaymentChannel()).isEqualTo("MTN_MOBILE_MONEY");
        assertThat(userBookedJourneyHistory.getPaymentDate()).isNotNull();
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getPassengerName()).isEqualTo("John Doe");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getPassengerIdNumber()).isEqualTo("1234001");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getPassengerSeatNumber()).isEqualTo(8);
        assertThat(userBookedJourneyHistory.getCarName()).isEqualTo("Musango 30 Seater Bus");
        assertThat(userBookedJourneyHistory.getCarDriverName()).isEqualTo("Michael John");
        assertThat(userBookedJourneyHistory.getCarLicenseNumber()).isEqualTo("123SW");
        assertThat(userBookedJourneyHistory.getDepartureLocation()).isEqualTo("Buea Moto Part, Buea, SW, Cameroon");
        assertThat(userBookedJourneyHistory.getDestinationLocation()).isEqualTo("Kumba Moto Part, Kumba, SW, Cameroon");
        assertThat(userBookedJourneyHistory.getId()).isEqualTo(2L);
        assertThat(userBookedJourneyHistory.getDepartureTime()).isNotNull();
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getPassengerEmail()).isEqualTo("email@email.net");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getPassengerPhoneNumber()).isEqualTo("67676767");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).getCheckedInCode()).isEqualTo("1111-1599933993");
        assertThat(userBookedJourneyHistory.getPassengers().get(0).isCheckedIn()).isFalse();

    }

    @Test
    void getUserBookedJourneyHistory_returnListOfStatus_whenUserLoginAndNoCompletedTransaction() {
        UserDTO userDto = new UserDTO();
        userDto.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
        bookedJourney.getPaymentTransaction().setTransactionStatus("DECLINED");
        when(mockBookedJourneyRepository.findAllByUserUserId(anyString()))
                .thenReturn(Collections.singletonList(bookedJourney));

        List<BookedJourneyStatusDTO> userBookedJourneyHistory = bookJourneyService.getUserBookedJourneyHistory();
        assertThat(userBookedJourneyHistory.size()).isEqualTo(0);
    }

    @Test
    void getBookJourneyStatus_returnStatusResponse_whenBookJourneyIdExit() {

        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.of(bookJourney));
        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        BookedJourneyStatusDTO bookJourneyStatus = bookJourneyService.getBookJourneyStatus(2L);
        assertThat(bookJourneyStatus.getAmount()).isEqualTo(2000.00);
        assertThat(bookJourneyStatus.getCurrencyCode()).isEqualTo("XAF");
        assertThat(bookJourneyStatus.getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(bookJourneyStatus.getPassengers().get(0).getQRCheckedInImage()).isEqualTo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA10lEQVR42u3XSw7EIAgGYFx5DG/q46Yeg1UdATvJTOzavwnGRdtvQ0TQ0nga5OLi8g5hmiOMUSlVeUxIIo+NKXBq9yuOzHibRm0OJ0yFUWV0TNFspyuO/T44KVolEvK+fk7KGr089Z2DIlFLwq/Ys0wkkT1IRSvY0g4l2ZLMNEOuhCVB0WZgJJnFESfqWrIca0BiQz+Xb62AyN1dqqT6d0XPixaHxCszE5boSSvdhWbgceCJ3gL+so0iSZe2BzCxZrxwcz84KKtKrLW0TQWfE/+fc3F5qXwAkHCU9h+9LrYAAAAASUVORK5CYII=");
        assertThat(bookJourneyStatus.getPaymentReason()).isEqualTo("Bus ticket");
        assertThat(bookJourneyStatus.getPaymentChannel()).isEqualTo("MTN_MOBILE_MONEY");
        assertThat(bookJourneyStatus.getPaymentDate()).isNotNull();
        assertThat(bookJourneyStatus.getPassengers().get(0).getPassengerName()).isEqualTo("John Doe");
        assertThat(bookJourneyStatus.getPassengers().get(0).getPassengerIdNumber()).isEqualTo("1234001");
        assertThat(bookJourneyStatus.getPassengers().get(0).getPassengerSeatNumber()).isEqualTo(8);
        assertThat(bookJourneyStatus.getCarName()).isEqualTo("Musango 30 Seater Bus");
        assertThat(bookJourneyStatus.getCarDriverName()).isEqualTo("Michael John");
        assertThat(bookJourneyStatus.getCarLicenseNumber()).isEqualTo("123SW");
        assertThat(bookJourneyStatus.getDepartureLocation()).isEqualTo("Buea Moto Part, Buea, SW, Cameroon");
        assertThat(bookJourneyStatus.getDestinationLocation()).isEqualTo("Kumba Moto Part, Kumba, SW, Cameroon");
        assertThat(bookJourneyStatus.getId()).isEqualTo(2L);
        assertThat(bookJourneyStatus.getDepartureTime()).isNotNull();
        assertThat(bookJourneyStatus.getPassengers().get(0).getPassengerEmail()).isEqualTo("email@email.net");
        assertThat(bookJourneyStatus.getPassengers().get(0).getPassengerPhoneNumber()).isEqualTo("67676767");
        assertThat(bookJourneyStatus.getPassengers().get(0).getCheckedInCode()).isEqualTo("1111-1599933993");
        assertThat(bookJourneyStatus.getPassengers().get(0).isCheckedIn()).isFalse();

    }

    @Test
    void getHtmlReceipt_returnHtmlStringOfReceipt_whenBookJourneyIdExit() {

        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.of(bookJourney));
        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        when(mockEmailContentBuilder.buildTicketPdfHtml(any()))
                .thenReturn("<html></html>");
        String htmlReceipt = bookJourneyService.getHtmlReceipt(2L);
        assertThat(htmlReceipt).isEqualTo("<html></html>");

    }

    @Test
    void handlePaymentResponse_throwException_whenBookJourneyIdDontExit() {

        when(mockBookedJourneyRepository.findById(2L))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () ->  bookJourneyService.handlePaymentResponse(2L, new PaymentStatusResponseDTO()));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());


    }

    @Test
    void handlePaymentResponse_UpdateTransactionStatus_whenBookJourneyIdExitAndTransactionValid() {

        PaymentStatusResponseDTO paymentStatusResponseDTO = new PaymentStatusResponseDTO();
        paymentStatusResponseDTO.setAppTransactionNumber("AppTxnNumber");
        paymentStatusResponseDTO.setPaymentChannelCode("MTN_MOBILE_MONEY");
        paymentStatusResponseDTO.setPaymentChannelTransactionNumber("PcTxnNumber");
        paymentStatusResponseDTO.setTransactionStatus("COMPLETED");
        paymentStatusResponseDTO.setProcessingNumber("processingNumber001");

        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
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
        verify(mockFileStorageService).saveFile(anyString(), any(), anyString(), any());
        verify(mockFileStorageService).getFilePath(anyString(), anyString(), any());
        PaymentTransaction paymentTransactionValue = paymentTransactionArgumentCaptor.getValue();

        assertThat(paymentTransactionValue.getTransactionStatus()).isEqualTo("COMPLETED");
        assertThat(paymentTransactionValue.getPaymentChannelTransactionNumber()).isEqualTo("PcTxnNumber");
        assertThat(paymentTransactionValue.getPaymentChannel()).isEqualTo("MTN_MOBILE_MONEY");

    }

    @Test
    void getPassengerOnBoardingInfo_throwsException_whenCheckInCodeNotFound() {

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getPassengerOnBoardingInfo("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());

    }

    @Test
    void getPassengerOnBoardingInfo_throwsException_whenJourneyAlreadyStarted() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getPassengerOnBoardingInfo("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_STARTED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_STARTED.getMessage());
    }

    @Test
    void getPassengerOnBoardingInfo_throwsException_whenJourneyAlreadyTerminated() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(true);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getPassengerOnBoardingInfo("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_TERMINATED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_TERMINATED.getMessage());
    }

    @Test
    void getPassengerOnBoardingInfo_throwsException_whenPaymentDeclined() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(DECLINED.toString());
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.getPassengerOnBoardingInfo("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());
    }

    @Test
    void getPassengerOnBoardingInfo_returnOnBoardingDTO_whenJourneyNotStartedOrTerminatedAndPaymentCompleted() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        bookedJourney.getPassengers().add(new Passenger("paul", "12345678", 16, "paul@gmail.com", "66887541", "1101-0001", false));
        Location location = new Location();
        location.setCountry("Cameroon");
        location.setState("SW");
        location.setCity("Buea");
        location.setAddress("Malingo");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        journey.setDestination(transitAndStop);
        journey.setDepartureLocation(transitAndStop);
        journey.setDriver(new Driver());
        journey.setCar(new Bus());
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);
        bookedJourney.setDestination(transitAndStop);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));
        OnBoardingInfoDTO dto = bookJourneyService.getPassengerOnBoardingInfo("someCode");
        assertThat(dto.getAmount()).isEqualTo(bookedJourney.getAmount());
        assertThat(dto.getCurrencyCode()).isEqualTo(paymentTransaction.getCurrencyCode());
        assertThat(dto.getDepartureTime()).isEqualTo(journey.getDepartureTime());
        assertThat(dto.getPassengers().get(0).getCheckedInCode()).isEqualTo(bookedJourney.getPassengers().get(0).getCheckedInCode());
        assertThat(dto.getCarDriverName()).isEqualTo(journey.getDriver().getDriverName());
        assertThat(dto.getCarLicenseNumber()).isEqualTo(journey.getCar().getLicensePlateNumber());
        assertThat(dto.getCarName()).isEqualTo(journey.getCar().getName());
        assertThat(dto.getDepartureLocation()).isEqualTo("Malingo, Buea, SW, Cameroon");
        assertThat(dto.getDestinationLocation()).isEqualTo("Malingo, Buea, SW, Cameroon");
        assertThat(dto.getPassengers().get(0).getPassengerName()).isEqualTo("paul");
        assertThat(dto.getPassengers().get(0).getPassengerIdNumber()).isEqualTo("12345678");
        assertThat(dto.getPassengers().get(0).getPassengerSeatNumber()).isEqualTo(16);
        assertThat(dto.getPassengers().get(0).getPassengerEmail()).isEqualTo("paul@gmail.com");
        assertThat(dto.getPassengers().get(0).getPassengerPhoneNumber()).isEqualTo("66887541");
    }

    @Test
    void checkInPassenger_throwException_whenJourneyAlreadyStarted() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);

        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.checkInPassengerByCode("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_STARTED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_STARTED.getMessage());
    }

    @Test
    void checkInPassenger_throwException_whenJourneyAlreadyTerminated() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(true);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.checkInPassengerByCode("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getErrorCode()).isEqualTo("JOURNEY_ALREADY_TERMINATED");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.JOURNEY_ALREADY_TERMINATED.getMessage());
    }

    @Test
    void checkInPassenger_throwException_whenPaymentDeclined() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(DECLINED.toString());
        paymentTransaction.setAppTransactionNumber("apTxnNumber");
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        Passenger passenger = new Passenger();
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.checkInPassengerByCode("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());
    }

    @Test
    void checkInPassenger_throwException_whenCheckInCodeNotFound() {

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.checkInPassengerByCode("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.RESOURCE_NOT_FOUND.getMessage());
    }

    @Test
    void checkInPassenger_throwException_whenUserAlreadyCheckedIn() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAppTransactionNumber("apTxnNumber");
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        Passenger passenger = new Passenger();
        passenger.setPassengerCheckedInIndicator(true);
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));

        ApiException apiException = assertThrows(ApiException.class, () -> bookJourneyService.checkInPassengerByCode("someCode"));
        assertThat(apiException.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(apiException.getErrorCode()).isEqualTo("PASSENGER_ALREADY_CHECKED_IN");
        assertThat(apiException.getMessage()).isEqualTo(ErrorCodes.PASSENGER_ALREADY_CHECKED_IN.getMessage());

    }

    @Test
    void checkInPassenger_checkIn_whenUserNotAlreadyCheckedIn() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAppTransactionNumber("apTxnNumber");
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        Passenger passenger = new Passenger();
        passenger.setPassengerCheckedInIndicator(false);
        passenger.setBookedJourney(bookedJourney);

        when(mockPassengerRepository.findByCheckedInCode(anyString()))
                .thenReturn(Optional.of(passenger));
        bookJourneyService.checkInPassengerByCode("someCode");
        verify(mockPassengerRepository).save(passengerArgumentCaptor.capture());
        assertThat(passengerArgumentCaptor.getValue().getPassengerCheckedInIndicator()).isEqualTo(true);
    }

    @Test
    void getAllPassengerOnBoardingInfo_returnOnBoardingDTOList() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setPaymentTransaction(paymentTransaction);
        bookedJourney.getPassengers().add(new Passenger("paul", "12345678", 16, "paul@gmail.com", "66887541", "1101-0001", false));
        Location location = new Location();
        location.setCountry("Cameroon");
        location.setState("SW");
        location.setCity("Buea");
        location.setAddress("Malingo");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        journey.setDestination(transitAndStop);
        journey.setDepartureLocation(transitAndStop);
        journey.setDriver(new Driver());
        journey.setCar(new Bus());
        bookedJourney.setDestination(transitAndStop);
        when(mockBookedJourneyRepository.findAllByJourneyId(anyLong()))
                .thenReturn(Collections.singletonList(bookedJourney));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        List<OnBoardingInfoDTO> dto = bookJourneyService.getAllPassengerOnBoardingInfo(journey.getId());
        assertThat(dto.get(0).getAmount()).isEqualTo(bookedJourney.getAmount());
        assertThat(dto.get(0).getCurrencyCode()).isEqualTo(paymentTransaction.getCurrencyCode());
        assertThat(dto.get(0).getDepartureTime()).isEqualTo(journey.getDepartureTime());
        assertThat(dto.get(0).getPassengers().get(0).getCheckedInCode()).isEqualTo(bookedJourney.getPassengers().get(0).getCheckedInCode());
        assertThat(dto.get(0).getCarDriverName()).isEqualTo(journey.getDriver().getDriverName());
        assertThat(dto.get(0).getCarLicenseNumber()).isEqualTo(journey.getCar().getLicensePlateNumber());
        assertThat(dto.get(0).getCarName()).isEqualTo(journey.getCar().getName());
        assertThat(dto.get(0).getDepartureLocation()).isEqualTo("Malingo, Buea, SW, Cameroon");
        assertThat(dto.get(0).getDestinationLocation()).isEqualTo("Malingo, Buea, SW, Cameroon");
        assertThat(dto.get(0).getPassengers().get(0).getPassengerName()).isEqualTo("paul");
        assertThat(dto.get(0).getPassengers().get(0).getPassengerIdNumber()).isEqualTo("12345678");
        assertThat(dto.get(0).getPassengers().get(0).getPassengerSeatNumber()).isEqualTo(16);
        assertThat(dto.get(0).getPassengers().get(0).getPassengerEmail()).isEqualTo("paul@gmail.com");
        assertThat(dto.get(0).getPassengers().get(0).getPassengerPhoneNumber()).isEqualTo("66887541");
    }

}
