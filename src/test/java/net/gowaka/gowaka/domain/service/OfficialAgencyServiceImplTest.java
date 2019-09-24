package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.OfficialAgency;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.OfficialAgencyService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 9:29 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class OfficialAgencyServiceImplTest {

    @Mock
    private OfficialAgencyRepository mockOfficialAgencyRepository;
    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private UserService mockUserService;
    @Mock
    private ApiSecurityService mockApiSecurityService;

    private ClientUserCredConfig clientUserCredConfig;

    private OfficialAgencyService officialAgencyService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.clientUserCredConfig = new ClientUserCredConfig();
        this.clientUserCredConfig.setClientId("client-id");
        this.clientUserCredConfig.setClientId("client-secret");
        this.clientUserCredConfig.setAppName("GoWaka");

        officialAgencyService = new OfficialAgencyServiceImpl(mockOfficialAgencyRepository, mockUserRepository, mockUserService, mockApiSecurityService, clientUserCredConfig);
    }

    @Test
    public void createOfficialAgency_call_OfficialAgencyRepository() {

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<OfficialAgency> officialAgencyArgumentCaptor = ArgumentCaptor.forClass(OfficialAgency.class);
        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);

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
    public void createOfficialAgency_throws_Exception_when_user_isAlready_inAn_Agency() {


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

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User already a member of an agency.");
        expectedException.expect(hasProperty("errorCode", is("USER_ALREADY_IN_AN_AGENCY")));

        officialAgencyService.createOfficialAgency(createOfficialAgencyDTO);

    }

    @Test
    public void assignAgencyUserRole_calls_ApiSecurityService() {

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
        officialAgencyUserRoleRequestDTO.setRoles(Arrays.asList("AGENCY_ADMIN", "AGENCY_MANAGER"));

        officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

        verify(mockApiSecurityService).updateUserInfo(idArgumentCaptor.capture(), fieldArgumentCaptor.capture(),
                roleArgumentCaptor.capture(), valueArgumentCaptor.capture());

        assertThat(idArgumentCaptor.getValue()).isEqualTo("12");
        assertThat(fieldArgumentCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleArgumentCaptor.getValue()).isEqualTo("USERS;AGENCY_ADMIN;AGENCY_MANAGER");
        assertThat(valueArgumentCaptor.getValue()).isEqualTo("jwt-token");

    }

    @Test
    public void assignAgencyUserRole_throw_Exception_when_User_not_inSameAgency_as_AuthUser() {

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

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User must be a member to your agency.");
        expectedException.expect(hasProperty("errorCode", is("USER_NOT_IN_AGENCY")));

        officialAgencyService.assignAgencyUserRole(officialAgencyUserRoleRequestDTO);

    }

    @Test
    public void getAgencyUsers_getAll_Users_in_Agency() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);

        OfficialAgency officialAgency = new OfficialAgency();
        User user1 = new User();
        user1.setUserId("10");
        User user2 = new User();
        user2.setUserId("11");
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
    public void addAgencyUsers_throw_Exception_when_authUser_NotFound() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.empty());

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User not found.");
        expectedException.expect(hasProperty("errorCode", is("RESOURCE_NOT_FOUND")));

        officialAgencyService.addAgencyUser(emailDTO);
    }

    @Test
    public void addAgencyUsers_throw_Exception_when_user_is_already_a_member_of_An_Agency() {

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

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User already a member of an agency.");
        expectedException.expect(hasProperty("errorCode", is("USER_ALREADY_IN_AN_AGENCY")));

        officialAgencyService.addAgencyUser(emailDTO);
    }

    @Test
    public void addAgencyUsers_call_OfficialAgencyRepository() {

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

        OfficialAgencyUserDTO officialAgencyUserDTO = officialAgencyService.addAgencyUser(emailDTO);
        verify(mockUserService).getCurrentAuthUser();
        verify(mockUserRepository).findById("12");
        verify(mockApiSecurityService).getUserByUsername("example@example.com", "jwt-token");
        verify(mockUserRepository).save(any());
        assertThat(officialAgencyUserDTO.getId()).isEqualTo("10");
        assertThat(officialAgencyUserDTO.getFullName()).isEqualTo("John Doe");
        assertThat(officialAgencyUserDTO.getRoles()).isEqualTo(Arrays.asList("USERS"));

        assertThat(user.getOfficialAgency()).isEqualTo(officialAgency);
    }
}