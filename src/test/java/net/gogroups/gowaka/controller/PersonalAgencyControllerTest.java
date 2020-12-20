package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import net.gogroups.gowaka.service.PersonalAgencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 11:00 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
public class PersonalAgencyControllerTest {

    @Mock
    private PersonalAgencyService mockPersonalAgencyService;
    private PersonalAgencyController personalAgencyController;

    @BeforeEach
    void setUp() throws Exception {

        personalAgencyController = new PersonalAgencyController(mockPersonalAgencyService);

    }

    @Test
    void createPersonalAgency_Calls_PersonalAgencyService() {

        CreatePersonalAgencyDTO createPersonalAgencyDTO = new CreatePersonalAgencyDTO();
        personalAgencyController.createPersonalAgency(createPersonalAgencyDTO);
        verify(mockPersonalAgencyService).createPersonalAgency(createPersonalAgencyDTO);
    }
}
