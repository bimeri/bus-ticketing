package net.gowaka.gowaka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/18/19 7:24 PM <br/>
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class OfficialAgencyControllerIntegrationTest {


    @Value("${security.jwt.token.secretKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfficialAgencyRepository officialAgencyRepository;

    private User user;

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("apiSecurityRestTemplate")
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
        newUser.setTimestamp(LocalDateTime.now());

        this.user = userRepository.save(newUser);

    }

    @After
    public void tearDown() throws Exception {
        mockServer.reset();
    }


    private void startMockServerWith(String url, HttpStatus status, String response) {
        mockServer.expect(requestTo(url))
//                .andExpect(header("content-type", "application/json;charset=UTF-8"))
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
                        "  \"roles\":\"users;\"\n" +
                        "}");

        startMockServerWith("http://localhost:8082/api/protected/v1/users/10/ROLES?value=users;agency_admin",
                HttpStatus.NO_CONTENT, "");



        User agencyAdminUser = new User();
        agencyAdminUser.setUserId("10");
        agencyAdminUser.setTimestamp(LocalDateTime.now());
        userRepository.save(agencyAdminUser);

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        createOfficialAgencyDTO.setAgencyRegistrationNumber("123456789");
        createOfficialAgencyDTO.setAgencyName("GG Express");
        createOfficialAgencyDTO.setAgencyAdminEmail("admin@example.com");

        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", new String[]{"users", "gw_admin"});

        String expectedResponse = "{\"id\":1,\"agencyName\":\"GG Express\",\"agencyRegistrationNumber\":\"123456789\",\"agencyAdmin\":{\"id\":\"10\",\"fullName\":\"Agency User\"}}\n";

        RequestBuilder requestBuilder = post("/api/protected/agency")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(createOfficialAgencyDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

        User aUser = userRepository.findById("10").get();
        assertThat(aUser.getOfficialAgency()).isNotNull();

    }


    private String createToken(String userId, String email, String fullName, String... roles) throws JsonProcessingException {
        Claims claims = Jwts.claims().setSubject(email);

        claims.put("auth", new ObjectMapper().writeValueAsString(roles));
        claims.put("id", userId);
        claims.put("fullName", fullName);
        claims.put("appName", "GoWaka");
        claims.put("grant_type", "user_profile");

        Date now = new Date();
        long expiredMillis = (now.getTime() + 100000);
        Date validity = new Date(expiredMillis);
        String key = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}