package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.OfficialAgency;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.OfficialAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.CreateOfficialAgencyDTO;
import net.gogroups.gowaka.dto.EmailDTO;
import net.gogroups.gowaka.dto.OfficialAgencyDTO;
import net.gogroups.gowaka.dto.OfficialAgencyUserRoleRequestDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;

import static net.gogroups.gowaka.TestUtils.createToken;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/18/19 7:24 PM <br/>
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class OfficialAgencyControllerIntegrationTest {


    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfficialAgencyRepository officialAgencyRepository;

    private User user;

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("ggClientRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private String successClientTokenResponse = "{\n" +
            "  \"header\": \"Authorization\",\n" +
            "  \"type\": \"Bearer\",\n" +
            "  \"issuer\": \"API-Security\",\n" +
            "  \"version\": \"v1\",\n" +
            "  \"token\": \"jwt-token\"\n" +
            "}";

    @Before
    public void setUp() throws Exception {

        mockServer = MockRestServiceServer.createServer(restTemplate);

        User newUser = new User();
        newUser.setUserId("12");
        newUser.setCreatedAt(LocalDateTime.now());

        this.user = userRepository.save(newUser);

    }

    @After
    public void tearDown() {
        mockServer.reset();
    }


    private void startMockServerWith(String url, HttpStatus status, String response) {
        mockServer.expect(requestTo(url))
                .andRespond(withStatus(status).body(response).contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    public void createOfficialAgency_success_return_200() throws Exception {

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



        User agencyAdminUser = new User();
        agencyAdminUser.setUserId("10");
        agencyAdminUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(agencyAdminUser);

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        createOfficialAgencyDTO.setAgencyRegistrationNumber("123456789");
        createOfficialAgencyDTO.setAgencyName("GG Express");
        createOfficialAgencyDTO.setAgencyAdminEmail("admin@example.com");

        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, new String[]{"USERS", "GW_ADMIN"});


        RequestBuilder requestBuilder = post("/api/protected/agency")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(createOfficialAgencyDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        User aUser = userRepository.findById("10").get();
        assertThat(aUser.getOfficialAgency()).isNotNull();

    }

    @Test
    public void updateAgency_success_204() throws Exception {

        User agencyAdminUser = new User();
        agencyAdminUser.setUserId("10");
        agencyAdminUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(agencyAdminUser);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Agency");
        officialAgency.setAgencyRegistrationNumber("reg");
        OfficialAgency officialAgency1 = officialAgencyRepository.save(officialAgency);

        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        officialAgency.setAgencyName("Agency2");
        officialAgency.setAgencyRegistrationNumber("reg2");

        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, new String[]{"USERS", "GW_ADMIN"});
        RequestBuilder requestBuilder = put("/api/protected/agency/"+officialAgency1.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(officialAgencyDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateAgencyLogo_success_204() throws Exception {

        User agencyAdminUser = new User();
        agencyAdminUser.setUserId("10");
        agencyAdminUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(agencyAdminUser);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("Agency");
        officialAgency.setAgencyRegistrationNumber("reg");
        OfficialAgency officialAgency1 = officialAgencyRepository.save(officialAgency);

        startMockServerWith("http://ggs2.space:9092/api/protected/files?bucketDirectory=GoWaka/agency_logos/"+officialAgency1.getId()+"&identifier=PROTECTED",
                HttpStatus.OK, "");

        MockMultipartFile file = new MockMultipartFile("file", "logo.png","multipart/form-data", "My Logo Content".getBytes());

        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, new String[]{"USERS", "GW_ADMIN"});
        RequestBuilder requestBuilder = multipart("/api/protected/agency/"+officialAgency1.getId()+"/logo")
                .file(file)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    public void getAllAgency_success_200() throws Exception {

        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, new String[]{"USERS", "GW_ADMIN"});
        RequestBuilder requestBuilder = get("/api/protected/agency")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void assignAgencyUserRole_success_return_200() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);
        startMockServerWith("http://localhost:8082/api/protected/v1/users/10",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"10\",\n" +
                        "  \"fullName\":\"Agency User\",\n" +
                        "  \"username\": \"user@example.com\",\n" +
                        "  \"email\": \"user@example.com\",\n" +
                        "  \"roles\":\"USERS;\"\n" +
                        "}");

        startMockServerWith("http://localhost:8082/api/protected/v1/users/10/ROLES?value=USERS;AGENCY_MANAGER;AGENCY_OPERATOR",
                HttpStatus.NO_CONTENT, "");

        User authUser = userRepository.findById("12").get();
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyName("My Agency");
        officialAgency.getUsers().add(authUser);
        authUser.setOfficialAgency(officialAgency);

        officialAgencyRepository.save(officialAgency);
        userRepository.save(authUser);

        User aUser = new User();
        aUser.setUserId("10");
        aUser.setCreatedAt(LocalDateTime.now());
        aUser.setOfficialAgency(officialAgency);
        userRepository.save(aUser);

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyUserRoleRequestDTO.setUserId("10");
        officialAgencyUserRoleRequestDTO.setRoles(Arrays.asList("AGENCY_MANAGER","AGENCY_OPERATOR"));

        String jwtToken = createToken("12", "agencyadmin@gg.com", "AG Admin", secretKey, new String[]{"USERS", "AGENCY_ADMIN"});

        String expectedResponse = "{\"id\":\"10\",\"fullName\":\"Agency User\",\"roles\":[\"USERS\",\"AGENCY_MANAGER\",\"AGENCY_OPERATOR\"]}";

        RequestBuilder requestBuilder = post("/api/protected/agency/user/role")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(officialAgencyUserRoleRequestDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void getAgencyUsers_success_return_200() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users/10",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"10\",\n" +
                        "  \"fullName\":\"Agency User10\",\n" +
                        "  \"username\": \"user1@example.com\",\n" +
                        "  \"email\": \"user@example.com\",\n" +
                        "  \"roles\":\"USERS;AGENCY_MANAGER\"\n" +
                        "}");
        startMockServerWith("http://localhost:8082/api/protected/v1/users/11",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"11\",\n" +
                        "  \"fullName\":\"Agency User11\",\n" +
                        "  \"username\": \"user@example.com\",\n" +
                        "  \"email\": \"user2@example.com\",\n" +
                        "  \"roles\":\"USERS;AGENCY_OPERATOR\"\n" +
                        "}");

        User authUser = userRepository.findById("12").get();
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyName("My Agency");
        officialAgency.getUsers().add(authUser);
        authUser.setOfficialAgency(officialAgency);

        officialAgencyRepository.save(officialAgency);
        userRepository.save(authUser);

        User aUser = new User();
        aUser.setUserId("10");
        aUser.setCreatedAt(LocalDateTime.now());
        aUser.setOfficialAgency(officialAgency);
        userRepository.save(aUser);

        User anotherUser = new User();
        anotherUser.setUserId("11");
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setOfficialAgency(officialAgency);
        userRepository.save(anotherUser);

        String jwtToken = createToken("12", "agencyadmin@gg.com", "AG Admin", secretKey, new String[]{"USERS", "AGENCY_ADMIN"});

        String expectedResponse = "[{\"id\":\"10\",\"fullName\":\"Agency User10\",\"roles\":[\"USERS\",\"AGENCY_MANAGER\"]},{\"id\":\"11\",\"fullName\":\"Agency User11\",\"roles\":[\"USERS\",\"AGENCY_OPERATOR\"]}]";

        RequestBuilder requestBuilder = get("/api/protected/agency/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void addAgencyUser_success_return_200() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users?username=user@example.com",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"10\",\n" +
                        "  \"fullName\":\"Agency User10\",\n" +
                        "  \"username\": \"user@example.com\",\n" +
                        "  \"email\": \"user@example.com\",\n" +
                        "  \"roles\":\"USERS;\"\n" +
                        "}");

        User authUser = userRepository.findById("12").get();
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyName("My Agency");
        officialAgency.getUsers().add(authUser);
        authUser.setOfficialAgency(officialAgency);

        officialAgencyRepository.save(officialAgency);
        userRepository.save(authUser);

        User aUser = new User();
        aUser.setUserId("10");
        aUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(aUser);


        String jwtToken = createToken("12", "agencyadmin@gg.com", "AG Admin", secretKey, new String[]{"USERS", "AGENCY_ADMIN"});

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("user@example.com");
        String expectedResponse = "{\"id\":\"10\",\"fullName\":\"Agency User10\",\"roles\":[\"USERS\"]}";

        RequestBuilder requestBuilder = post("/api/protected/agency/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void removeAgencyUser_success_return_204() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users/10/ROLES?value=USERS",
                HttpStatus.OK, "{\n" +
                        "  \"id\": \"10\",\n" +
                        "  \"fullName\":\"Agency User10\",\n" +
                        "  \"username\": \"user@example.com\",\n" +
                        "  \"email\": \"user@example.com\",\n" +
                        "  \"roles\":\"USERS;AGENCY_MANAGER\"\n" +
                        "}");

        User authUser = userRepository.findById("12").get();
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyName("My Agency");
        officialAgency.getUsers().add(authUser);
        authUser.setOfficialAgency(officialAgency);

        OfficialAgency agency = officialAgencyRepository.save(officialAgency);
        userRepository.save(authUser);

        User aUser = new User();
        aUser.setUserId("10");
        aUser.setCreatedAt(LocalDateTime.now());
        aUser.setOfficialAgency(agency);
        userRepository.save(aUser);

        String jwtToken = createToken("12", "agencyadmin@gg.com", "AG Admin", secretKey, new String[]{"USERS", "AGENCY_ADMIN"});

        RequestBuilder requestBuilder = delete("/api/protected/agency/user/10")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();

        User user = userRepository.findById("10").get();
        assertThat(user.getOfficialAgency()).isNull();

    }

}
