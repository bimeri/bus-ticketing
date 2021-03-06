package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.LocalDateTime;

import static net.gogroups.gowaka.TestUtils.createToken;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/18/19 7:24 PM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class PersonalAgencyControllerIntegrationTest {


    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";

    @Autowired
    private UserRepository userRepository;

    private User user;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {

        User newUser = new User();
        newUser.setUserId("12");
        newUser.setCreatedAt(LocalDateTime.now());

        this.user = userRepository.save(newUser);

    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void createPersonalAgency_success_return_200() throws Exception {

        CreatePersonalAgencyDTO createOfficialAgencyDTO = new CreatePersonalAgencyDTO();
        createOfficialAgencyDTO.setName("GG Express");


        String jwtToken = createToken("12", "gwuser@gg.com", "GW User", secretKey, new String[]{"USERS"});

        String expectedResponse = "{\"id\":1,\"name\":\"GG Express\"}";

        RequestBuilder requestBuilder = post("/api/protected/users/agency")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(createOfficialAgencyDTO))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andReturn();

        User aUser = userRepository.findById("12").get();
        assertThat(aUser.getPersonalAgency()).isNotNull();

    }


}
