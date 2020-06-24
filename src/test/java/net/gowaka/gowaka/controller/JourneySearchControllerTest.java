package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.service.JourneyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/20/20 11:26 AM <br/>
 */
@RunWith(SpringRunner.class)
public class JourneySearchControllerTest {

    private JourneySearchController journeySearchController;

    @Mock
    private JourneyService mockJourneyService;


    @Before
    public void setUp() {
        journeySearchController = new JourneySearchController(mockJourneyService);
    }

    @Test
    public void searchJourney_calls_JourneyService() {

        journeySearchController.searchJourney(1L, 3L, "2020-02-01");
        verify(mockJourneyService).searchJourney(1L, 3L, "2020-02-01");

    }

    @Test
    public void searchJourney_calls_JourneyService_returns_userJourneys() {

        journeySearchController.searchJourney();
        verify(mockJourneyService).searchJourney();

    }


}
