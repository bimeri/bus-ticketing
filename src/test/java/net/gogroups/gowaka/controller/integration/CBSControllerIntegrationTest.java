package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.gogroups.gowaka.TestUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 7:04 PM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class CBSControllerIntegrationTest {

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    @Qualifier("cbsApiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    private String jwtToken;
    private MockRestServiceServer mockServer;

    private String successTokenResponse = "{" +
            "\"accessToken\":\"access-token\"," +
            "\"issuer\":\"APISecurity\"" +
            "}";
    private String benefitResponse = "[\n" +
            "  {\n" +
            "    \"coveragePercentage\": 5.0,\n" +
            "    \"description\": \"UB Student\",\n" +
            "    \"id\": 2\n" +
            "  }\n" +
            "]";

    private String benefit404Response = "{\n" +
            "  \"code\": \"RESOURCE_NOT_FOUND\",\n" +
            "  \"message\": \"User not found.\",\n" +
            "  \"endpoint\": \"/api/protected/benefits/user\"\n" +
            "}";

    private String rewardPointsResponse = "{\n" +
            "  \"point\": 0\n" +
            "}";

    private String reward404Response = "{\n" +
            "  \"code\": \"RESOURCE_NOT_FOUND\",\n" +
            "  \"message\": \"User not found.\",\n" +
            "  \"endpoint\": \"/api/protected/cbs/reward_points/user\"\n" +
            "}";

    @Before
    public void setUp() throws JsonProcessingException {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        jwtToken = TestUtils.createToken("12", "ggadmin@gg.com", "GW Root", secretKey, "USERS", "GW_ADMIN", "AGENCY_MANAGER");
    }

    private void startMockServerWith(String url, HttpStatus status, String response) {
        mockServer.expect(requestTo(url))
                .andRespond(withStatus(status).body(response).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getAllBenefits_getAllBenefits() throws Exception {

        startMockServerWith("http://localhost:9087/api/public/login",
                HttpStatus.OK, successTokenResponse);

        startMockServerWith("http://localhost:9087/api/protected/benefits",
                HttpStatus.OK, benefitResponse);

        RequestBuilder requestBuilder = get("/api/public/cbs/benefits")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"coveragePercentage\":5.0,\"description\":\"UB Student\",\"id\":2}]"))
                .andReturn();
    }

    @Test
    public void getAllUserBenefits_getAllBenefits() throws Exception {
        startMockServerWith("http://localhost:9087/api/public/login",
                HttpStatus.OK, successTokenResponse);

        startMockServerWith("http://localhost:9087/api/protected/benefits/user?goWakaUserId=12",
                HttpStatus.OK, benefitResponse);

        RequestBuilder requestBuilder = get("/api/protected/cbs/benefits/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"coveragePercentage\":5.0,\"description\":\"UB Student\",\"id\":2}]"))
                .andReturn();
    }

    @Test
    public void getAllUserBenefits_return_404_whenUserIdNotfound() throws Exception {
        startMockServerWith("http://localhost:9087/api/public/login",
                HttpStatus.OK, successTokenResponse);

        startMockServerWith("http://localhost:9087/api/protected/benefits/user?goWakaUserId=12",
                HttpStatus.NOT_FOUND, benefit404Response);

        RequestBuilder requestBuilder = get("/api/protected/cbs/benefits/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"code\":\"CBS_USER_RESOURCE_NOT_FOUND\",\"message\":\"User not found.\",\"endpoint\":\"/api/protected/cbs/benefits/user\"}"))
                .andReturn();
    }

    @Test
    public void getUserRewardPoints_thenReturnRewardPointDTO() throws Exception {
        startMockServerWith("http://localhost:9087/api/public/login",
                HttpStatus.OK, successTokenResponse);

        startMockServerWith("http://localhost:9087/api/protected/reward_points/users/12",
                HttpStatus.OK, rewardPointsResponse);

        RequestBuilder requestBuilder = get("/api/protected/cbs/reward_points/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"point\": 0}"))
                .andReturn();
    }

    @Test
    public void getUserRewardPoints_return_404_whenUserIdNotfound() throws Exception {
        startMockServerWith("http://localhost:9087/api/public/login",
                HttpStatus.OK, successTokenResponse);

        startMockServerWith("http://localhost:9087/api/protected/reward_points/users/12",
                HttpStatus.NOT_FOUND, reward404Response);

        RequestBuilder requestBuilder = get("/api/protected/cbs/reward_points/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"code\":\"CBS_USER_RESOURCE_NOT_FOUND\",\"message\":\"User not found.\",\"endpoint\":\"/api/protected/cbs/reward_points/user\"}"))
                .andReturn();
    }

}