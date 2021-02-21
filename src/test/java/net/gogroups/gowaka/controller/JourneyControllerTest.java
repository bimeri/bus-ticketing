package net.gogroups.gowaka.controller;


import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.dto.JourneyDTO;
import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@ExtendWith(MockitoExtension.class)
public class JourneyControllerTest {
    @Mock
    private JourneyService mockJourneyService;


    private JourneyController journeyController;

    @BeforeEach
    void setup() throws Exception {
        journeyController = new JourneyController(mockJourneyService);
    }

    @Test
    void add_journey_should_call_car_service_add_location() {
        JourneyDTO journeyDTO = new JourneyDTO();
        journeyController.addJourney(journeyDTO, "1");
        verify(mockJourneyService).addJourney(journeyDTO, 1L);
    }

    @Test
    void getAJourneyById_callJourneyService() {
        ResponseEntity<JourneyResponseDTO> response = journeyController.getAJourneyById(1L);
        verify(mockJourneyService).getAJourneyById(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOfficialAgencyJourneys_callJourneyService() {

        when(mockJourneyService.getOfficialAgencyJourneys(anyInt(), anyInt()))
                .thenReturn(new PaginatedResponse<>());
        ResponseEntity<PaginatedResponse<JourneyResponseDTO>> response = journeyController.getOfficialAgencyJourneys(1, 10);
        verify(mockJourneyService).getOfficialAgencyJourneys(1, 10);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(PaginatedResponse.class);
    }
}
