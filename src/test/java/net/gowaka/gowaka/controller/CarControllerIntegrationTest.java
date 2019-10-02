package net.gowaka.gowaka.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.CarService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
public class CarControllerIntegrationTest {

    @Value("${security.jwt.token.secretKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("apiSecurityRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private User user;

    private MockRestServiceServer mockServer;

    @Mock
    private CarService mockCarService;


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

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);
        startMockServerWith("http://localhost:8082/api/protected/v1/users?username=admin@example.com",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"10\",\n" +
                        "  \"fullName\":\"Agency User\",\n" +
                        "  \"username\": \"admin@example.com\",\n" +
                        "  \"email\": \"admin@example.com\",\n" +
                        "  \"roles\":\"USERS;\"\n" +
                        "}");

        startMockServerWith("http://localhost:8082/api/protected/v1/users/10/ROLES?value=USERS;AGENCY_ADMIN",
                HttpStatus.NO_CONTENT, "");
        jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    @After
    public void tearDown() throws Exception {
        mockServer.reset();
    }


    private void startMockServerWith(String url, HttpStatus status, String response) {
        mockServer.expect(requestTo(url))
                .andRespond(withStatus(status).body(response).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void official_agency_add_bus_should_return_200_ok_status_code_with_responseBusDTO() throws Exception {
        BusDTO busDTO = new BusDTO();
        busDTO.setLicensePlateNumber("12345LT");
        busDTO.setNumberOfSeats(5);
        busDTO.setName("Malingo Royal");

        RequestBuilder requestBuilder = post("/api/protected/agency/car")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(busDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty()) // could not use json(expectedResponse)) since id is autogenerated
                .andExpect(jsonPath("$.name").value(busDTO.getName()))
                .andExpect(jsonPath("$.licensePlateNumber").value(busDTO.getLicensePlateNumber()))
                .andExpect(jsonPath("$.numberOfSeats").value(busDTO.getNumberOfSeats()))
                .andReturn();
    }
    @Test
    public void official_agency_should_return_400_with_validation_error() throws Exception {
        BusDTO busDTO = new BusDTO();
        busDTO.setName("Malingo Royal");

        RequestBuilder requestBuilder = post("/api/protected/agency/car")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(busDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR.toString()))
                .andReturn();
    }

    @Test
    public void personal_agency_add_sharedRide_should_return_200_ok_status_code_with_responseSharedRideDTO() throws Exception {
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        sharedRideDTO.setCarOwnerIdNumber("12345");
        sharedRideDTO.setName("Danfo Driver");
        sharedRideDTO.setCarOwnerName("Ndifor Fuh");
        sharedRideDTO.setLicensePlateNumber("1245NW");

        RequestBuilder requestBuilder = post("/api/protected/user/car")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(sharedRideDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty()) // could not use json(expectedResponse)) since id is autogenerated
                .andExpect(jsonPath("$.name").value(sharedRideDTO.getName()))
                .andExpect(jsonPath("$.licensePlateNumber").value(sharedRideDTO.getLicensePlateNumber()))
                .andExpect(jsonPath("$.carOwnerName").value(sharedRideDTO.getCarOwnerName()))
                .andExpect(jsonPath("$.carOwnerIdNumber").value(sharedRideDTO.getCarOwnerIdNumber()))
                .andReturn();
    }

    @Test
    public void personal_agency_should_return_400_with_validation_error() throws Exception {
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        sharedRideDTO.setName("Danfo Driver");

        RequestBuilder requestBuilder = post("/api/protected/agency/car")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(sharedRideDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR.toString()))
                .andReturn();
    }

}
