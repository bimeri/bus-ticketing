package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.dto.BookJourneyRequest;
import net.gogroups.gowaka.dto.CodeDTO;
import net.gogroups.gowaka.dto.PaymentStatusResponseDTO;
import net.gogroups.gowaka.dto.PhoneNumberDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.payamgo.constants.PayAmGoPaymentStatus;
import net.gogroups.payamgo.model.PayAmGoRequestResponseDTO;
import net.gogroups.payamgo.service.PayAmGoService;
import net.gogroups.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.LocalDateTime;
import java.util.Collections;

import static net.gogroups.gowaka.TestUtils.createToken;
import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.COMPLETED;
import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.WAITING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/26/20 12:05 AM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookJourneyControllerIntegrationTest {

    @Autowired
    private JourneyRepository journeyRepository;
    @Autowired
    private BookedJourneyRepository bookedJourneyRepository;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired
    private TransitAndStopRepository transitAndStopRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfficialAgencyRepository officialAgencyRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private AgencyBranchRepository agencyBranchRepository;

    @MockBean
    private PayAmGoService payAmGoService;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private FileStorageService fileStorageService;

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    private Journey journey;
    private User user;

    @BeforeEach
    public void setUp() {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        officialAgency.setCode("GG");
        OfficialAgency savedOA = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setOfficialAgency(savedOA);
        agencyBranch.setName("Main Office");
        AgencyBranch savedBranch = agencyBranchRepository.save(agencyBranch);

        User newUser = new User();
        newUser.setUserId("12");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setOfficialAgency(officialAgency);
        newUser.setAgencyBranch(savedBranch);

        this.user = userRepository.save(newUser);

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
        destination.setLocation(destinationLocation);

        TransitAndStop departure = new TransitAndStop();
        departure.setLocation(departureLocation);

        TransitAndStop savedDeparture = transitAndStopRepository.save(departure);
        TransitAndStop savedDestination = transitAndStopRepository.save(destination);

        Bus car = new Bus();
        car.setLicensePlateNumber("123SW");
        car.setName("Musango 30 Seater Bus");
        car.setNumberOfSeats(10);
        car.setOfficialAgency(officialAgency);
        car.setIsOfficialAgencyIndicator(true);
        Bus savedCar = carRepository.save(car);

        officialAgency.setBuses(Collections.singletonList(car));
        officialAgencyRepository.save(officialAgency);

        Journey newJourney = new Journey();
        newJourney.setCar(savedCar);
        newJourney.setArrivalIndicator(false);
        newJourney.setDepartureIndicator(false);
        newJourney.setAmount(2000.00);
        newJourney.setDestination(savedDestination);
        newJourney.setDepartureLocation(savedDeparture);
        newJourney.setDepartureTime(LocalDateTime.of(2020, 3, 26, 9, 35));
        newJourney.setEstimatedArrivalTime(LocalDateTime.of(2020, 3, 26, 10, 35));
        newJourney.setAgencyBranch(savedBranch);
        Driver driver = new Driver();
        driver.setDriverLicenseNumber("321SW");
        driver.setDriverName("Michael John");
        newJourney.setDriver(driver);

        Journey savedJourney = journeyRepository.save(newJourney);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(savedJourney);
        bookedJourney.setUser(user);
        bookedJourney.setDestination(savedDestination);
        bookedJourney.setAmount(2000.00);
        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

        Passenger passenger = new Passenger("John Doe", "1234001", 8, "email@example.com", "123423", "1111-1599933993", false);
        passenger.setBookedJourney(savedBookJourney);
        passengerRepository.save(passenger);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("999999");
        paymentTransaction.setAppUserEmail("email@email.net");
        paymentTransaction.setAppUserLastName("John");
        paymentTransaction.setAppUserLastName("Doe");
        paymentTransaction.setBookedJourney(savedBookJourney);
        paymentTransaction.setPaymentDate(LocalDateTime.of(2020, 3, 26, 9, 30));

        paymentTransactionRepository.save(paymentTransaction);

        this.journey = journeyRepository.findById(savedJourney.getId()).get();

    }


//    TODO: Fix me
//    @Test
//    public void bookJourney_failure_return400_whenRequestParameterNotValid() throws Exception {
//
//        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
//        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
//
//        RequestBuilder requestBuilder = post("/api/protected/bookJourney/journey/" + journey.getId())
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .header("Authorization", "Bearer " + jwtToken)
//                .content(new ObjectMapper().writeValueAsString(bookJourneyRequest))
//                .accept(MediaType.APPLICATION_JSON);
//        mockMvc.perform(requestBuilder)
//                .andExpect(status().isBadRequest())
//                .andReturn();
//    }

    @Test
    public void bookJourney_success_return200_whenRequestParameterNotValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setTransitAndStopId(1L);

        BookJourneyRequest.Passenger passenger = new BookJourneyRequest.Passenger();
        passenger.setSeatNumber(10);
        passenger.setEmail("info@go-groups.net");
        passenger.setPassengerName("John Doe");
        passenger.setPassengerIdNumber("1234567890");
        passenger.setPhoneNumber("676767676");
        bookJourneyRequest.getPassengers().add(passenger);
        bookJourneyRequest.setDestinationIndicator(true);

        PayAmGoRequestResponseDTO payAmGoResponse = new PayAmGoRequestResponseDTO();
        payAmGoResponse.setAppTransactionNumber("appTxnId");
        payAmGoResponse.setProcessingNumber("processing001");
        payAmGoResponse.setPaymentUrl("https://payamgo.com/payment_url");
        when(payAmGoService.initiatePayment(any()))
                .thenReturn(payAmGoResponse);

        RequestBuilder requestBuilder = post("/api/protected/bookJourney/journey/" + journey.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(bookJourneyRequest))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void agencyUserBookJourney_success_return200_whenRequestParameterNotValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "AGENCY_OPERATOR");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setTransitAndStopId(1L);

        BookJourneyRequest.Passenger passenger = new BookJourneyRequest.Passenger();
        passenger.setSeatNumber(10);
        passenger.setEmail("info@go-groups.net");
        passenger.setPassengerName("John Doe");
        passenger.setPassengerIdNumber("1234567890");
        passenger.setPhoneNumber("676767676");
        bookJourneyRequest.getPassengers().add(passenger);
        bookJourneyRequest.setDestinationIndicator(true);

        RequestBuilder requestBuilder = post("/api/protected/bookJourney/journey/" + journey.getId() + "/agency")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(bookJourneyRequest))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void getAllBookedSeats_success_return200_whenRequestParameterNotValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");

        RequestBuilder requestBuilder = get("/api/protected/bookJourney/journey/" + journey.getId() + "/booked_seats")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("[8]"))
                .andReturn();
    }

    @Test
    public void getBookJourneyStatus_success_return200_whenRequestParameterIsValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");

        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
        RequestBuilder requestBuilder = get("/api/protected/bookJourney/" + bookedJourney.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    /*@Test
    public void downloadReceipt_success_return200_whenRequestParameterIsValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");

        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
        RequestBuilder requestBuilder = get("/api/protected/bookJourney/" + bookedJourney.getId() + "/receipt")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }*/

    @Test
    public void handlePaymentResponses_success_return204_whenPaymentResponseRecieved() throws Exception {


        Passenger passenger = new Passenger("Edward Tanko", "1234033", 10, "email@example.com", "123423", "2111-1599933988", false);
        BookedJourney bookedJourney = new BookedJourney();
        passenger.setBookedJourney(bookedJourney);
        bookedJourney.setJourney(journey);
        bookedJourney.setUser(user);
        bookedJourney.getPassengers().add(passenger);
        bookedJourney.setDestination(journey.getDestination());
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(WAITING.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("55555");
        paymentTransaction.setAppUserEmail("tanko.edward@go-groups.net");
        paymentTransaction.setAppUserLastName("Edward");
        paymentTransaction.setAppUserLastName("Tanko");
        paymentTransaction.setBookedJourney(savedBookJourney);
        paymentTransaction.setProcessingNumber("2222222222");
        paymentTransaction.setAppTransactionNumber("3333333333");
        paymentTransactionRepository.save(paymentTransaction);

        PaymentStatusResponseDTO paymentStatusResponseDTO = new PaymentStatusResponseDTO();
        paymentStatusResponseDTO.setProcessingNumber("2222222222");
        paymentStatusResponseDTO.setAppTransactionNumber("3333333333");
        paymentStatusResponseDTO.setTransactionStatus("COMPLETED");
        paymentStatusResponseDTO.setPaymentChannelTransactionNumber("txtxtxtxt123");
        paymentStatusResponseDTO.setPaymentChannelCode("ORANGE_MONEY");

        RequestBuilder requestBuilder = post("/api/public/booking/status/" + savedBookJourney.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(paymentStatusResponseDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
        verify(notificationService).sendEmail(any());
        verify(fileStorageService).saveFile(any(), any(), any(), any());
        verify(fileStorageService, times(3)).getFilePath(any(), any(), any());

    }

    @Test
    public void bookedJourneyHistory_success_return200() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");

        RequestBuilder requestBuilder = get("/api/protected/bookJourney/history")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getOnBoardingInfoResponse_success_return200() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG kingston");
        officialAgencyRepository.save(officialAgency);

        User newUser = new User();
        newUser.setUserId("11");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setOfficialAgency(officialAgency);
        newUser.setAgencyBranch(user.getAgencyBranch());

        newUser = userRepository.save(newUser);

        Location destinationLocation = new Location();
        destinationLocation.setAddress("Kosalla Moto Part");
        destinationLocation.setCity("KumbaMbeng");
        destinationLocation.setState("SW");
        destinationLocation.setCountry("Cameroon");

        Location departureLocation = new Location();
        departureLocation.setAddress("Buea Sawa Moto Part");
        departureLocation.setCity("Sawa");
        departureLocation.setState("SW");
        departureLocation.setCountry("Cameroon");

        TransitAndStop destination = new TransitAndStop();
        destination.setLocation(destinationLocation);

        TransitAndStop departure = new TransitAndStop();
        departure.setLocation(departureLocation);

        TransitAndStop savedDeparture = transitAndStopRepository.save(departure);
        TransitAndStop savedDestination = transitAndStopRepository.save(destination);

        Bus car = new Bus();
        car.setLicensePlateNumber("1232233SW");
        car.setName("Musango 35 Seater Bus");
        car.setNumberOfSeats(35);
        car.setOfficialAgency(officialAgency);
        carRepository.save(car);

        officialAgency.setBuses(Collections.singletonList(car));
        officialAgencyRepository.save(officialAgency);

        Journey newJourney = new Journey();
        Car bus = carRepository.findById(car.getId()).get();
        newJourney.setCar(bus);
        newJourney.setArrivalIndicator(false);
        newJourney.setDepartureIndicator(false);
        newJourney.setAmount(2000.00);
        newJourney.setDestination(savedDestination);
        newJourney.setDepartureLocation(savedDeparture);
        newJourney.setDepartureTime(LocalDateTime.of(2020, 3, 26, 9, 35));
        newJourney.setEstimatedArrivalTime(LocalDateTime.of(2020, 3, 26, 10, 35));
        newJourney.setAgencyBranch(user.getAgencyBranch());

        Driver driver = new Driver();
        driver.setDriverLicenseNumber("321SW");
        driver.setDriverName("Michael John");
        newJourney.setDriver(driver);

        newJourney = journeyRepository.save(newJourney);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(newJourney);
        bookedJourney.setUser(newUser);
        bookedJourney.addPassenger(new Passenger("Edward Tanko", "1234033", 10, "email@example.com", "123423", "24755hsyw08jaja", false));
        bookedJourney.setDestination(newJourney.getDestination());
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);
        newJourney.setBookedJourneys(Collections.singletonList(bookedJourney));
        journeyRepository.save(newJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("55555");
        paymentTransaction.setAppUserEmail("tanko.edward@go-groups.net");
        paymentTransaction.setAppUserLastName("Edward");
        paymentTransaction.setAppUserLastName("Tanko");
        paymentTransaction.setBookedJourney(savedBookJourney);
        paymentTransaction.setProcessingNumber("2222222222");
        paymentTransaction.setAppTransactionNumber("3333333333");
        paymentTransactionRepository.save(paymentTransaction);

        savedBookJourney.setPaymentTransaction(paymentTransaction);
        bookedJourneyRepository.save(savedBookJourney);

        String jwtToken = createToken(newUser.getUserId(), "ggadmin@gg.com", "Me User", secretKey, "AGENCY_BOOKING");
        RequestBuilder requestBuilder = get("/api/protected/checkIn_status?code=24755hsyw08jaja")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void checkInPassenger_success_return204() throws Exception {
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setUser(user);
        Passenger passenger = new Passenger("Edward Tanko", "1234033", 10, "email@example.com", "123423", "2000-1599933988", false);
        passenger.setBookedJourney(bookedJourney);
        bookedJourney.getPassengers().add(passenger);
        bookedJourney.setDestination(journey.getDestination());
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

        journey.setBookedJourneys(Collections.singletonList(bookedJourney));
        journeyRepository.save(journey);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("55555");
        paymentTransaction.setAppUserEmail("tanko.edward@go-groups.net");
        paymentTransaction.setAppUserLastName("Edward");
        paymentTransaction.setAppUserLastName("Tanko");
        paymentTransaction.setBookedJourney(savedBookJourney);
        paymentTransaction.setProcessingNumber("1111111111");
        paymentTransaction.setAppTransactionNumber("4444444444");
        paymentTransactionRepository.save(paymentTransaction);

        savedBookJourney.setPaymentTransaction(paymentTransaction);
        bookedJourneyRepository.save(savedBookJourney);

        String jwtToken = createToken(user.getUserId(), "ggadmin@gg.com", "Me User", secretKey, "AGENCY_BOOKING");
        RequestBuilder requestBuilder = post("/api/protected/checkIn")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CodeDTO("2000-1599933988")))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void getAllOnBoardingInfoResponse_success_return200() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG kingston");
        officialAgencyRepository.save(officialAgency);

        User newUser = new User();
        newUser.setUserId("11");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setOfficialAgency(officialAgency);
        newUser.setAgencyBranch(user.getAgencyBranch());

        newUser = userRepository.save(newUser);

        Location destinationLocation = new Location();
        destinationLocation.setAddress("Kosalla Moto Part");
        destinationLocation.setCity("KumbaMbeng");
        destinationLocation.setState("SW");
        destinationLocation.setCountry("Cameroon");

        Location departureLocation = new Location();
        departureLocation.setAddress("Buea Sawa Moto Part");
        departureLocation.setCity("Sawa");
        departureLocation.setState("SW");
        departureLocation.setCountry("Cameroon");

        TransitAndStop destination = new TransitAndStop();
        destination.setLocation(destinationLocation);

        TransitAndStop departure = new TransitAndStop();
        departure.setLocation(departureLocation);

        TransitAndStop savedDeparture = transitAndStopRepository.save(departure);
        TransitAndStop savedDestination = transitAndStopRepository.save(destination);

        Bus car = new Bus();
        car.setLicensePlateNumber("1232233SW");
        car.setName("Musango 35 Seater Bus");
        car.setNumberOfSeats(35);
        car.setOfficialAgency(officialAgency);
        carRepository.save(car);

        officialAgency.setBuses(Collections.singletonList(car));
        officialAgencyRepository.save(officialAgency);

        Journey newJourney = new Journey();
        Car bus = carRepository.findById(car.getId()).get();
        newJourney.setCar(bus);
        newJourney.setArrivalIndicator(false);
        newJourney.setDepartureIndicator(false);
        newJourney.setAmount(2000.00);
        newJourney.setDestination(savedDestination);
        newJourney.setDepartureLocation(savedDeparture);
        newJourney.setDepartureTime(LocalDateTime.of(2020, 3, 26, 9, 35));
        newJourney.setEstimatedArrivalTime(LocalDateTime.of(2020, 3, 26, 10, 35));
        newJourney.setAgencyBranch(user.getAgencyBranch());
        Driver driver = new Driver();
        driver.setDriverLicenseNumber("321SW");
        driver.setDriverName("Michael John");
        newJourney.setDriver(driver);

        newJourney = journeyRepository.save(newJourney);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(newJourney);
        bookedJourney.setUser(newUser);
        bookedJourney.getPassengers().add(new Passenger("Edward Tanko", "1234033", 10, "email@example.com", "123423", "24755hsyw08kuku", false));

        bookedJourney.setDestination(newJourney.getDestination());
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);
        newJourney.setBookedJourneys(Collections.singletonList(bookedJourney));
        journeyRepository.save(newJourney);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.toString());
        paymentTransaction.setAmount(2000.00);
        paymentTransaction.setCurrencyCode("XAF");
        paymentTransaction.setPaymentReason("Bus ticket");
        paymentTransaction.setPaymentDate(LocalDateTime.now());
        paymentTransaction.setPaymentChannel("MTN_MOBILE_MONEY");
        paymentTransaction.setAppUserPhoneNumber("55555");
        paymentTransaction.setAppUserEmail("tanko.edward@go-groups.net");
        paymentTransaction.setAppUserLastName("Edward");
        paymentTransaction.setAppUserLastName("Tanko");
        paymentTransaction.setBookedJourney(savedBookJourney);
        paymentTransaction.setProcessingNumber("2222222222");
        paymentTransaction.setAppTransactionNumber("3333333333");
        paymentTransactionRepository.save(paymentTransaction);

        savedBookJourney.setPaymentTransaction(paymentTransaction);
        bookedJourneyRepository.save(savedBookJourney);

        String jwtToken = createToken(newUser.getUserId(), "ggadmin@gg.com", "Me User", secretKey, "AGENCY_BOOKING");
        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/" + newJourney.getId() + "/booking_history")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void changeSeat_failure_return412_whenJourneyAlreadyStarted() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        journey.setDepartureIndicator(true);
        journeyRepository.save(journey);
        String expectedRequest = "[\n" +
                "{\"currentSeatNumber\": 8, \"newSeatNumber\": 6}\n" +
                "]";
        String expectedResponse = "{\"code\":\"JOURNEY_ALREADY_STARTED\",\"message\":" +
                "\"This Journey is already started.\"," +
                "\"endpoint\":\"/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number\"}";
        RequestBuilder requestBuilder = post("/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(expectedRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    @Test
    public void changeSeat_failure_return412_whenJourneyAlreadyTerminated() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(true);
        journeyRepository.save(journey);
        String expectedRequest = "[\n" +
                "{\"currentSeatNumber\": 8, \"newSeatNumber\": 6}\n" +
                "]";
        String expectedResponse = "{\"code\":\"JOURNEY_ALREADY_TERMINATED\",\"message\":" +
                "\"This Journey is already terminated.\"," +
                "\"endpoint\":\"/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number\"}";
        RequestBuilder requestBuilder = post("/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(expectedRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    @Test
    public void changeSeat_failure_return404_whenPaymentNotCompleted() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        PaymentTransaction pt = bookJourney.getPaymentTransaction();
        pt.setTransactionStatus(PayAmGoPaymentStatus.DECLINED.toString());
        paymentTransactionRepository.save(pt);
        journeyRepository.save(journey);
        String expectedRequest = "[\n" +
                "{\"currentSeatNumber\": 8, \"newSeatNumber\": 6}\n" +
                "]";
        String expectedResponse = "{\"code\":\"PAYMENT_NOT_COMPLETED\",\"message\":" +
                "\"Payment is not completed.\",\"endpoint\":\"/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number\"}";
        RequestBuilder requestBuilder = post("/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(expectedRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    @Test
    public void changeSeat_success_return204_whenJourneyNotStartedNorTerminatedSeatNotTaken() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookedJourney bookJourney = journey.getBookedJourneys().get(0);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journeyRepository.save(journey);
        String expectedRequest = "[\n" +
                "{\"currentSeatNumber\": 8, \"newSeatNumber\": 6}\n" +
                "]";
        RequestBuilder requestBuilder = post("/api/protected/bookJourney/" + bookJourney.getId() + "/change_seat_number")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(expectedRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void findPassenger_success_return200() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "AGENCY_MANAGER");

        RequestBuilder requestBuilder = post("/api/protected/bookJourney/find_passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(new PhoneNumberDTO("237", "777777777")))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }


}
