package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.OfficialAgencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@ExtendWith(MockitoExtension.class)
public class OfficialAgencyControllerTest {

    @Mock
    private OfficialAgencyService mockOfficialAgencyService;

    private OfficialAgencyController officialAgencyController;
    @BeforeEach
    void setUp() throws Exception {
        officialAgencyController = new OfficialAgencyController(mockOfficialAgencyService);
    }

    @Test
    void createOfficialAgency_calls_OfficialAgencyService() {

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        officialAgencyController.createOfficialAgency(createOfficialAgencyDTO);
        verify(mockOfficialAgencyService).createOfficialAgency(createOfficialAgencyDTO);

    }

    @Test
    void uploadAgencyLogo_calls_OfficialAgencyService() {

        MockMultipartFile file = new MockMultipartFile("logo.png", "".getBytes());
        officialAgencyController.uploadAgencyLogo(1L, file);
        verify(mockOfficialAgencyService).uploadAgencyLogo(1L, file);

    }

    @Test
    void getOfficialAgency_calls_OfficialAgencyService() {

        ResponseEntity<List<OfficialAgencyDTO>> response = officialAgencyController.getOfficialAgencies();
        verify(mockOfficialAgencyService).getAllAgencies();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
    @Test
    void getUserOfficialAgency_calls_OfficialAgencyService() {

        ResponseEntity<OfficialAgencyDTO> response = officialAgencyController.getUserOfficialAgency();
        verify(mockOfficialAgencyService).getUserAgency();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
    @Test
    void updateOfficialAgency_calls_OfficialAgencyService() {

        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        ResponseEntity<Void> response = officialAgencyController.updateOfficialAgency(2L, officialAgencyDTO);
        verify(mockOfficialAgencyService).updateOfficialAgency(2L, officialAgencyDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }


    @Test
    void assignAgencyUserRole_calls_OfficialAgencyService() {

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyController.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);
        verify(mockOfficialAgencyService).assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

    }

    @Test
    void getAgencyUser_calls_OfficialAgencyService() {
        officialAgencyController.getAgencyUsers();
        verify(mockOfficialAgencyService).getAgencyUsers();

    }
    @Test
    void removeAgencyUser_calls_OfficialAgencyService() {
        officialAgencyController.removeAgencyUser("12");
        verify(mockOfficialAgencyService).removeAgencyUser("12");

    }

    @Test
    void addAgencyUser_calls_OfficialAgencyService() {
        EmailDTO emailDTO = new EmailDTO();
        officialAgencyController.addAgencyUser(emailDTO, 1L);
        verify(mockOfficialAgencyService).addAgencyUser(emailDTO, 1L);

    }

    @Test
    void createBranch_calls_OfficialAgencyService() {
        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");
        officialAgencyController.createBranch(createBranchDTO);
        verify(mockOfficialAgencyService).createBranch(createBranchDTO);

    }

    @Test
    void updateBranch_calls_OfficialAgencyService() {
        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");
        officialAgencyController.updateBranch(createBranchDTO, 1L);
        verify(mockOfficialAgencyService).updateBranch(createBranchDTO, 1L);

    }

    @Test
    void deleteBranch_calls_OfficialAgencyService() {
        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");
        officialAgencyController.deleteBranch(1L);
        verify(mockOfficialAgencyService).deleteBranch(1L);

    }


}
