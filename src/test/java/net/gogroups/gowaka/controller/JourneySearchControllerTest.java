package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/20/20 11:26 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
public class JourneySearchControllerTest {

    private JourneySearchController journeySearchController;

    @Mock
    private JourneyService mockJourneyService;


    @BeforeEach
    void setUp() {
        journeySearchController = new JourneySearchController(mockJourneyService);
    }

    @Test
    void searchJourney_calls_JourneyService() {

        journeySearchController.searchJourney(1L, 3L, "2020-02-01");
        verify(mockJourneyService).searchJourney(1L, 3L, "2020-02-01");

    }

    @Test
    void searchJourney_calls_JourneyService_returns_userJourneys() {

        journeySearchController.searchJourney();
        verify(mockJourneyService).searchJourney();

    }

    @Test
    void getAllAvailableJourney_calls_JourneyService_returns_userJourneys() {

        ResponseEntity<List<JourneyResponseDTO>> response = journeySearchController.getAllAvailableJourney();
        verify(mockJourneyService).searchAllAvailableJourney();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


}
