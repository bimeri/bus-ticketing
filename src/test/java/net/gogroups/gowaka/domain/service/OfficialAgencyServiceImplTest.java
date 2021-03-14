package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.AgencyBranchRepository;
import net.gogroups.gowaka.domain.repository.JourneyRepository;
import net.gogroups.gowaka.domain.repository.OfficialAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.service.OfficialAgencyService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.security.model.ApiSecurityAccessToken;
import net.gogroups.security.model.ApiSecurityUser;
import net.gogroups.security.service.ApiSecurityService;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 9:29 PM <br/>
 */
@ExtendWith(MockitoExtension.class)
public class OfficialAgencyServiceImplTest {

    @Mock
    private OfficialAgencyRepository mockOfficialAgencyRepository;
    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private UserService mockUserService;
    @Mock
    private ApiSecurityService mockApiSecurityService;

    @Mock
    private FileStorageService mockFileStorageService;

    @Mock
    private JourneyRepository mockJourneyRepository;

    @Mock
    private AgencyBranchRepository mockAgencyBranchRepository;

    private OfficialAgencyService officialAgencyService;

    @BeforeEach
    void setUp() {
        ClientUserCredConfig clientUserCredConfig = new ClientUserCredConfig();
        clientUserCredConfig.setClientId("client-id");
        clientUserCredConfig.setClientId("client-secret");
        clientUserCredConfig.setAppName("GoWaka");

        officialAgencyService = new OfficialAgencyServiceImpl(mockOfficialAgencyRepository, mockUserRepository, mockUserService, mockApiSecurityService, clientUserCredConfig, mockFileStorageService, mockJourneyRepository, mockAgencyBranchRepository);
    }

    @Test
    void createOfficialAgency_call_OfficialAgencyRepository() {

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<OfficialAgency> officialAgencyArgumentCaptor = ArgumentCaptor.forClass(OfficialAgency.class);
        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AgencyBranch> agencyBranchArgumentCaptor = ArgumentCaptor.forClass(AgencyBranch.class);

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        createOfficialAgencyDTO.setAgencyAdminEmail("example@example.com");
        createOfficialAgencyDTO.setAgencyName("Amo Mezam");
        createOfficialAgencyDTO.setAgencyRegistrationNumber("ABC20111234");

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        apiSecurityUser.setRoles("USERS;");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setFullName("Jesus Christ");
        apiSecurityUser.setUsername("example@example.com");

        when(mockApiSecurityService.getUserByUsername(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        User user = new User();
        user.setUserId("12");
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyRegistrationNumber("ABC20111234");
        officialAgency.setAgencyName("Amo Mezam");
        when(mockOfficialAgencyRepository.save(any()))
                .thenReturn(officialAgency);
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Name");
        agencyBranch.setId(9L);
        when(mockAgencyBranchRepository.save(any()))
                .thenReturn(agencyBranch);

        OfficialAgencyDTO officialAgencyDTO = officialAgencyService.createOfficialAgency(createOfficialAgencyDTO);

        verify(mockApiSecurityService).updateUserInfo(idArgumentCaptor.capture(), fieldArgumentCaptor.capture(),
                roleArgumentCaptor.capture(), valueArgumentCaptor.capture());
        assertThat(idArgumentCaptor.getValue()).isEqualTo("12");
        assertThat(fieldArgumentCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleArgumentCaptor.getValue()).isEqualTo("USERS;AGENCY_ADMIN");
        assertThat(valueArgumentCaptor.getValue()).isEqualTo("jwt-token");

        verify(mockUserRepository).findById("12");

        verify(mockUserRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getUserId()).isEqualTo("12");

        verify(mockAgencyBranchRepository).save(agencyBranchArgumentCaptor.capture());
        assertThat(agencyBranchArgumentCaptor.getValue().getName()).isEqualTo("Main Branch");

        verify(mockOfficialAgencyRepository).save(officialAgencyArgumentCaptor.capture());
        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyName()).isEqualTo("Amo Mezam");
        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyRegistrationNumber()).isEqualTo("ABC20111234");
        assertThat(officialAgencyArgumentCaptor.getValue().getUsers().size()).isEqualTo(1);
        assertThat(officialAgencyArgumentCaptor.getValue().getIsDisabled()).isEqualTo(false);

        assertThat(officialAgencyDTO.getAgencyAdmin()).isNotNull();
        assertThat(officialAgencyDTO.getAgencyName()).isEqualTo("Amo Mezam");
        assertThat(officialAgencyDTO.getAgencyRegistrationNumber()).isEqualTo("ABC20111234");

    }

    @Test
    void uploadAgencyLogo_calls_fileStorageService() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "multipart/form-data", "My logo Content".getBytes());
        when(mockOfficialAgencyRepository.findById(2L))
                .thenReturn(Optional.of(new OfficialAgency()));
        officialAgencyService.uploadAgencyLogo(2L, file);
        verify(mockFileStorageService).saveFile("logo.png", file.getBytes(), "agency_logos/2", FileAccessType.PROTECTED);

    }

    @Test
    void getAllOfficialAgencies_returnAgencyDTO() {

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setLogo("agency_logos/2/logo.png");
        officialAgency.setBuses(Collections.singletonList(new Bus()));
        when(mockOfficialAgencyRepository.findAll())
                .thenReturn(Collections.singletonList(officialAgency));
        when(mockFileStorageService.getFilePath("agency_logos/2/logo.png", "", FileAccessType.PROTECTED))
                .thenReturn("http://localhost/logo.png");
        List<OfficialAgencyDTO> allAgencies = officialAgencyService.getAllAgencies();
        assertThat(allAgencies.size()).isEqualTo(1);
        assertThat(allAgencies.get(0).getBuses().size()).isEqualTo(1);
        assertThat(allAgencies.get(0).getLogo()).isEqualTo("http://localhost/logo.png");

    }

    @Test
    void getUserOfficialAgency_throwException_whenUserNotFound() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.getUserAgency());
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("User not found");

    }

    @Test
    void getUserOfficialAgency_returnAgencyDTO() {

        OfficialAgency officialAgency = new OfficialAgency();
        Bus car = new Bus();
        User user = new User();
        UserDTO userDTO = new UserDTO();
        Journey journey = new Journey();

        officialAgency.setId(10L);
        officialAgency.setLogo("agency_logos/2/logo.png");
        officialAgency.setBuses(Collections.singletonList(car));

        car.setOfficialAgency(officialAgency);

        user.setUserId("10");
        user.setOfficialAgency(officialAgency);
        userDTO.setId("10");

        journey.setCar(car);

        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));
        when(mockJourneyRepository.findByArrivalIndicatorTrue())
                .thenReturn(Collections.singletonList(journey));

        when(mockFileStorageService.getFilePath("agency_logos/2/logo.png", "", FileAccessType.PROTECTED))
                .thenReturn("http://localhost/logo.png");
        OfficialAgencyDTO agency = officialAgencyService.getUserAgency();
        assertThat(agency.getBuses().size()).isEqualTo(1);
        assertThat(agency.getLogo()).isEqualTo("http://localhost/logo.png");
        assertThat(agency.getNumberOfCompletedTrips()).isEqualTo(1);

    }

    @Test
    void updateOfficialAgencies_returnAgencyDTO() {


        ArgumentCaptor<OfficialAgency> officialAgencyArgumentCaptor = ArgumentCaptor.forClass(OfficialAgency.class);

        OfficialAgency officialAgency = new OfficialAgency();
        when(mockOfficialAgencyRepository.findById(2L))
                .thenReturn(Optional.of(officialAgency));

        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        officialAgencyDTO.setAgencyName("GG Agency");
        officialAgencyDTO.setAgencyRegistrationNumber("Registration GG Agency");

        officialAgencyService.updateOfficialAgency(2L, officialAgencyDTO);
        verify(mockOfficialAgencyRepository).save(officialAgencyArgumentCaptor.capture());

        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyName()).isEqualTo("GG Agency");
        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyRegistrationNumber()).isEqualTo("Registration GG Agency");

    }

    @Test
    void createOfficialAgency_throws_Exception_when_user_isAlready_inAn_Agency() {


        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        createOfficialAgencyDTO.setAgencyAdminEmail("example@example.com");
        createOfficialAgencyDTO.setAgencyName("Amo Mezam");
        createOfficialAgencyDTO.setAgencyRegistrationNumber("ABC20111234");

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        when(mockApiSecurityService.getUserByUsername(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        User user = new User();
        user.setUserId("12");
        user.setOfficialAgency(new OfficialAgency());
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.createOfficialAgency(createOfficialAgencyDTO));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_ALREADY_IN_AN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User already a member of an agency.");

    }

    @Test
    void assignAgencyUserRole_calls_ApiSecurityService() {

        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);


        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        apiSecurityUser.setRoles("USERS;");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setFullName("Jesus Christ");
        apiSecurityUser.setUsername("example@example.com");

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(88L);
        User agencyUser = new User();
        agencyUser.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(agencyUser));
        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(authUser));

        when(mockApiSecurityService.getUserByUserId(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyUserRoleRequestDTO.setUserId("12");
        officialAgencyUserRoleRequestDTO.setRoles(Arrays.asList("AGENCY_OPERATOR", "AGENCY_MANAGER"));

        officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

        verify(mockApiSecurityService).updateUserInfo(idArgumentCaptor.capture(), fieldArgumentCaptor.capture(),
                roleArgumentCaptor.capture(), valueArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo("12");
        assertThat(fieldArgumentCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleArgumentCaptor.getValue()).isEqualTo("USERS;AGENCY_OPERATOR;AGENCY_MANAGER");
        assertThat(valueArgumentCaptor.getValue()).isEqualTo("jwt-token");

    }

    @Test
    void assignAgencyUserRole_Donot_assign_AGENCY_ADMIN_and_GW_ADMIN() {

        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);


        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        apiSecurityUser.setRoles("USERS;");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setFullName("Jesus Christ");
        apiSecurityUser.setUsername("example@example.com");

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(88L);
        User agencyUser = new User();
        agencyUser.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(agencyUser));
        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(authUser));

        when(mockApiSecurityService.getUserByUserId(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyUserRoleRequestDTO.setUserId("12");
        officialAgencyUserRoleRequestDTO.setRoles(Arrays.asList("AGENCY_ADMIN", "AGENCY_MANAGER", "GW_ADMIN"));

        officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

        verify(mockApiSecurityService).updateUserInfo(idArgumentCaptor.capture(), fieldArgumentCaptor.capture(),
                roleArgumentCaptor.capture(), valueArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo("12");
        assertThat(fieldArgumentCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleArgumentCaptor.getValue()).isEqualTo("USERS;AGENCY_MANAGER");
        assertThat(valueArgumentCaptor.getValue()).isEqualTo("jwt-token");

    }

    @Test
    void assignAgencyUserRole_throw_Exception_when_User_not_inSameAgency_as_AuthUser() {

        OfficialAgency userOfficialAgency = new OfficialAgency();
        userOfficialAgency.setId(88L);
        User agencyUser = new User();
        agencyUser.setOfficialAgency(userOfficialAgency);
        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(agencyUser));
        UserDTO userDTO = new UserDTO();
        userDTO.setId("10");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        OfficialAgency authUserOfficialAgency = new OfficialAgency();
        userOfficialAgency.setId(99L);
        User authUser = new User();
        authUser.setOfficialAgency(authUserOfficialAgency);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(authUser));

        OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO = new OfficialAgencyUserRoleRequestDTO();
        officialAgencyUserRoleRequestDTO.setUserId("12");
        officialAgencyUserRoleRequestDTO.setRoles(Arrays.asList("AGENCY_ADMIN", "AGENCY_MANAGER"));

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User must be a member to your agency.");

    }

    @Test
    void getAgencyUsers_getAll_Users_in_Agency() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setName("Main Branch");
        agencyBranch.setAddress("Bda");
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName("EE Xpress");
        officialAgency.setId(9L);
        User user1 = new User();
        user1.setOfficialAgency(officialAgency);
        user1.setAgencyBranch(agencyBranch);
        user1.setUserId("10");
        User user2 = new User();
        user2.setUserId("11");
        user2.setOfficialAgency(officialAgency);
        user2.setAgencyBranch(agencyBranch);
        officialAgency.setUsers(Arrays.asList(user1, user2));

        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(officialAgency);

        ApiSecurityAccessToken clientToken = new ApiSecurityAccessToken();
        clientToken.setToken("jwt-token");

        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(clientToken);
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));

        ApiSecurityUser apiSecurityUser1 = new ApiSecurityUser();
        apiSecurityUser1.setId("10");
        apiSecurityUser1.setEmail("ex1@example.com");
        apiSecurityUser1.setRoles("USER;AGENCY_ADMIN");

        ApiSecurityUser apiSecurityUser2 = new ApiSecurityUser();
        apiSecurityUser2.setId("10");
        apiSecurityUser2.setEmail("ex1@example.com");
        apiSecurityUser2.setRoles("USER;AGENCY_ADMIN");

        when(mockApiSecurityService.getUserByUserId("10", "jwt-token"))
                .thenReturn(apiSecurityUser1);
        when(mockApiSecurityService.getUserByUserId("11", "jwt-token"))
                .thenReturn(apiSecurityUser2);

        List<OfficialAgencyUserDTO> agencyUsers = officialAgencyService.getAgencyUsers();
        assertThat(agencyUsers.size()).isEqualTo(2);

    }

    @Test
    void addAgencyUsers_throw_Exception_when_authUser_NotFound() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.addAgencyUser(emailDTO, 1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("User not found.");

    }

    @Test
    void addAgencyUsers_throw_Exception_when_user_is_already_a_member_of_An_Agency() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        User user = new User();
        user.setUserId("10");
        user.getRoles().add("USERS");
        user.setOfficialAgency(new OfficialAgency());
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("10");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setUsername("example@example.com");
        apiSecurityUser.setFullName("John Doe");
        apiSecurityUser.setRoles("USERS");
        when(mockApiSecurityService.getUserByUsername(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.addAgencyUser(emailDTO, 1L));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_ALREADY_IN_AN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User already a member of an agency.");
    }

    @Test
    void addAgencyUsers_throwsException_whenBranchNotFound() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        User user = new User();
        user.setUserId("10");
        user.getRoles().add("USERS");
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("10");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setUsername("example@example.com");
        apiSecurityUser.setFullName("John Doe");
        apiSecurityUser.setRoles("USERS");
        when(mockApiSecurityService.getUserByUsername(any(), any()))
                .thenReturn(apiSecurityUser);

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.addAgencyUser(emailDTO, 1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("Not a valid agency branch");
    }

    @Test
    void addAgencyUsers_call_OfficialAgencyRepository() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setAddress("Bda");
        agencyBranch.setName("Main branch");
        officialAgency.setAgencyBranch(Collections.singletonList(agencyBranch));

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        User user = new User();
        user.setUserId("10");
        user.getRoles().add("USERS");
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("10");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setUsername("example@example.com");
        apiSecurityUser.setFullName("John Doe");
        apiSecurityUser.setRoles("USERS");
        when(mockApiSecurityService.getUserByUsername(any(), any()))
                .thenReturn(apiSecurityUser);

        OfficialAgencyUserDTO officialAgencyUserDTO = officialAgencyService.addAgencyUser(emailDTO, 1L);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("12");
        verify(mockApiSecurityService).getUserByUsername("example@example.com", "jwt-token");
        verify(mockUserRepository).save(any());
        assertThat(officialAgencyUserDTO.getId()).isEqualTo("10");
        assertThat(officialAgencyUserDTO.getFullName()).isEqualTo("John Doe");
        assertThat(officialAgencyUserDTO.getRoles()).isEqualTo(Collections.singletonList("USERS"));

        assertThat(user.getOfficialAgency()).isEqualTo(officialAgency);
    }

    @Test
    void removeAgencyUser_remove_a_user_from_an_Agency() {

        String userId = "10";
        User user = new User();
        user.setUserId(userId);
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.getUsers().add(user);
        officialAgency.setId(1L);
        user.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        User authUser = new User();
        authUser.setUserId("12");
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);
        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        officialAgencyService.removeAgencyUser(userId);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockApiSecurityService).updateUserInfo(userIdCaptor.capture(), fieldCaptor.capture(), roleCaptor.capture(), tokenCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo("10");
        assertThat(fieldCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleCaptor.getValue()).isEqualTo("USERS");
        assertThat(tokenCaptor.getValue()).isEqualTo("jwt-token");

        verify(mockUserRepository).save(any());
        assertThat(user.getOfficialAgency()).isNull();
    }

    @Test
    void removeAgencyUser_throw_Exception_when_user_is_not_a_member_Agency() {
        String userId = "10";
        User user = new User();
        user.setUserId(userId);
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.getUsers().add(user);
        officialAgency.setId(1L);
        user.setOfficialAgency(officialAgency);
        when(mockUserRepository.findById("10"))
                .thenReturn(Optional.of(user));

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        User authUser = new User();
        authUser.setUserId("12");
        authUser.setOfficialAgency(new OfficialAgency());
        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.removeAgencyUser(userId));
        assertThat(apiException.getErrorCode()).isEqualTo("USER_NOT_IN_AGENCY");
        assertThat(apiException.getMessage()).isEqualTo("User must be a member to your agency.");

    }

    @Test
    void createBranch_call_AgencyBranchRepository() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        ArgumentCaptor<AgencyBranch> agencyBranchArgumentCaptor = ArgumentCaptor.forClass(AgencyBranch.class);

        officialAgencyService.createBranch(createBranchDTO);
        verify(mockAgencyBranchRepository).save(agencyBranchArgumentCaptor.capture());

        assertThat(agencyBranchArgumentCaptor.getValue().getName()).isEqualTo("Main Branch");
        assertThat(agencyBranchArgumentCaptor.getValue().getAddress()).isEqualTo("Address");

    }

    @Test
    void updateBranch_throwsException_whenBranchNotFound() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");

        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.updateBranch(createBranchDTO, 1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("Branch not found");
    }

    @Test
    void updateBranch_throwsException_whenNotInAgencyUserNotNotFound() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");

        OfficialAgency officialAgency1 = new OfficialAgency();
        officialAgency1.setId(3L);
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main Branch");
        agencyBranch.setAddress("Address");
        agencyBranch.setOfficialAgency(officialAgency1);
        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.of(agencyBranch));

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));
        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.updateBranch(createBranchDTO, 1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("User not in agency");
    }

    @Test
    void updateBranch_call_AgencyBranchRepository() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch2");
        createBranchDTO.setAddress("Address2");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(2L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main Branch");
        agencyBranch.setAddress("Address");
        agencyBranch.setOfficialAgency(officialAgency);
        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.of(agencyBranch));

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        ArgumentCaptor<AgencyBranch> agencyBranchArgumentCaptor = ArgumentCaptor.forClass(AgencyBranch.class);

        officialAgencyService.updateBranch(createBranchDTO, 1L);
        verify(mockAgencyBranchRepository).save(agencyBranchArgumentCaptor.capture());

        assertThat(agencyBranchArgumentCaptor.getValue().getName()).isEqualTo("Main Branch2");
        assertThat(agencyBranchArgumentCaptor.getValue().getAddress()).isEqualTo("Address2");

    }

    @Test
    void deleteBranch_throwsException_whenBranchNotFound() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");

        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.deleteBranch(1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("Branch not found");
    }

    @Test
    void deleteBranch_throwsException_whenNotInAgencyUserNotNotFound() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch");
        createBranchDTO.setAddress("Address");

        OfficialAgency officialAgency1 = new OfficialAgency();
        officialAgency1.setId(3L);
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main Branch");
        agencyBranch.setAddress("Address");
        agencyBranch.setOfficialAgency(officialAgency1);
        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.of(agencyBranch));

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));
        ApiException apiException = assertThrows(ApiException.class, () -> officialAgencyService.deleteBranch(1L));
        assertThat(apiException.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(apiException.getMessage()).isEqualTo("User not in agency");
    }

    @Test
    void deleteBranch_call_AgencyBranchRepository() {

        CreateBranchDTO createBranchDTO = new CreateBranchDTO();
        createBranchDTO.setName("Main Branch2");
        createBranchDTO.setAddress("Address2");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(2L);
        User authUser = new User();
        authUser.setOfficialAgency(officialAgency);
        officialAgency.getUsers().add(authUser);

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main Branch");
        agencyBranch.setAddress("Address");
        agencyBranch.setOfficialAgency(officialAgency);
        when(mockAgencyBranchRepository.findById(1L))
                .thenReturn(Optional.of(agencyBranch));

        when(mockUserRepository.findById("12"))
                .thenReturn(Optional.of(authUser));

        officialAgencyService.deleteBranch(1L);
        verify(mockAgencyBranchRepository).deleteById(1L);

    }


}
