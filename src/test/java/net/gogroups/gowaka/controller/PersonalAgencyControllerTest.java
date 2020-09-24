package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import net.gogroups.gowaka.service.PersonalAgencyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 11:00 AM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class PersonalAgencyControllerTest {

    @Mock
    private PersonalAgencyService mockPersonalAgencyService;
    private PersonalAgencyController personalAgencyController;

    @Before
    public void setUp() throws Exception {

        personalAgencyController = new PersonalAgencyController(mockPersonalAgencyService);

    }

    @Test
    public void createPersonalAgency_Calls_PersonalAgencyService() {

        CreatePersonalAgencyDTO createPersonalAgencyDTO = new CreatePersonalAgencyDTO();
        personalAgencyController.createPersonalAgency(createPersonalAgencyDTO);
        verify(mockPersonalAgencyService).createPersonalAgency(createPersonalAgencyDTO);
    }
}
