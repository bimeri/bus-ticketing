package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.EmailDTO;
import net.gogroups.gowaka.dto.EmailPasswordDTO;
import net.gogroups.gowaka.dto.UpdateProfileDTO;
import net.gogroups.security.model.ApiSecurityAccessToken;
import net.gogroups.gowaka.dto.CreateUserRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import static net.gogroups.gowaka.TestUtils.createToken;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:31 AM <br/>
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";


    @Qualifier("ggClientRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    private String successClientTokenResponse = "{\n" +
            "  \"header\": \"Authorization\",\n" +
            "  \"type\": \"Bearer\",\n" +
            "  \"issuer\": \"API-Security\",\n" +
            "  \"version\": \"v1\",\n" +
            "  \"token\": \"jwt-token\"\n" +
            "}";

    private String successNotificationTokenResponse = "{\n" +
            "  \"header\": \"Some Header\",\n" +
            "  \"type\": \"Some Type\",\n" +
            "  \"issuer\": \"Some Issuer\",\n" +
            "  \"token\": \"jwt-token-user\"\n" +
            "}";

    private String successUserResponse = "{\n" +
            "  \"id\": 1,\n" +
            "  \"username\": \"eddytnk\",\n" +
            "  \"fullName\": \"Edward Tanko\",\n" +
            "  \"email\": \"tanko.edward@go-groups.net\",\n" +
            "  \"roles\": \"users\"\n" +
            "}";

    private String failureUserTokenResponse = "{\n" +
            "  \"code\": \"BAD_CREDENTIALS\",\n" +
            "  \"message\": \"Wrong  username or password!\",\n" +
            "  \"path\": \"/api/public/v1/users/authorized\"\n" +
            "}";

    private String failureChangePasswordResponse = "{\n" +
            "  \"code\": \"BAD_USER_CREDENTIALS\",\n" +
            "  \"message\": \"Your old password does not match.\",\n" +
            "  \"path\": \"/api/public/v1/users/password\"\n" +
            "}";


    private String failureUserResponse = "{\n" +
            "  \"code\": \"INVALID_EMAIL\",\n" +
            "  \"message\": \"Invalid email address.\",\n" +
            "  \"path\": \"/api/protected/v1/users\"\n" +
            "}";


    private MockRestServiceServer mockServer;

    @Before
    public void setUp() throws JsonProcessingException {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    @After
    public void tearDown() {
        mockServer.reset();
    }

    private void startMockServerWith(String url, HttpStatus status, String response) {
        mockServer.expect(requestTo(url))
//                .andExpect(header("content-type", "application/json;charset=UTF-8"))
                .andRespond(withStatus(status).body(response).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createUser_success_returns_200() throws Exception {


        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users",
                HttpStatus.OK, successUserResponse);
        startMockServerWith("http://localhost:8082/api/public/login",
                HttpStatus.OK, successNotificationTokenResponse);
        mockServer.expect(requestTo("http://localhost:8082/api/protected/sendEmail"))
                .andExpect(header("content-type", "application/json;charset=UTF-8"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        String expectedResponse = "{\"id\":\"1\",\"fullName\":\"Jesus Christ\",\"email\":\"info@go-groups.net\",\"roles\":[\"USERS\"]}";

        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setFullName("Jesus Christ");
        userReq.setEmail("info@go-groups.net");
        userReq.setPassword("secret");

        RequestBuilder requestBuilder = post("/api/public/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(userReq))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void createUser_failure_returns_422() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users",
                HttpStatus.UNPROCESSABLE_ENTITY, failureUserResponse);

        String expectedResponse = "{\"code\":\"INVALID_EMAIL\",\"message\":\"Invalid email address.\",\"endpoint\":\"/api/public/register\"}";

        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setFullName("Jesus Christ");
        userReq.setEmail("info@go-groups.net");
        userReq.setPassword("secret");

        RequestBuilder requestBuilder = post("/api/public/register")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(userReq))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void loginUser_success_returns_200() throws Exception {

        User user = new User();
        user.setUserId("12");
        userRepository.save(user);
        String jwtToken = createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");

        ApiSecurityAccessToken apiSecurityAccessToken = new ApiSecurityAccessToken();
        apiSecurityAccessToken.setToken(jwtToken);
        apiSecurityAccessToken.setHeader("Authorization");
        apiSecurityAccessToken.setType("Bearer");
        apiSecurityAccessToken.setIssuer("API-Security");
        apiSecurityAccessToken.setVersion("v1");

        startMockServerWith("http://localhost:8082/api/public/v1/users/authorized",
                HttpStatus.OK, new ObjectMapper().writeValueAsString(apiSecurityAccessToken));

        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        emailPasswordDTO.setEmail("info@go-groups.net");
        emailPasswordDTO.setPassword("secret");

        RequestBuilder requestBuilder = post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(emailPasswordDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void loginUser_failed_returns_401() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/users/authorized",
                HttpStatus.UNAUTHORIZED, failureUserTokenResponse);

        String expectedResponse = "{\"code\":\"BAD_CREDENTIALS\",\"message\":\"Bad Credentials\",\"endpoint\":\"/api/public/login\"}";

        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        emailPasswordDTO.setEmail("info@go-groups.net");
        emailPasswordDTO.setPassword("secret");

        RequestBuilder requestBuilder = post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(emailPasswordDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }


    @Test
    public void changeUserPassword_success_returns_204() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/users/password",
                HttpStatus.NO_CONTENT, "");

        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        emailPasswordDTO.setEmail("info@go-groups.net");
        emailPasswordDTO.setPassword("secret");

        RequestBuilder requestBuilder = post("/api/public/change_password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(emailPasswordDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();

    }

    @Test
    public void changeUserPassword_failure_returns_422() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/users/password",
                HttpStatus.UNPROCESSABLE_ENTITY, failureChangePasswordResponse);

        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        emailPasswordDTO.setEmail("info@go-groups.net");
        emailPasswordDTO.setPassword("secret");

        String expectedResponse = "{\"code\":\"BAD_USER_CREDENTIALS\",\"message\":\"Your old password does not match.\",\"endpoint\":\"/api/public/change_password\"}";

        RequestBuilder requestBuilder = post("/api/public/change_password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(emailPasswordDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedResponse))
                .andReturn();

    }

    @Test
    public void forgotUserPassword_success_return_204() throws Exception {
        startMockServerWith("http://localhost:8082/api/public/v1/users/otp",
                HttpStatus.NO_CONTENT, "");

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("info@go-groups.net");

        RequestBuilder requestBuilder = post("/api/public/forgot_password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(emailDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void updateProfile_success_return_204() throws Exception {

        User user = new User();
        user.setUserId("12");
        userRepository.save(user);

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK, successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users/12/FULL_NAME?value=John%20Doe",
                HttpStatus.NO_CONTENT, "{}");

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setPhoneNumber("676767676");
        updateProfileDTO.setIdCardNumber("1234567890");
        updateProfileDTO.setFullName("John Doe");

        RequestBuilder requestBuilder = post("/api/protected/users/profile")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(updateProfileDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());

    }

}