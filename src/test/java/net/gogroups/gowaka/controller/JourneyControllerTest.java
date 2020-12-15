package net.gogroups.gowaka.controller;


import net.gogroups.gowaka.dto.JourneyDTO;
import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.service.JourneyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class JourneyControllerTest {
    @Mock
    private JourneyService mockJourneyService;


    private JourneyController journeyController;

    @Before
    public void setup() throws Exception {
        journeyController = new JourneyController(mockJourneyService);
    }

    @Test
    public void add_journey_should_call_car_service_add_location() {
        JourneyDTO journeyDTO = new JourneyDTO();
        journeyController.addJourney(journeyDTO, "1");
        verify(mockJourneyService).addJourney(journeyDTO, 1L);
    }

    @Test
    public void getAJourneyById_callJourneyService() {
        ResponseEntity<JourneyResponseDTO> response = journeyController.getAJourneyById(1L);
        verify(mockJourneyService).getAJourneyById(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
