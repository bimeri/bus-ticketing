package net.gogroups.gowaka.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogroups.gowaka.domain.model.ServiceCharge;
import net.gogroups.gowaka.domain.repository.ServiceChargeServiceRepository;
import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.security.utils.ApiSecurityTestUtils;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 7:14 AM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ServiceChargeIntegrationTest {

    @Autowired
    private ServiceChargeServiceRepository serviceChargeServiceRepository;

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.jwt.token.privateKey}")
    private String secretKey = "";


    private String jwtToken;

    @BeforeEach
    public void setUp() throws Exception {
        serviceChargeServiceRepository.save(new ServiceCharge("sc-id", 5.0, 100.0));
        jwtToken = ApiSecurityTestUtils.createToken("12", "gwuser@gg.com", "GW User", secretKey, new String[]{"GW_ADMIN", "USERS"});
    }

    @Test
    public void getServiceCharges_success_return_200() throws Exception {

        RequestBuilder requestBuilder = get("/api/protected/service_charges")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Test
    public void updateServiceCharges_success_return_204() throws Exception {

        RequestBuilder requestBuilder = put("/api/protected/service_charges")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + jwtToken)
                .content(new ObjectMapper().writeValueAsString(new ServiceChargeDTO("sc-id", 10.0, 100.0)))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

}
