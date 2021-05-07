package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.Location;
import net.gogroups.gowaka.domain.model.TransitAndStop;
import net.gogroups.gowaka.domain.repository.TransitAndStopRepository;
import net.gogroups.gowaka.dto.LocationDTO;
import net.gogroups.gowaka.dto.LocationResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Arrays;
import java.util.Collections;

import static net.gogroups.gowaka.TestUtils.createToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Nnouka Stephen
 * @date 08 Oct 2019
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class TransitAndStopControllerIntegrationTest {

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    @Autowired
    private TransitAndStopRepository transitAndStopRepository;

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    @AfterEach
    void tearDown() {
        transitAndStopRepository.deleteAll();
    }

    @Test
    public void transit_should_return_201_created_status() throws Exception{
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        locationDTO.setTlaAddress("ML");
        locationDTO.setTlaCity("BUE");
        locationDTO.setTlaState("SWR");
        locationDTO.setTlaCountry("CMR");
        RequestBuilder requestBuilder = post("/api/protected/location")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void transit_should_return_400_bad_request() throws Exception {

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        locationDTO.setTlaAddress("ML");
        locationDTO.setTlaCity("BUE");
        locationDTO.setTlaState("SWR");
        locationDTO.setTlaCountry("CMR");
        RequestBuilder requestBuilder = post("/api/protected/location")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.[0].field").value("country"))
                .andExpect(jsonPath("$.errors.[0].message").value("country is required"));
    }

    @Test
    public void transit_should_update_and_return_204_no_content() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        locationDTO.setTlaAddress("ML");
        locationDTO.setTlaCity("BUE");
        locationDTO.setTlaState("SWR");
        locationDTO.setTlaCountry("CMR");
        Location location = new Location();
        location.setAddress("Long Street");
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        RequestBuilder requestBuilder = post("/api/protected/location/" + transitAndStop.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void transit_update_should_return_400_bad_request() throws Exception {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        locationDTO.setTlaAddress("ML");
        locationDTO.setTlaCity("BUE");
        locationDTO.setTlaState("SWR");
        locationDTO.setTlaCountry("CMR");
        RequestBuilder requestBuilder = post("/api/protected/location/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.[0].field").value("country"))
                .andExpect(jsonPath("$.errors.[0].message").value("country is required"));
    }

    @Test
    public void transit_update_should_return_409_conflict_transit_already_in_use() throws Exception {

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setState("SW");
        locationDTO.setCountry("CMR");
        locationDTO.setAddress("Malingo");
        locationDTO.setCity("Buea");
        locationDTO.setTlaAddress("ML");
        locationDTO.setTlaCity("BUE");
        locationDTO.setTlaState("SWR");
        locationDTO.setTlaCountry("CMR");
        Location location = new Location();
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        location.setTlaAddress("ML");
        location.setTlaCity("BUE");
        location.setTlaState("SWR");
        location.setTlaCountry("CMR");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        String badResponse = "{\n" +
                "  \"code\": \"TRANSIT_AND_STOP_ALREADY_IN_USE\",\n" +
                "  \"message\": \"TransitAndStop already Exists\",\n" +
                "  \"endpoint\": \"/api/protected/location/" + transitAndStop.getId() + "\"\n" +
                "}";
        RequestBuilder requestBuilder = post("/api/protected/location/" + transitAndStop.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(locationDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().json(badResponse))
                .andReturn();
    }
    @Test
    public void transit_delete_should_delete_and_return_204_no_content() throws Exception {
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStopRepository.save(transitAndStop);
        RequestBuilder requestBuilder = delete("/api/protected/location/" + transitAndStop.getId())
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }
    @Test
    public void get_all_transit_and_stop_locations_should_return_200_with_location_list() throws Exception{
        Location location = new Location();
        location.setAddress("Malingo");
        location.setCity("Buea");
        location.setState("SW");
        location.setCountry("CMR");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
        locationResponseDTO.setId(transitAndStop.getId());
        locationResponseDTO.setCountry(location.getCountry());
        locationResponseDTO.setCity(location.getCity());
        locationResponseDTO.setAddress(location.getAddress());
        locationResponseDTO.setState(location.getState());
        location.setAddress("Bokova");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        LocationResponseDTO locationResponseDTO1 = new LocationResponseDTO();
        locationResponseDTO1.setCity(location.getCity());
        locationResponseDTO1.setId(transitAndStop1.getId());
        locationResponseDTO1.setCountry(location.getCountry());
        locationResponseDTO1.setCity(location.getCity());
        locationResponseDTO1.setAddress(location.getAddress());
        locationResponseDTO1.setState(location.getState());
        RequestBuilder requestBuilder = get("/api/public/location/")
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper()
                        .writeValueAsString(Arrays.asList(locationResponseDTO, locationResponseDTO1))))
                .andReturn();
    }

    @Test
    public void get_all_transit_and_stop_locations_should_return_empty_list() throws Exception {
        RequestBuilder requestBuilder = get("/api/public/location/")
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper()
                        .writeValueAsString(Collections.emptyList())))
                .andReturn();
    }

    @Test
    public void search_transit_and_stop_by_location_should_return_empty_list() throws Exception {
        Location location = new Location();
        location.setAddress("Malingo");
        location.setCity("Mamfe");
        location.setState("SW");
        location.setCountry("CMR");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        RequestBuilder requestBuilder = get("/api/public/location/search?city=ngkongsamba")
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper()
                        .writeValueAsString(Collections.emptyList())))
                .andReturn();
    }

    @Test
    public void search_transit_and_stop_by_location_should_return_location_list() throws Exception {
        Location location = new Location();
        location.setAddress("Malingo");
        location.setCity("Douala");
        location.setState("SW");
        location.setCountry("CMR");
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStopRepository.save(transitAndStop);
        LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
        locationResponseDTO.setId(transitAndStop.getId());
        locationResponseDTO.setCountry(location.getCountry());
        locationResponseDTO.setCity(location.getCity());
        locationResponseDTO.setAddress(location.getAddress());
        locationResponseDTO.setState(location.getState());
        location.setAddress("Bokova");
        TransitAndStop transitAndStop1 = new TransitAndStop();
        transitAndStop1.setLocation(location);
        transitAndStopRepository.save(transitAndStop1);
        LocationResponseDTO locationResponseDTO1 = new LocationResponseDTO();
        locationResponseDTO1.setCity(location.getCity());
        locationResponseDTO1.setId(transitAndStop1.getId());
        locationResponseDTO1.setCountry(location.getCountry());
        locationResponseDTO1.setCity(location.getCity());
        locationResponseDTO1.setAddress(location.getAddress());
        locationResponseDTO1.setState(location.getState());
        RequestBuilder requestBuilder = get("/api/public/location/search?city=DouAlA")
                .header("Authorization", "Bearer " + jwtToken);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper()
                        .writeValueAsString(Arrays.asList(locationResponseDTO, locationResponseDTO1))))
                .andReturn();
    }
}
