package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.CreateOfficialAgencyDTO;
import net.gogroups.gowaka.dto.EmailDTO;
import net.gogroups.gowaka.dto.OfficialAgencyDTO;
import net.gogroups.gowaka.dto.OfficialAgencyUserRoleRequestDTO;
import net.gogroups.gowaka.service.OfficialAgencyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void uploadAgencyLogo_calls_OfficialAgencyService() {

        MockMultipartFile file = new MockMultipartFile("logo.png", "".getBytes());
        officialAgencyController.uploadAgencyLogo(1L, file);
        verify(mockOfficialAgencyService).uploadAgencyLogo(1L, file);

    }

    @Test
    public void getOfficialAgency_calls_OfficialAgencyService() {

        ResponseEntity<List<OfficialAgencyDTO>> response = officialAgencyController.getOfficialAgencies();
        verify(mockOfficialAgencyService).getAllAgencies();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
    @Test
    public void getUserOfficialAgency_calls_OfficialAgencyService() {

        ResponseEntity<OfficialAgencyDTO> response = officialAgencyController.getUserOfficialAgency();
        verify(mockOfficialAgencyService).getUserAgency();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
    @Test
    public void updateOfficialAgency_calls_OfficialAgencyService() {

        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        ResponseEntity<Void> response = officialAgencyController.updateOfficialAgency(2L, officialAgencyDTO);
        verify(mockOfficialAgencyService).updateOfficialAgency(2L, officialAgencyDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

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
