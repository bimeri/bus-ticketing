package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.TimeProviderTestUtil;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

import static net.gowaka.gowaka.TestUtils.createToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
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
    private MockMvc mockMvc;

    @Qualifier("apiSecurityRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private User user;

    private MockRestServiceServer mockServer;


    private String successClientTokenResponse = "{\n" +
            "  \"header\": \"Authorization\",\n" +
            "  \"type\": \"Bearer\",\n" +
            "  \"issuer\": \"API-Security\",\n" +
            "  \"version\": \"v1\",\n" +
            "  \"token\": \"jwt-token\"\n" +
            "}";
    private String jwtToken;

    @Before
    public void setUp() throws Exception {

        mockServer = MockRestServiceServer.createServer(restTemplate);
        User newUser = new User();
        newUser.setUserId("12");
        newUser.setTimestamp(LocalDateTime.now());

        this.user = userRepository.save(newUser);

        jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    @AfterClass
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
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setTimestamp(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);
        String expectedResponse = "{\"id\":1,\"departureTime\":\"" + currentDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\": 1000.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":"+ transitAndStop.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Buea\"," +
                "\"address\":\"Mile 17 Motto Park\"" +
                "}," +
                "\"destination\":{" +
                "\"id\":" + transitAndStop1.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Kumba\"," +
                "\"address\":\"Buea Road Motor Park\"," +
                "\"amount\":1000.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":"+ transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Muyuka\"," +
                "\"address\":\"Muyuka Main Park\"," +
                "\"amount\": 1000" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Ekona\"," +
                "\"address\":\"Ekona Main Park\"," +
                "\"amount\": 2000 }" +
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
                "  \"departureTime\": \"" + currentDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop1.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop2.getId()+", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop3.getId()+", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/cars/" + bus.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void add_journey_should_return_validation_date_time_error() throws Exception{
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
     * @throws Exception
     */
    @Test
    public void update_journey_should_return_ok_with_valid_journey_response_dto() throws Exception {
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
        String updatedDateTime = localDateTime.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String currentDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bus.setTimestamp(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop.getLocation());
        journey.setDestination(transitAndStop1.getLocation());
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
        Set<JourneyStop> journeyStops = journey.getJourneyStops();
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
                "\"id\":"+ transitAndStop1.getId() + "," +
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
                "\"id\":"+ transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Ekona\"," +
                "\"address\":\"Ekona Main Park\"," +
                "\"amount\":500.0" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Muyuka\"," +
                "\"address\":\"Muyuka Main Park\"," +
                "\"amount\":1500.0" +
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
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop3.getId()+", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop2.getId()+", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/"+ journey.getId() + "/cars/" + bus.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
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
        bus.setTimestamp(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1.getLocation());
        journey.setDestination(transitAndStop.getLocation());
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
        Set<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);


        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setTimestamp(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);
        String expectedResponse = "[{\"id\":" + journey.getId() + ",\"departureTime\":\"" + currentDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\": 0.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":"+ transitAndStop1.getId() + "," +
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
                "\"amount\": 0.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":"+ transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Ekona\"," +
                "\"address\":\"Ekona Main Park\"," +
                "\"amount\":500.0" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Muyuka\"," +
                "\"address\":\"Muyuka Main Park\"," +
                "\"amount\":1500.0" +
                "}" +
                "]," +
                "\"car\":{" +
                "\"id\":" + bus.getId() + "," +
                "\"name\":\"Kumba One Chances\"," +
                "\"licensePlateNumber\":\"123454387\"," +
                "\"isOfficialAgencyIndicator\":true," +
                "\"isCarApproved\":true," +
                "\"timestamp\":\"" + currentDateTime + "\"" +
                "}}]";

        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();
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
        bus.setTimestamp(TimeProviderTestUtil.now());
        bus.setOfficialAgency(officialAgency);
        bus = carRepository.save(bus);

        Journey journey = new Journey();
        journey.setDepartureLocation(transitAndStop1.getLocation());
        journey.setDestination(transitAndStop.getLocation());
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
        Set<JourneyStop> journeyStops = journey.getJourneyStops();
        journeyStops.add(journeyStop);
        journeyStops.add(journeyStop1);
        journey.setJourneyStops(journeyStops);
        Driver driver = new Driver();
        driver.setDriverName("John Doe");
        driver.setDriverLicenseNumber("1234567899");
        journey.setDriver(driver);
        journey.setCar(bus);
        journey.setTimestamp(localDateTime.toLocalDateTime());
        journeyRepository.save(journey);
        String expectedResponse = "{\"id\":" + journey.getId() + ",\"departureTime\":\"" + currentDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\": 0.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":"+ transitAndStop1.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Kumba\"," +
                "\"address\":\"Fiango Motor Park\"" +
                "}," +
                "\"destination\":{" +
                "\"id\":" + transitAndStop.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Buea\"," +
                "\"address\":\"Tole Park\"," +
                "\"amount\": 0.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":"+ transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Ekona\"," +
                "\"address\":\"Small Park\"," +
                "\"amount\": 500.0" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Muyuka\"," +
                "\"address\":\"Munyenge Park\"," +
                "\"amount\": 1500.0" +
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
        RequestBuilder requestBuilder = get("/api/protected/agency/journeys/" + journey.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

    /**
     * #169112805
     * Scenario 5. Add Stop Success
     */
    @Test
    public void add_stops_should_return_204_no_content() throws Exception{
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();


        user.setOfficialAgency(officialAgency);
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
        journey.setJourneyStops(Collections.singleton(journeyStop));
        journey.setCar(bus);
        journeyRepository.save(journey);
        String reqBody = "{\"transitAndStopId\": " + transitAndStop.getId() + ", \"amount\" : 1000.0 }";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/add_stops")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
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
    public void delete_journey_should_delete_journey_successfully() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
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
        journeyRepository.save(journey);
        RequestBuilder requestBuilder = delete("/api/protected/agency/journeys/" + journey.getId() )
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
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
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
        journeyRepository.save(journey);
        String reqBody = "{\"departureIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/departure" )
                .contentType(MediaType.APPLICATION_JSON_UTF8)
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
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();

        user.setOfficialAgency(officialAgency);
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
        journeyRepository.save(journey);
        String reqBody = "{\"arrivalIndicator\": true}";
        RequestBuilder requestBuilder = post("/api/protected/agency/journeys/" + journey.getId() + "/arrival" )
                .contentType(MediaType.APPLICATION_JSON_UTF8)
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
        String badRequest = "{\n" +
                "  \"departureTime\": \"2019-02-20 11:11:11\",\n" +
                "  \"estimatedArrivalTime\": \"2019-02-20 11:11:11\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" + "\"departureLocation\": 10," +
                "  \"transitAndStops\": [{\"transitAndStopId\": 12,\"amount\": 600},{\"transitAndStopId\": 13,\"amount\": 700}]\n" +
                "}";
        String expectedResponse = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"MethodArgumentNotValidException: #destination @errors.\",\"endpoint\":\"/api/protected/users/journeys/cars/1\",\"errors\":{\"destination\":\"destination is required\"}}";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/cars/1")
                                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                                            .header("Authorization", "Bearer " + jwtToken)
                                            .content(badRequest)
                                            .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse))
                .andReturn();
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
        sharedRide.setTimestamp(TimeProviderTestUtil.now());
        sharedRide.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);
        String expectedResponse = "{\"id\":1,\"departureTime\":\"" + currentDateTime + "\"," +
                "\"estimatedArrivalTime\":\"" + currentDateTime + "\"," +
                "\"departureIndicator\":false," +
                "\"arrivalIndicator\":false," +
                "\"timestamp\":\"" + currentDateTime + "\"," +
                "\"amount\": 1000.0," +
                "\"driver\":{" +
                "\"driverName\":\"John Doe\"," +
                "\"driverLicenseNumber\":\"1234567899\"" +
                "}," +
                "\"departureLocation\":{" +
                "\"id\":"+ transitAndStop.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Konye\"," +
                "\"address\":\"Mile 17 Motto Park\"" +
                "}," +
                "\"destination\":{" +
                "\"id\":" + transitAndStop1.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Mabanda\"," +
                "\"address\":\"Mabanda Road Motor Park\"," +
                "\"amount\":1000.0" +
                "}," +
                "\"transitAndStops\":[" +
                "{" +
                "\"id\":"+ transitAndStop2.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\": \"South West\"," +
                "\"city\":\"Butu\"," +
                "\"address\":\"Butu German Park\"," +
                "\"amount\": 1000" +
                "}," +
                "{" +
                "\"id\":" + transitAndStop3.getId() + "," +
                "\"country\":\"Cameroon\"," +
                "\"state\":\"South West\"," +
                "\"city\":\"Matoh\"," +
                "\"address\":\"Matoh Backside Park\"," +
                "\"amount\": 2000 }" +
                "]," +
                "\"car\":{" +
                "\"id\":" + sharedRide.getId() + "," +
                "\"name\":\"Te widikum\"," +
                "\"licensePlateNumber\":\"1234568755\"," +
                "\"isOfficialAgencyIndicator\":false," +
                "\"isCarApproved\":true," +
                "\"timestamp\":\"" + currentDateTime + "\"" +
                "}}";
        String reqBody = "{\n" +
                "  \"departureTime\": \"" + currentDateTime + "\",\n" +
                "  \"estimatedArrivalTime\": \"" + currentDateTime + "\",\n" +
                "  \"driver\": {\n" +
                "    \"driverName\": \"John Doe\",\n" +
                "    \"driverLicenseNumber\": \"1234567899\"\n" +
                "  },\n" +
                "  \"departureLocation\": " + transitAndStop.getId() + ",\n" +
                "  \"destination\": {\"transitAndStopId\":" + transitAndStop1.getId() + ",\"amount\": 1000 }, \n" +
                "  \"transitAndStops\": [{\"transitAndStopId\":" + transitAndStop2.getId()+", \"amount\": 1000}, " +
                "{\"transitAndStopId\":" + transitAndStop3.getId()+", \"amount\": 2000}]\n" +
                "}\n";
        RequestBuilder requestBuilder = post("/api/protected/users/journeys/cars/" + sharedRide.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(reqBody)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(expectedResponse))
                .andReturn();
    }

}
