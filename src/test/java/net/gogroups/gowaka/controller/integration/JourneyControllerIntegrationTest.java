package net.gogroups.gowaka.controller.integration;

import net.gogroups.gowaka.TimeProviderTestUtil;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.gogroups.gowaka.TestUtils.createToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class JourneyControllerIntegrationTest {

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfficialAgencyRepository officialAgencyRepository;

    @Autowired
    private PersonalAgencyRepository personalAgencyRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private TransitAndStopRepository transitAndStopRepository;

    @Autowired
    private JourneyRepository journeyRepository;

    @Autowired
    private BookedJourneyRepository bookedJourneyRepository;

    @Autowired
    private AgencyBranchRepository agencyBranchRepository;


    @Autowired
    private MockMvc mockMvc;

    @Qualifier("ggClientRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private User user;

    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {

        User newUser = new User();
        newUser.setUserId("12");
        newUser.setCreatedAt(LocalDateTime.now());

        this.user = userRepository.save(newUser);

        jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    @AfterTestClass
    public static void tearDown() {
        TimeProviderTestUtil.useSystemClock();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void add_journey_should_return_ok_with_valid_journey_response_dto() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");


        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        location.setCity("Kumba");
        location.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        location.setCity("Muyuka");
        location.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location);
        transitAndStopRepository.save(transitAndStop2);
        location.setCity("Ekona");
        location.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String currentTimeStamp = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        String reqBody = "{\n" +
                "  \"departureTime\": \"" + currentDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop1.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop2.getId() + ", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop3.getId() + ", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/cars/" + bus.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void add_journey_should_return_validation_date_time_error() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        String depTime = localDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String reqBody = "{\n" +
                "  \"departureTime\": \"" + depTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + depTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": 1,\n" +
                "  \"destination\": 1,\n" +
                "  \"transitAndStops\": [1]\n" +
                "}\n";
        String expectedResponse = "{\"code\":\"INVALID_FORMAT\",\"message\":\"expected format \\\"yyyy-MM-dd HH:mm:ss\\\"\",\"endpoint\":\"/api/protected/agency/journeys/cars/1\"}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * Scenario 3. Invalid input object
     */
    @Test
    public void update_journey_should_throw_invalid_date_format_error_response() throws Exception {
        String reqBody = "{\n" +
                "  \"departureTime\": \" 27-02-29 \",\n" +
                "  \"estimatedArrivalTime\": \" 27-0-20 \",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": 1,\n" +
                "  \"destination\": 1,\n" +
                "  \"transitAndStops\": [1]\n" +
                "}\n";
        String expectedResponse = "{\"code\":\"INVALID_FORMAT\",\"message\":\"expected format \\\"yyyy-MM-dd HH:mm:ss\\\"\",\"endpoint\":\"/api/protected/agency/journeys/1/cars/1\"}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/1/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * Scenario 5 valid request
     *
     * @throws Exception
     */
    @Test
    public void update_journey_should_return_ok_with_valid_journey_response_dto() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);

        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(branch);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        location.setCity("Kumba");
        location.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        location.setCity("Muyuka");
        location.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location);
        transitAndStopRepository.save(transitAndStop2);
        location.setCity("Ekona");
        location.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String updatedDateTime = localDateTime.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop);
        journey.setDestination(transitAndStop1);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setAgencyBranch(branch);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);

        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journeyRepository.save(journey);
        String expectedResponse = "{\"id\":" + journey.getId() + ",\"departureTime\":\"" + updatedDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\":1000.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":" + transitAndStop1.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Kumba\"," +
                "\"address\":\"Buea Road Motor Park\"" +
                "}," +
                "\"destination\":{" +
                "\"id\":" + transitAndStop.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Buea\"," +
                "\"address\":\"Mile 17 Motto Park\"," +
                "\"amount\":1000.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":" + transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Ekona\"," +
                "\"address\":\"Ekona Main Park\"," +
                "\"amount\":1000.0" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Muyuka\"," +
                "\"address\":\"Muyuka Main Park\"," +
                "\"amount\":2000.0" +
                "}" +
                "]," +
                "\"car\":{" +
                "\"id\":" + bus.getId() + "," +
                "\"name\":\"Kumba One Chances\"," +
                "\"licensePlateNumber\":\"123454387\"," +
                "\"isOfficialAgencyIndicator\":true," +
                "\"isCarApproved\":true," +
                "\"timestamp\":\"" + currentDateTime + "\"" +
                "}}";
        String reqBody = "{\n" +
                "  \"departureTime\": \"" + updatedDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop1.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop3.getId() + ", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop2.getId() + ", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/cars/" + bus.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * #169112516
     * scenario 1 No journey found for user's Agency
     */
    @Test
    public void get_all_journeys_should_return_empty_list_if_no_journey_found() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        officialAgencyRepository.save(officialAgency);
        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
                .andReturn();
    }

    /**
     * #169112516
     * scenario 2 Journeys found in user's Agency
     */
    @Test
    public void get_all_journeys_should_return_list_of_journey_response_dtos() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Malingo Major");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");

        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Kumba");
        location1.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Muyuka");
        location2.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Ekona");
        location3.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);


        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }


    @Test
    public void getOfficialAgencyJourneys_should_return_list_of_journey_response_dtos() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Malingo Major");
        OfficialAgency saveOA = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setOfficialAgency(saveOA);
        agencyBranch.setName("main branch");

        AgencyBranch saveBranch = agencyBranchRepository.save(agencyBranch);

        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(saveBranch);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Kumba");
        location1.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Muyuka");
        location2.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Ekona");
        location3.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setAgencyBranch(saveBranch);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);

        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/page?limit=10&pageNumber=1&branchId="+saveBranch.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].id").value(journey.getId()));
    }

    /**
     * #169114688
     * Scenario 3. Journey Success
     */
    @Test
    public void get_journey_by_id_should_return_journey_response_dto() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Malingo Major");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");


        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Tole Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Kumba");
        location1.setAddress("Fiango Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Muyuka");
        location2.setAddress("Munyenge Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Ekona");
        location3.setAddress("Small Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);
        journey.setJourneyStops(journeyStops);
        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getJourneyById_success_return200_journey_response_dto() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Malingo Major");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");

        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Tole Park");
        location.setCity("Buea");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Kumba");
        location1.setAddress("Fiango Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Muyuka");
        location2.setAddress("Munyenge Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Ekona");
        location3.setAddress("Small Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);
        journey.setJourneyStops(journeyStops);
        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }


    /**
     * #169112805
     * Scenario 5. Add Stop Success
     */
    @Test
    public void add_stops_should_return_204_no_content() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);
        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(branch);
        userRepository.save(user);

        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop1);
        journeyStop.setJourney(journey);
        journey.setJourneyStops(Collections.singletonList(journeyStop));
        journey.setCar(bus);
        journey.setAgencyBranch(branch);
        journeyRepository.save(journey);
        String reqBody = "{\"transitAndStopId\": " + transitAndStop.getId() + ", \"amount\" : 1000.0 }";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/add_stops")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * #169112562
     * Scenario: 4 Delete Journey Success
     */
    @Test
    public void delete_Non_Booked_journey_should_delete_journey_successfully() throws Exception {


        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);

        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(branch);
        userRepository.save(user);
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);

        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(branch);
        journeyRepository.save(journey);
        RequestBuilder requestBuilder = delete("/api/protected/agency/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * #169114980
     * Scenario: 4. change Journey departureIndicator state
     */
    @Test
    public void set_journey_departure_indicator_should_update_and_return_no_content() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);

        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);
        userRepository.save(user);

        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(branch);
        journeyRepository.save(journey);
        String reqBody = "{\"departureIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/departure")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * #169114979
     * Scenario: 4. change Journey arrivalIndicator state
     */
    @Test
    public void update_journey_arrival_indicator_should_update_and_return_no_content() throws Exception {


        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);

        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(branch);
        userRepository.save(user);
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);

        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureIndicator(true);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(branch);
        journeyRepository.save(journey);
        String reqBody = "{\"arrivalIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/arrival")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 2 Invalid input object
     */
    @Test
    public void personal_agency_add_journey_shared_rides_should_return_bad_request_with_validation_errors() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        String depTime = localDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String badRequest = "{\n" +
                "  \"departureTime\": \"" + depTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + depTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" + "\"departureLocation\": 10," +
                "  \"transitAndStops\": [{\"transitAndStopId\": 12,\"amount\": 600},{\"transitAndStopId\": 13,\"amount\": 700}]\n" +
                "}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(badRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.[0].field").value("destination"))
                .andExpect(jsonPath("$.errors.[0].message").value("destination is required"));
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 2 Invalid input object
     */
    @Test
    public void personal_agency_add_journey_shared_rides_should_return_unprocessable_entity_with_validation_errors() throws Exception {
        String badRequest = "{\n" +
                "  \"departureTime\": \" 11:11:11\",\n" +
                "  \"estimatedArrivalTime\": \"2019-02-20 11:11:11\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" + "\"departureLocation\": 10," +
                "  \"transitAndStops\": [{\"transitAndStopId\": 12,\"amount\": 600},{\"transitAndStopId\": 13,\"amount\": 700}]\n" +
                "}";
        String expectedResponse = "{\"code\":\"INVALID_FORMAT\",\"message\":\"expected format \\\"yyyy-MM-dd HH:mm:ss\\\"\",\"endpoint\":\"/api/protected/users/journeys/cars/1\"}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(badRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 3 Success
     */
    @Test
    public void personal_agency_add_journey_shared_rides_should_return_ok_with_journey_response_dto() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("my way");
        personalAgencyRepository.save(personalAgency);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("Te widikum");
        sharedRide.setLicensePlateNumber("1234568755");
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setIsCarApproved(true);


        user.setPersonalAgency(personalAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("Konye");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        location.setCity("Mabanda");
        location.setAddress("Mabanda Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        location.setCity("Butu");
        location.setAddress("Butu German Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location);
        transitAndStopRepository.save(transitAndStop2);
        location.setCity("Matoh");
        location.setAddress("Matoh Backside Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sharedRide.setCreatedAt(TimeProviderTestUtil.now());
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);
        String reqBody = "{\n" +
                "  \"departureTime\": \"" + currentDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop1.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop2.getId() + ", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop3.getId() + ", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/cars/" + sharedRide.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 3 Invalid input object missing field
     */
    @Test
    public void personal_agency_update_journey_shared_rides_should_return_bad_request_with_validation_errors() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        String depTime = localDateTime.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String badRequest = "{\n" +
                "  \"departureTime\": \"" + depTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + depTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" + "\"departureLocation\": 10," +
                "  \"transitAndStops\": [{\"transitAndStopId\": 12,\"amount\": 600},{\"transitAndStopId\": 13,\"amount\": 700}]\n" +
                "}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/1/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(badRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.[0].field").value("destination"))
                .andExpect(jsonPath("$.errors.[0].message").value("destination is required"));
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 3 Invalid input object missing field
     */
    @Test
    public void personal_agency_update_journey_shared_rides_should_return_unprocessable_entity_with_validation_errors() throws Exception {
        String badRequest = "{\n" +
                "  \"departureTime\": \" 11:11:11\",\n" +
                "  \"estimatedArrivalTime\": \"2019-02-20 11:11:11\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" + "\"departureLocation\": 10," +
                "  \"transitAndStops\": [{\"transitAndStopId\": 12,\"amount\": 600},{\"transitAndStopId\": 13,\"amount\": 700}]\n" +
                "}";
        String expectedResponse = "{\"code\":\"INVALID_FORMAT\",\"message\":\"expected format \\\"yyyy-MM-dd HH:mm:ss\\\"\",\"endpoint\":\"/api/protected/users/journeys/1/cars/1\"}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/1/cars/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(badRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 5 Success throws Exception
     */
    @Test
    public void update_shared_ride_journey_should_return_ok_with_valid_journey_response_dto() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("Good Luck");
        personalAgencyRepository.save(personalAgency);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("Mulongo Oscar");
        sharedRide.setIsCarApproved(true);
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setLicensePlateNumber("SW1920");


        user.setPersonalAgency(personalAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Tiko Park");
        location.setCity("Tiko");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        location.setCity("Limbe");
        location.setAddress("Limbe Mile 4 park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        location.setCity("Limbe");
        location.setAddress("Half Mile");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location);
        transitAndStopRepository.save(transitAndStop2);
        location.setCity("Tole");
        location.setAddress("Longstreet");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String updatedDateTime = localDateTime.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sharedRide.setCreatedAt(TimeProviderTestUtil.now());
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop);
        journey.setDestination(transitAndStop1);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);

        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(sharedRide);
        journeyRepository.save(journey);
        String expectedResponse = "{\"id\":" + journey.getId() + ",\"departureTime\":\"" + updatedDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\":1000.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":" + transitAndStop1.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Limbe\"," +
                "\"address\":\"Limbe Mile 4 park\"" +
                "}," +
                "\"destination\":{" +
                "\"id\":" + transitAndStop.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Tiko\"," +
                "\"address\":\"Tiko Park\"," +
                "\"amount\":1000.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":" + transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Tole\"," +
                "\"address\":\"Longstreet\"," +
                "\"amount\":1000.0" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Limbe\"," +
                "\"address\":\"Half Mile\"," +
                "\"amount\":2000.0" +
                "}" +
                "]," +
                "\"car\":{" +
                "\"id\":" + sharedRide.getId() + "," +
                "\"name\":\"Mulongo Oscar\"," +
                "\"licensePlateNumber\":\"SW1920\"," +
                "\"isOfficialAgencyIndicator\":false," +
                "\"isCarApproved\":true," +
                "\"timestamp\":\"" + currentDateTime + "\"" +
                "}}";
        String reqBody = "{\n" +
                "  \"departureTime\": \"" + updatedDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop1.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop3.getId() + ", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop2.getId() + ", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/" + journey.getId() + "/cars/" + sharedRide.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * **USERS** can GET  a Journey for OfficialAgency
     * #169528470
     * Scenario: 3 Get Journey Success
     */
    @Test
    public void personal_agency_get_journey_should_throw_car_not_in_personal_agency_api_exception() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("Malingo Boy");
        personalAgencyRepository.save(personalAgency);

        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("Oboy");
        sharedRide.setPersonalAgency(personalAgency);
        sharedRide.setLicensePlateNumber("123SW");
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setIsCarApproved(true);

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        Location location = new Location();
        location.setAddress("Tiko Park");
        location.setCity("Tiko");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Mutengene");
        location1.setAddress("Mutengene Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Mile 14");
        location2.setAddress("Mile 14 Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Mile 17");
        location3.setAddress("Mile 17 main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sharedRide.setCreatedAt(TimeProviderTestUtil.now());
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);
        journey.setJourneyStops(journeyStops);
        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(sharedRide);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/users/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andReturn();
    }

    /**
     * **USERS**  can view all Journey for PersonalAgency ordered by date and arrivalIndicator
     * * #169528531
     * Scenario: 2.  Journeys found for user's Agency
     * Given journeys exit
     */
    @Test
    public void Journeys_found_for_user_Agency_Given_journeys_exit() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("Malingo Boy");
        personalAgencyRepository.save(personalAgency);

        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("Oboy");
        sharedRide.setPersonalAgency(personalAgency);
        sharedRide.setLicensePlateNumber("123SW");
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setIsCarApproved(true);

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        Location location = new Location();
        location.setAddress("Tikos Park");
        location.setCity("Tikos");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("Mutengenes");
        location1.setAddress("Mutengenes Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("Miles 14");
        location2.setAddress("Miles 14 Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("Miles 17");
        location3.setAddress("Miles 17 main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sharedRide.setCreatedAt(TimeProviderTestUtil.now());
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);
        journey.setJourneyStops(journeyStops);
        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(sharedRide);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/protected/users/journeys/")
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andReturn();
    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  4. change  Journey departureIndicator state
     */
    @Test
    public void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_false_and_Journey_car_is_IN_AuthUser_Agency_then_change_departure_state() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgencyRepository.save(personalAgency);

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setCar(sharedRide);
        journeyRepository.save(journey);
        String reqBody = "{\"departureIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/" + journey.getId() + "/departure")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .content(reqBody);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    /**
     * **USERS** can change arrivalIndicator state
     * #169528624
     * Scenario:  4. change  Journey arrivalIndicator state
     */
    @Test
    public void given_journeyId_passed_as_parameter_exist_and_journey_departureIndicator_true_and_Journey_car_is_in_AuthUser_Agency_then_change_departure_state() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgencyRepository.save(personalAgency);

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureIndicator(true);
        journey.setArrivalIndicator(false);
        journey.setCar(sharedRide);
        journeyRepository.save(journey);
        String reqBody = "{\"arrivalIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/" + journey.getId() + "/arrival")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(reqBody);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();

    }

    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     * Scenario:  4. Delete Journey Success
     */
    @Test
    public void delete_shared_journey_should_delete_journey_and_return_204() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgencyRepository.save(personalAgency);

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setDepartureIndicator(true);
        journey.setArrivalIndicator(false);
        journey.setCar(sharedRide);
        journeyRepository.save(journey);
        RequestBuilder requestBuilder = delete("/api/protected/users/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder).andExpect(status().isNoContent()).andReturn();


    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * Scenario:  5. Add Journey Success
     * #169528838
     */
    @Test
    public void add_stops_to_personal_agency_should_return_204_no_content() throws Exception {

        PersonalAgency personalAgency = new PersonalAgency();
        personalAgencyRepository.save(personalAgency);
        SharedRide sharedRide = new SharedRide();

        user.setPersonalAgency(personalAgency);
        userRepository.save(user);

        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);

        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop);
        journeyStop.setJourney(journey);
        journey.setJourneyStops(Collections.singletonList(journeyStop));
        journey.setCar(sharedRide);
        journeyRepository.save(journey);
        String reqBody = "{\"transitAndStopId\": " + transitAndStop.getId() + ", \"amount\" : 1000.0 }";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/" + journey.getId() + "/add_stops")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();


    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 2. Success search with input data passeds
     */
    @Test
    public void return_a_list_of_journeyDto_on_successful_search() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Malingo Major");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Kumba One Chances");
        bus.setNumberOfSeats(3);
        bus.setIsCarApproved(true);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123454387");

        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        Location location = new Location();
        location.setAddress("Mile 17 Motto Park");
        location.setCity("koke");
        location.setState("South West");
        location.setCountry("Cameroon");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        Location location1 = new Location();
        location1.setState("South West");
        location1.setCountry("Cameroon");
        location1.setCity("bova");
        location1.setAddress("Buea Road Motor Park");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);
        Location location2 = new Location();
        location2.setState("South West");
        location2.setCountry("Cameroon");
        location2.setCity("mamu");
        location2.setAddress("Muyuka Main Park");
        TransitAndStop transitAndStop2 = new TransitAndStop();
        transitAndStop2.setLocation(location2);
        transitAndStopRepository.save(transitAndStop2);
        Location location3 = new Location();
        location3.setState("South West");
        location3.setCountry("Cameroon");
        location3.setCity("mautu");
        location3.setAddress("Ekona Main Park");
        TransitAndStop transitAndStop3 = new TransitAndStop();
        transitAndStop3.setLocation(location3);
        transitAndStopRepository.save(transitAndStop3);

        TimeProviderTestUtil.useFixedClockAt(LocalDateTime.now());
        ZonedDateTime localDateTime = TimeProviderTestUtil.now().atZone(ZoneId.of("GMT"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String currentShortDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        bus.setCreatedAt(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1);
        journey.setDestination(transitAndStop);
        journey.setDepartureTime(localDateTime.toLocalDateTime());
        journey.setEstimatedArrivalTime(localDateTime.toLocalDateTime());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);

        JourneyStop journeyStop = new JourneyStop();
        journeyStop.setTransitAndStop(transitAndStop2);
        journeyStop.setAmount(1500);
        journeyStop.setJourney(journey);
        JourneyStop journeyStop1 = new JourneyStop();
        journeyStop1.setTransitAndStop(transitAndStop3);
        journeyStop1.setJourney(journey);
        journeyStop1.setAmount(500);
        List<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);

        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setCreatedAt(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);

        RequestBuilder requestBuilder = get("/api/public/journey/search/departure/" + transitAndStop1.getId() + "/destination/" + transitAndStop.getId() + "?time=" + currentShortDateTime)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * AGENCY_MANAGER or AGENCY_OPERATOR** can remove STOPS
     * or Updating Journey  for Journey in their OfficialAgency  if  arrivalIndicator = false and NO booking
     * #169112817
     * Scenario: 5. Remove Journey Stops Success
     */
    @Test
    public void given_journey_has_no_booking_for_transit_and_stop_then_remove_transit_and_stop() throws Exception {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        OfficialAgency agency = officialAgencyRepository.save(officialAgency);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main");
        agencyBranch.setOfficialAgency(agency);
        AgencyBranch branch = agencyBranchRepository.save(agencyBranch);
        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(branch);
        userRepository.save(user);
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);

        Location location1 = new Location();
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop.setLocation(location1);
        transitAndStopRepository.save(transitAndStop1);

        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(branch);

        JourneyStop journeyStop = new JourneyStop(journey, transitAndStop, 2000.0);
        JourneyStop journeyStop1 = new JourneyStop(journey, transitAndStop1, 300.0);
        journey.setJourneyStops(new ArrayList<>(Arrays.asList(journeyStop, journeyStop1)));
        journeyRepository.save(journey);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setDestination(transitAndStop);
        bookedJourney.setJourney(journey);
        bookedJourneyRepository.save(bookedJourney);

        Journey journey1 = new Journey();
        journey1.setDepartureIndicator(false);
        journey1.setArrivalIndicator(false);
        JourneyStop journeyStop2 = new JourneyStop(journey1, transitAndStop1, 2500.0);
        journey1.setJourneyStops(Collections.singletonList(journeyStop2));
        journeyRepository.save(journey1);
        BookedJourney bookedJourney1 = new BookedJourney();
        bookedJourney1.setDestination(transitAndStop1);
        bookedJourney1.setJourney(journey1);
        bookedJourneyRepository.save(bookedJourney1);

        RequestBuilder requestBuilder = delete("/api/protected/agency/journeys/" + journey.getId() + "/transitAndStops/" + transitAndStop1.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void searchJourney_makesJourney_search() throws Exception {

        RequestBuilder requestBuilder = get("/api/protected/journey/search")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
    }

}
