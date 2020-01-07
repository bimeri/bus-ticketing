package net.gowaka.gowaka.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.TimeProviderTestUtil;
import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.*;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ErrorCodes;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static net.gowaka.gowaka.TestUtils.createToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
@ActiveProfiles("test")
public class CarControllerIntegrationTest {

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
                            BusResponseDTO busResponseDTO = new BusResponseDTO();
                            busResponseDTO.setId(officialAgencyBus.getId());
                            busResponseDTO.setNumberOfSeats(officialAgencyBus.getNumberOfSeats());
                            busResponseDTO.setLicensePlateNumber(officialAgencyBus.getLicensePlateNumber());
                            busResponseDTO.setName(officialAgencyBus.getName());
                            return busResponseDTO;
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
                            SharedRideResponseDTO sharedRideResponseDTO = new SharedRideResponseDTO();
                            sharedRideResponseDTO.setName(sharedRides.getName());
                            sharedRideResponseDTO.setId(sharedRides.getId());
                            return sharedRideResponseDTO;
                        }
                ).collect(Collectors.toList()))))
                .andReturn();
    }

    @Test
    public void approve_should_return_204() throws Exception {
        ApproveCarDTO approveCarDTO = new ApproveCarDTO();
        approveCarDTO.setApprove(true);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("happi");
        carRepository.save(sharedRide);
        RequestBuilder requestBuilder = post("/api/protected/car/" + sharedRide.getId() + "/approve")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(approveCarDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void gw_admin_get_unapproved_cars_should_return_200_with_car_list() throws Exception {

        PersonalAgency personalAgency = new PersonalAgency();
        personalAgency.setName("Homer home");
        PersonalAgency personalAgency1 = new PersonalAgency();
        personalAgency1.setName("My agency");
        personalAgencyRepository.save(personalAgency);
        personalAgencyRepository.save(personalAgency1);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setName("H1");
        sharedRide.setPersonalAgency(personalAgency);
        sharedRide.setIsCarApproved(false);
        SharedRide sharedRide1 = new SharedRide();
        sharedRide1.setName("H2");
        sharedRide1.setIsCarApproved(false);
        sharedRide1.setPersonalAgency(personalAgency);
        SharedRide sharedRide2 = new SharedRide();
        sharedRide2.setName("H3");
        sharedRide2.setIsCarApproved(true);
        sharedRide2.setPersonalAgency(personalAgency1);
        carRepository.save(sharedRide);
        carRepository.save(sharedRide1);
        carRepository.save(sharedRide2);
        user.setPersonalAgency(personalAgency);
        userRepository.save(user);
        RequestBuilder requestBuilder = get("/api/protected/cars/unapproved")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"name\":\"H1\",\"licensePlateNumber\":null,\"isOfficialAgencyIndicator\":null,\"isCarApproved\":false,\"timestamp\":null},{\"id\":2,\"name\":\"H2\",\"licensePlateNumber\":null,\"isOfficialAgencyIndicator\":null,\"isCarApproved\":false,\"timestamp\":null}]"))
                .andReturn();
    }

    @Test
    public void gw_admin_search_should_return_200_ok_status_code_with_responseCarDTO() throws Exception {
        SharedRide sharedRide = new SharedRide();
        sharedRide.setLicensePlateNumber("1234");
        sharedRide.setName("H3");
        sharedRide.setIsCarApproved(true);
        sharedRide.setIsOfficialAgencyIndicator(false);
        carRepository.save(sharedRide);
        CarDTO carDTO = new CarDTO();
        carDTO.setId(sharedRide.getId());
        carDTO.setName(sharedRide.getName());
        carDTO.setLicensePlateNumber(sharedRide.getLicensePlateNumber());
        carDTO.setIsCarApproved(sharedRide.getIsCarApproved());
        carDTO.setIsOfficialAgencyIndicator(sharedRide.getIsOfficialAgencyIndicator());

        RequestBuilder requestBuilder = get("/api/protected/car/search?licensePlateNumber=" + sharedRide.getLicensePlateNumber())
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(carDTO)))
                .andReturn();
    }
    /**
     * #170426654
     * Update Agency Car Information
     * Scenario: 1. Car already has journey booked
     */
    @Test
    public void update_car_should_update_car_and_return_no_content() throws Exception {
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("GG Express");
        officialAgencyRepository.save(officialAgency);
        Bus bus = new Bus();
        bus.setName("Te widikum");
        bus.setNumberOfSeats(3);
        bus.setOfficialAgency(officialAgency);
        carRepository.save(bus);
        user.setOfficialAgency(officialAgency);
        userRepository.save(user);
        String expectedRequest = "{\n" +
                "  \"licensePlateNumber\": \"123SW\",\n" +
                "  \"name\": \"70 Seater\",\n" +
                "  \"numberOfSeats\": 70\n" +
                "}";
        RequestBuilder request = post("/api/protected/agency/car/" + bus.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(expectedRequest)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isNoContent())
                .andReturn();
    }

}
