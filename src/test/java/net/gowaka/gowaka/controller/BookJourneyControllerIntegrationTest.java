package net.gowaka.gowaka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.*;
import net.gowaka.gowaka.dto.BookJourneyRequest;
import net.gowaka.gowaka.dto.CodeDTO;
import net.gowaka.gowaka.dto.OnBoardingInfoDTO;
import net.gowaka.gowaka.dto.PaymentStatusResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.service.FileStorageService;
import net.gowaka.gowaka.service.NotificationService;
import net.gowaka.gowaka.service.PayAmGoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.LocalDateTime;

import static net.gowaka.gowaka.TestUtils.createToken;
import static net.gowaka.gowaka.network.api.payamgo.PayAmGoPaymentStatus.COMPLETED;
import static net.gowaka.gowaka.network.api.payamgo.PayAmGoPaymentStatus.WAITING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
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
    private MockMvc mockMvc;
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

    @Before
    public void setUp() {

        User newUser = new User();
        newUser.setUserId("12");
        newUser.setTimestamp(LocalDateTime.now());

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
        Bus savedCar = carRepository.save(car);

        Journey newJourney = new Journey();
        newJourney.setCar(savedCar);
        newJourney.setArrivalIndicator(false);
        newJourney.setDepartureIndicator(false);
        newJourney.setAmount(2000.00);
        newJourney.setDestination(savedDestination);
        newJourney.setDepartureLocation(savedDeparture);
        newJourney.setDepartureTime(LocalDateTime.of(2020, 3, 26, 9, 35));
        newJourney.setEstimatedArrivalTime(LocalDateTime.of(2020, 3, 26, 10, 35));

        Driver driver = new Driver();
        driver.setDriverLicenseNumber("321SW");
        driver.setDriverName("Michael John");
        newJourney.setDriver(driver);

        Journey savedJourney = journeyRepository.save(newJourney);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(savedJourney);
        bookedJourney.setUser(user);
        bookedJourney.setPassenger(new Passenger("John Doe", "1234001", 8));
        bookedJourney.setDestination(savedDestination);
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setCheckedInCode("1111-1599933993");
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

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

    @Test
    public void bookJourney_failure_return400_whenRequestParameterNotValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();

        RequestBuilder requestBuilder = post("/api/protected/bookJourney/journey/" + journey.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(bookJourneyRequest))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void bookJourney_success_return200_whenRequestParameterNotValid() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setSeatNumber(10);
        bookJourneyRequest.setTransitAndStopId(1L);
        bookJourneyRequest.setPhoneNumber("676767676");
        bookJourneyRequest.setPassengerIdNumber("1234567890");
        bookJourneyRequest.setPassengerName("John Doe");
        bookJourneyRequest.setEmail("info@go-groups.net");
        bookJourneyRequest.setDestinationIndicator(true);

        PayAmGoRequestResponseDTO payAmGoResponse = new PayAmGoRequestResponseDTO();
        payAmGoResponse.setAppTransactionNumber("appTxnId");
        payAmGoResponse.setProcessingNumber("processing001");
        payAmGoResponse.setPaymentUrl("https://payamgo.com/payment_url");
        when(payAmGoService.initiatePayment(any()))
                .thenReturn(payAmGoResponse);

        RequestBuilder requestBuilder = post("/api/protected/bookJourney/journey/" + journey.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(bookJourneyRequest))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
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
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":" + bookedJourney.getId() + ",\"amount\":2000.0,\"currencyCode\":\"XAF\",\"paymentStatus\":\"COMPLETED\",\"checkedInCode\":\"1111-1599933993\",\"paymentReason\":\"Bus ticket\",\"paymentChannel\":\"MTN_MOBILE_MONEY\",\"paymentChannelTransactionNumber\":null,\"paymentDate\":\"2020-03-26T09:30:00\",\"checkedIn\":false,\"passengerName\":\"John Doe\",\"passengerIdNumber\":\"1234001\",\"passengerSeatNumber\":8,\"passengerEmail\":\"email@email.net\",\"passengerPhoneNumber\":\"999999\",\"carName\":\"Musango 30 Seater Bus\",\"carLicenseNumber\":\"123SW\",\"carDriverName\":\"Michael John\",\"departureLocation\":\"Buea Moto Part, Buea SW, Cameroon\",\"departureTime\":\"2020-03-26T09:35:00\",\"estimatedArrivalTime\":\"2020-03-26T10:35:00\",\"destinationLocation\":\"Kumba Moto Part, Kumba SW, Cameroon\",\"qrcheckedInImage\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA10lEQVR42u3XSw7EIAgGYFx5DG/q46Yeg1UdATvJTOzavwnGRdtvQ0TQ0nga5OLi8g5hmiOMUSlVeUxIIo+NKXBq9yuOzHibRm0OJ0yFUWV0TNFspyuO/T44KVolEvK+fk7KGr089Z2DIlFLwq/Ys0wkkT1IRSvY0g4l2ZLMNEOuhCVB0WZgJJnFESfqWrIca0BiQz+Xb62AyN1dqqT6d0XPixaHxCszE5boSSvdhWbgceCJ3gL+so0iSZe2BzCxZrxwcz84KKtKrLW0TQWfE/+fc3F5qXwAkHCU9h+9LrYAAAAASUVORK5CYII=\"}"))
                .andReturn();
    }

    @Test
    public void handlePaymentResponses_success_return204_whenPaymentResponseRecieved() throws Exception {


        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setUser(user);
        bookedJourney.setPassenger(new Passenger("Edward Tanko", "1234033", 10));
        bookedJourney.setDestination(journey.getDestination());
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setCheckedInCode("2111-1599933988");
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
        verify(fileStorageService).savePublicFile(any(), any(), any());
        verify(fileStorageService).getPublicFilePath(any(), any());

    }

    @Test
    public void bookedJourneyHistory_success_return200() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "USERS");

//        BookedJourney bookedJourney = journey.getBookedJourneys().get(0);
        RequestBuilder requestBuilder = get("/api/protected/bookJourney/history")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
//                .andExpect(content().json("[{\"id\":1,\"amount\":2000.0,\"currencyCode\":\"XAF\",\"paymentStatus\":\"COMPLETED\",\"checkedInCode\":\"1111-1599933993\",\"paymentReason\":\"Bus ticket\",\"paymentChannel\":\"MTN_MOBILE_MONEY\",\"paymentChannelTransactionNumber\":null,\"paymentDate\":\"2020-03-26T09:30:00\",\"checkedIn\":false,\"passengerName\":\"John Doe\",\"passengerIdNumber\":\"1234001\",\"passengerSeatNumber\":8,\"passengerEmail\":\"email@email.net\",\"passengerPhoneNumber\":\"999999\",\"carName\":\"Musango 30 Seater Bus\",\"carLicenseNumber\":\"123SW\",\"carDriverName\":\"Michael John\",\"departureLocation\":\"Buea Moto Part, Buea SW, Cameroon\",\"departureTime\":\"2020-03-26T09:35:00\",\"estimatedArrivalTime\":\"2020-03-26T10:35:00\",\"destinationLocation\":\"Kumba Moto Part, Kumba SW, Cameroon\",\"qrcheckedInImage\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA10lEQVR42u3XSw7EIAgGYFx5DG/q46Yeg1UdATvJTOzavwnGRdtvQ0TQ0nga5OLi8g5hmiOMUSlVeUxIIo+NKXBq9yuOzHibRm0OJ0yFUWV0TNFspyuO/T44KVolEvK+fk7KGr089Z2DIlFLwq/Ys0wkkT1IRSvY0g4l2ZLMNEOuhCVB0WZgJJnFESfqWrIca0BiQz+Xb62AyN1dqqT6d0XPixaHxCszE5boSSvdhWbgceCJ3gL+so0iSZe2BzCxZrxwcz84KKtKrLW0TQWfE/+fc3F5qXwAkHCU9h+9LrYAAAAASUVORK5CYII=\"}]"))
                .andReturn();
    }

    @Test
    public void getOnBoardingInfoResponse_success_return200() throws Exception {
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setUser(user);
        bookedJourney.setPassenger(new Passenger("Edward Tanko", "1234033", 10));
        bookedJourney.setDestination(journey.getDestination());
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setCheckedInCode("2111-1599933988");
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

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

        String expectedResponse = "{\"amount\":2000.0," +
                "\"currencyCode\":\"XAF\"," +
                "\"carDriverName\":\"Michael John\"," +
                "\"carLicenseNumber\":\"123SW\"," +
                "\"carName\":\"Musango 30 Seater Bus\"," +
                "\"departureLocation\":\"Buea Moto Part, Buea, SW, Cameroon\"," +
                "\"departureTime\":\"2020-03-26T09:35:00\"," +
                "\"destinationLocation\":\"Kumba Moto Part, Kumba, SW, Cameroon\"," +
                "\"passengerEmail\":null," +
                "\"passengerIdNumber\":\"1234033\"," +
                "\"passengerName\":\"Edward Tanko\"," +
                "\"passengerPhoneNumber\":null," +
                "\"passengerSeatNumber\":10," +
                "\"checkedInCode\":\"2111-1599933988\"," +
                "\"passengerCheckedInIndicator\":false}";

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "AGENCY_BOOKING");
        RequestBuilder requestBuilder = get("/api/protected/checkIn_status?code=" + savedBookJourney.getCheckedInCode())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    @Test
    public void checkInPassenger_success_return204() throws Exception {
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setUser(user);
        bookedJourney.setPassenger(new Passenger("Edward Tanko", "1234033", 10));
        bookedJourney.setDestination(journey.getDestination());
        bookedJourney.setPassengerCheckedInIndicator(false);
        bookedJourney.setCheckedInCode("2000-1599933988");
        bookedJourney.setAmount(2000.00);

        BookedJourney savedBookJourney = bookedJourneyRepository.save(bookedJourney);

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

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "AGENCY_BOOKING");
        RequestBuilder requestBuilder = post("/api/protected/checkIn")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(new CodeDTO("2000-1599933988")))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

}
