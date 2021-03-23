package net.gogroups.gowaka.controller.integration;

import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 6:05 AM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class CacheDataProcessorControllerIntegrationTest {

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
    private AgencyBranchRepository agencyBranchRepository;
    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void getAppNotice_success_returns_200() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "Me User", secretKey, "GW_ADMIN");

        RequestBuilder requestBuilder = get("/api/protected/cache/journey")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journeys[0].car.agencyId").value(journey.getAgencyBranch().getOfficialAgency().getId()))
                .andExpect(jsonPath("$.journeys[0].branchId").value(journey.getAgencyBranch().getId()))
                .andExpect(jsonPath("$.bookedSeats[0].journeyId").value(journey.getId()))
                .andExpect(jsonPath("$.bookedSeats[0].bookedSeats[0]").value(8));
    }

}
