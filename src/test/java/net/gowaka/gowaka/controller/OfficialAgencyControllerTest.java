package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
import net.gowaka.gowaka.dto.EmailDTO;
import net.gowaka.gowaka.dto.OfficialAgencyUserRoleRequestDTO;
import net.gowaka.gowaka.service.OfficialAgencyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:41 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class OfficialAgencyControllerTest {

    @Mock
    private OfficialAgencyService mockOfficialAgencyService;

    private OfficialAgencyController officialAgencyController;
    @Before
    public void setUp() throws Exception {
        officialAgencyController = new OfficialAgencyController(mockOfficialAgencyService);
    }

    @Test
    public void createOfficialAgency_calls_OfficialAgencyService() {

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        officialAgencyController.createOfficialAgency(createOfficialAgencyDTO);
        verify(mockOfficialAgencyService).createOfficialAgency(createOfficialAgencyDTO);

    }

    @Test
    public void assignAgencyUserRole_calls_OfficialAgencyService() {

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyController.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);
        verify(mockOfficialAgencyService).assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

    }

    @Test
    public void getAgencyUser_calls_OfficialAgencyService() {
        officialAgencyController.getAgencyUsers();
        verify(mockOfficialAgencyService).getAgencyUsers();

    }
    @Test
    public void removeAgencyUser_calls_OfficialAgencyService() {
        officialAgencyController.removeAgencyUser("12");
        verify(mockOfficialAgencyService).removeAgencyUser("12");

    }

    @Test
    public void addAgencyUser_calls_OfficialAgencyService() {
        EmailDTO emailDTO = new EmailDTO();
        officialAgencyController.addAgencyUser(emailDTO);
        verify(mockOfficialAgencyService).addAgencyUser(emailDTO);

    }

}