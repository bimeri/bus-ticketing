package net.gowaka.gowaka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.domain.model.Location;
import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.LocationDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static net.gowaka.gowaka.TestUtils.createToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
/**
 * @author Nnouka Stephen
 * @date 08 Oct 2019
 */
public class TransitAndStopControllerIntegrationTest {
    @Value("${security.jwt.token.secretKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransitAndStopRepository transitAndStopRepository;

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("apiSecurityRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Test
    public void transit_should_return_201_created_status() throws Exception{
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        RequestBuilder requestBuilder = post("/api/protected/location")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void transit_should_return_400_bad_request() throws Exception {
        String badResponse = "{\n" +
                "  \"code\": \"VALIDATION_ERROR\",\n" +
                "  \"message\": \"MethodArgumentNotValidException: #country @errors.\",\n" +
                "  \"endpoint\": \"/api/protected/location\",\n" +
                "  \"errors\": {\n" +
                "    \"country\": \"country is required\"\n" +
                "  }\n" +
                "}";
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        RequestBuilder requestBuilder = post("/api/protected/location")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(badResponse))
                .andReturn();
    }

    @Test
    public void transit_should_update_and_return_204_no_content() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        Location location = new Location();
        location.setAddress("Long Street");
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        RequestBuilder requestBuilder = post("/api/protected/location/" + transitAndStop.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void transit_update_should_return_400_bad_request() throws Exception {
        String badResponse = "{\n" +
                "  \"code\": \"VALIDATION_ERROR\",\n" +
                "  \"message\": \"MethodArgumentNotValidException: #country @errors.\",\n" +
                "  \"endpoint\": \"/api/protected/location/1\",\n" +
                "  \"errors\": {\n" +
                "    \"country\": \"country is required\"\n" +
                "  }\n" +
                "}";
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        RequestBuilder requestBuilder = post("/api/protected/location/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(badResponse))
                .andReturn();
    }

    @Test
    public void transit_update_should_and_return_409_conflict_transit_already_in_use() throws Exception {

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        Location location = new Location();
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        String badResponse = "{\n" +
                "  \"code\": \"TRANSIT_AND_STOP_ALREADY_IN_USE\",\n" +
                "  \"message\": \"TransitAndStop already Exists\",\n" +
                "  \"endpoint\": \"/api/protected/location/" + transitAndStop.getId() + "\"\n" +
                "}";
        RequestBuilder requestBuilder = post("/api/protected/location/" + transitAndStop.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().json(badResponse))
                .andReturn();
    }
}
