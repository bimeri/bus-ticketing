package net.gowaka.gowaka.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.dto.CreateUserRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
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

    @Qualifier("apiSecurityRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    private String successClientTokenResponse = "{\n" +
            "  \"header\": \"Authorization\",\n" +
            "  \"type\": \"Bearer\",\n" +
            "  \"issuer\": \"API-Security\",\n" +
            "  \"version\": \"v1\",\n" +
            "  \"token\": \"jwt-token\"\n" +
            "}";

    private String successUserResponse = "{\n" +
            "  \"id\": 1,\n" +
            "  \"username\": \"eddytnk\",\n" +
            "  \"fullName\": \"Edward Tanko\",\n" +
            "  \"email\": \"tanko.edward@go-groups.net\",\n" +
            "  \"roles\": \"users\"\n" +
            "}";

    private String failureUserResponse = "{\n" +
            "  \"code\": \"INVALID_EMAIL\",\n" +
            "  \"message\": \"Invalid email address.\",\n" +
            "  \"path\": \"/api/protected/v1/users\"\n" +
            "}";


    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void tearDown() throws Exception {
        mockServer.reset();
    }

    private void startMockServerWith(String url, HttpStatus status, String response){
        mockServer.expect(requestTo(url))
                .andExpect(header("content-type","application/json;charset=UTF-8"))
                .andRespond(withStatus(status).body(response).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createUser_success_returns_200() throws Exception {

        startMockServerWith("http://localhost:8082/api/public/v1/clients/authorized",
                HttpStatus.OK,successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users",
                HttpStatus.OK,successUserResponse);

        String expectedResponse = "{\"id\":\"1\",\"fullName\":\"Jesus Christ\",\"email\":\"info@go-groups.net\",\"roles\":[\"users\"]}";

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
                HttpStatus.OK,successClientTokenResponse);

        startMockServerWith("http://localhost:8082/api/protected/v1/users",
                HttpStatus.UNPROCESSABLE_ENTITY,failureUserResponse);

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

}