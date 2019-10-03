package net.gowaka.gowaka.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.PersonalAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.ResponseSharedRideDTO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static net.gowaka.gowaka.TestUtils.createToken;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private OfficialAgencyRepository officialAgencyRepository;

    @Autowired
    private PersonalAgencyRepository personalAgencyRepository;

    @Autowired
    private CarRepository carRepository;

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
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName(user.getUserId());
        user.setOfficialAgency(officialAgencyRepository.save(officialAgency));
        userRepository.save(user);
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
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName(user.getUserId());
        user.setPersonalAgency(personalAgencyRepository.save(personalAgency));
        userRepository.save(user);

        RequestBuilder requestBuilder = post("/api/protected/users/car")
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

        RequestBuilder requestBuilder = post("/api/protected/users/car")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(sharedRideDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR.toString()))
                .andReturn();
    }

    @Test
    public void official_agency_get_all_buses_should_return_200_ok_with_responseBusDTOList() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Te widikum");
        bus.setNumberOfSeats(3);
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);
        Bus bus1 = new Bus();
        bus1.setName("Fly way");
        bus1.setNumberOfSeats(7);
        bus1.setOfficialAgency(officialAgency);
        carRepository.save(bus1);
        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        RequestBuilder requestBuilder = get("/api/protected/agency/car")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString( new ArrayList<>(Arrays.asList(bus, bus1)).stream().map(
                        officialAgencyBus -> {
                            ResponseBusDTO responseBusDTO = new ResponseBusDTO();
                            responseBusDTO.setId(officialAgencyBus.getId());
                            responseBusDTO.setNumberOfSeats(officialAgencyBus.getNumberOfSeats());
                            responseBusDTO.setLicensePlateNumber(officialAgencyBus.getLicensePlateNumber());
                            responseBusDTO.setName(officialAgencyBus.getName());
                            return responseBusDTO;
                        }
                ).collect(Collectors.toList()))))
                .andReturn();
    }

    @Test
    public void personal_agency_get_shared_rides_should_return_200_with_shared_ride_list() throws Exception {
        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("Homer home");
        personalAgencyRepository.save(personalAgency);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("H1");
        sharedRide.setPersonalAgency(personalAgency);
        SharedRide sharedRide1 = new SharedRide();
        sharedRide1.setName("H2");
        sharedRide1.setPersonalAgency(personalAgency);
        carRepository.save(sharedRide);
        carRepository.save(sharedRide1);
        user.setPersonalAgency(personalAgency);
        userRepository.save(user);
        RequestBuilder requestBuilder = get("/api/protected/users/car")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString( new ArrayList<>(Arrays.asList(sharedRide, sharedRide1))
                        .stream().map(
                        sharedRides -> {
                            ResponseSharedRideDTO responseSharedRideDTO = new ResponseSharedRideDTO();
                            responseSharedRideDTO.setName(sharedRides.getName());
                            responseSharedRideDTO.setId(sharedRides.getId());
                            return responseSharedRideDTO;
                        }
                ).collect(Collectors.toList()))))
                .andReturn();
    }

}
