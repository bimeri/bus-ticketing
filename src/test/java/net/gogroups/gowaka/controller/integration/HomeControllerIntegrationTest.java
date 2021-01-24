package net.gogroups.gowaka.controller.integration;

import net.gogroups.gowaka.domain.model.AppAlertNotice;
import net.gogroups.gowaka.domain.repository.AppAlertNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 6:05 AM <br/>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class HomeControllerIntegrationTest {

    @Autowired
    private AppAlertNoticeRepository appAlertNoticeRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        appAlertNoticeRepository.save(new AppAlertNotice(12L, "hello world", "en", true));
    }

    @Test
    void getAppNotice_success_returns_200() throws Exception {

        RequestBuilder requestBuilder = get("/api/public/notice")
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].language").value("en"))
                .andExpect(jsonPath("$.[0].message").value("hello world"));

    }
}
