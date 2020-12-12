package net.gogroups.gowaka.domain.service;


import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.security.accessconfig.AppGrantedAuthority;
import net.gogroups.security.accessconfig.JwtTokenProvider;
import net.gogroups.security.accessconfig.UserDetailsImpl;
import net.gogroups.security.model.*;
import net.gogroups.security.service.ApiSecurityService;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:57 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private ApiSecurityService mockApiSecurityService;
    @Mock
    private EmailContentBuilder mockEmailContentBuilder;
    private ClientUserCredConfig clientUserCredConfig;
    private UserService userService;
    @Mock
    private JwtTokenProvider mockJwtTokenProvider;
    @Mock
    private NotificationService mockNotificationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    ArgumentCaptor<ApiSecurityUser> apiSecurityUserArgumentCaptor;
    ArgumentCaptor<String> stringArgumentCaptor;
    ArgumentCaptor<User> userArgumentCaptor;
    ArgumentCaptor<ApiSecurityChangePassword> apiSecurityChangePasswordArgumentCaptor;
    ArgumentCaptor<ApiSecurityForgotPassword> apiSecurityForgotPasswordArgumentCaptor;
    ArgumentCaptor<ApiSecurityVerifyEmail> apiSecurityVerifyEmailArgumentCaptor;

    @Before
    public void setUp() {

        this.clientUserCredConfig = new ClientUserCredConfig();
        this.clientUserCredConfig.setClientId("client-id");
        this.clientUserCredConfig.setClientId("client-secret");
        this.clientUserCredConfig.setAppName("GoWaka");

        userService = new UserServiceImpl(mockUserRepository, mockApiSecurityService, clientUserCredConfig, mockJwtTokenProvider, mockEmailContentBuilder);

        apiSecurityUserArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityUser.class);
        stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ((UserServiceImpl) userService).setNotificationService(mockNotificationService);

    }

    @Test
    public void createUser_calls_UserRepository_ApiSecurityService_with_proper_Values() {

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("example@example.com");
        createUserRequest.setFullName("Jesus Christ");
        createUserRequest.setPassword("secret");

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        apiSecurityUser.setToken(new ApiSecurityAccessToken());

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");

        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);
        when(mockApiSecurityService.registerUser(any(), anyString()))
                .thenReturn(apiSecurityUser);

        UserDTO userDTO = userService.createUser(createUserRequest);

        verify(mockApiSecurityService).registerUser(apiSecurityUserArgumentCaptor.capture(), stringArgumentCaptor.capture());

        ApiSecurityUser apiSecurityUserValue = apiSecurityUserArgumentCaptor.getValue();
        String TokenValue = stringArgumentCaptor.getValue();

        assertThat(apiSecurityUserValue.getRoles()).isEqualTo("USERS");
        assertThat(apiSecurityUserValue.getEmail()).isEqualTo("example@example.com");
        assertThat(apiSecurityUserValue.getFullName()).isEqualTo("Jesus Christ");
        assertThat(apiSecurityUserValue.getUsername()).isEqualTo("example@example.com");
        assertThat(apiSecurityUserValue.getPassword()).isEqualTo("secret");
        assertThat(TokenValue).isEqualTo("jwt-token");

        verify(mockUserRepository).save(userArgumentCaptor.capture());
        User userValue = userArgumentCaptor.getValue();
        assertThat(userValue.getUserId()).isEqualTo("12");

        assertThat(userDTO.getId()).isEqualTo("12");
        assertThat(userDTO.getFullName()).isEqualTo("Jesus Christ");
        assertThat(userDTO.getEmail()).isEqualTo("example@example.com");
        assertThat(userDTO.getRoles().size()).isEqualTo(1);
        assertThat(userDTO.getRoles().get(0)).isEqualTo("USERS");

    }


    @Test
    public void loginUser_calls_ApiSecurityService() {

        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        emailPasswordDTO.setEmail("example@example.com");
        emailPasswordDTO.setPassword("secret");

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        accessToken.setHeader("Authorization");
        accessToken.setIssuer("Api-Security");
        accessToken.setType("Bearer");
        accessToken.setRefreshToken("refresh-token");
        accessToken.setExpiredIn(1000L);
        accessToken.setVersion("v1");

        when(mockApiSecurityService.getUserToken(any()))
                .thenReturn(accessToken);
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setFullName("Full Name");
        userDetails.setUsername("example@example.com");
        userDetails.setId("1111");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("USERS"), new AppGrantedAuthority("AGENCY")));
        when(mockJwtTokenProvider.getUserDetails(any()))
                .thenReturn(userDetails);
        User user = new User();
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));

        TokenDTO tokenDTO = userService.loginUser(emailPasswordDTO);

        verify(mockApiSecurityService).getUserToken(any());
        assertThat(tokenDTO.getAccessToken()).isEqualTo("jwt-token");
        assertThat(tokenDTO.getHeader()).isEqualTo("Authorization");
        assertThat(tokenDTO.getIssuer()).isEqualTo("Api-Security");
        assertThat(tokenDTO.getType()).isEqualTo("Bearer");
        assertThat(tokenDTO.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(tokenDTO.getExpiredIn()).isEqualTo(1000L);
        assertThat(tokenDTO.getUserDetails().toString()).isEqualTo("UserDTO(id=1111, fullName=Full Name, email=example@example.com, roles=[USERS, AGENCY], phoneNumber=null, idCardNumber=null, token=null)");

    }

    @Test
    public void getNewToken_calls_ApiSecurityService() {

        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
        refreshTokenDTO.setRefreshToken("refresh-token-1");

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        accessToken.setHeader("Authorization");
        accessToken.setIssuer("Api-Security");
        accessToken.setType("Bearer");
        accessToken.setRefreshToken("refresh-token");
        accessToken.setExpiredIn(1000L);
        accessToken.setVersion("v1");

        when(mockApiSecurityService.getNewUserToken(any()))
                .thenReturn(accessToken);

        TokenDTO tokenDTO = userService.getNewToken(refreshTokenDTO);

        verify(mockApiSecurityService).getNewUserToken(any());
        assertThat(tokenDTO.getAccessToken()).isEqualTo("jwt-token");
        assertThat(tokenDTO.getHeader()).isEqualTo("Authorization");
        assertThat(tokenDTO.getIssuer()).isEqualTo("Api-Security");
        assertThat(tokenDTO.getType()).isEqualTo("Bearer");
        assertThat(tokenDTO.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(tokenDTO.getExpiredIn()).isEqualTo(1000L);
        assertThat(tokenDTO.getUserDetails()).isEqualTo(null);

    }

    @Test
    public void changeUserPassword_calls_ApiSecurityService() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setEmail("example@example.com");
        changePasswordDTO.setOldPassword("secret");
        changePasswordDTO.setPassword("new-secret");

        apiSecurityChangePasswordArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityChangePassword.class);

        userService.changeUserPassword(changePasswordDTO);

        verify(mockApiSecurityService).changePassword(apiSecurityChangePasswordArgumentCaptor.capture());

        ApiSecurityChangePassword apiSecurityChangePasswordValue = apiSecurityChangePasswordArgumentCaptor.getValue();
        assertThat(apiSecurityChangePasswordValue.getUsername()).isEqualTo("example@example.com");
        assertThat(apiSecurityChangePasswordValue.getOldPassword()).isEqualTo("secret");
        assertThat(apiSecurityChangePasswordValue.getPassword()).isEqualTo("new-secret");

    }

    @Test
    public void forgotUserPassword_calls_ApiSecurityService() {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        apiSecurityForgotPasswordArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityForgotPassword.class);

        userService.forgotUserPassword(emailDTO);

        verify(mockApiSecurityService).forgotPassword(apiSecurityForgotPasswordArgumentCaptor.capture());

        ApiSecurityForgotPassword value = apiSecurityForgotPasswordArgumentCaptor.getValue();
        assertThat(value.getApplicationName()).isEqualTo(clientUserCredConfig.getAppName());
        assertThat(value.getUsername()).isEqualTo("example@example.com");
    }

    @Test
    public void getCurrentAuthUser_getUserInfo_from_securityContextHolder() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        assertThat(currentAuthUser.getId()).isEqualTo("12");
        assertThat(currentAuthUser.getEmail()).isEqualTo("example@example.com");
        assertThat(currentAuthUser.getFullName()).isEqualTo("Jesus Christ");
        assertThat(currentAuthUser.getRoles()).isEqualTo(Arrays.asList("users"));

    }

    @Test
    public void updateProfile_throwException_whenAuthUserNotFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.empty());

        expectedException.expect(ResourceNotFoundException.class);
        expectedException.expectMessage("User not found.");

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        userService.updateProfile(updateProfileDTO);
        verify(mockUserRepository).findById("12");
    }

    @Test
    public void updateProfile_updateIDCardAndPhoneNumberButNotFullName_whenAuthUserFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        User user = new User();
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setFullName("Jesus Christ");
        updateProfileDTO.setIdCardNumber("123456789");
        updateProfileDTO.setPhoneNumber("67676767676");
        userService.updateProfile(updateProfileDTO);
        verify(mockUserRepository).findById("12");
        verify(mockUserRepository).save(userArgumentCaptor.capture());
        verifyZeroInteractions(mockApiSecurityService);

        User userValue = userArgumentCaptor.getValue();
        assertThat(userValue.getIdCardNumber()).isEqualTo("123456789");
        assertThat(userValue.getPhoneNumber()).isEqualTo("67676767676");
    }

    @Test
    public void updateProfile_updateIDCardAndPhoneNumberAndFullName_whenAuthUserFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        User user = new User();
        user.setUserId("12");
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));
        ApiSecurityAccessToken token = new ApiSecurityAccessToken();
        token.setToken("my-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(token);

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setFullName("New Name");
        updateProfileDTO.setIdCardNumber("123456789");
        updateProfileDTO.setPhoneNumber("67676767676");
        userService.updateProfile(updateProfileDTO);
        verify(mockUserRepository).findById("12");
        verify(mockUserRepository).save(userArgumentCaptor.capture());
        verify(mockApiSecurityService).updateUserInfo("12", "FULL_NAME", "New Name", "my-token");

        User userValue = userArgumentCaptor.getValue();
        assertThat(userValue.getIdCardNumber()).isEqualTo("123456789");
        assertThat(userValue.getPhoneNumber()).isEqualTo("67676767676");
    }

    @Test
    public void verifyEmail_calls_ApiSecurityService() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        apiSecurityVerifyEmailArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityVerifyEmail.class);
        userService.verifyEmail(emailDTO);

        verify(mockApiSecurityService).verifyEmail(apiSecurityVerifyEmailArgumentCaptor.capture());

        ApiSecurityVerifyEmail value = apiSecurityVerifyEmailArgumentCaptor.getValue();
        assertThat(value.getApplicationName()).isEqualTo(clientUserCredConfig.getAppName());
        assertThat(value.getUsername()).isEqualTo("example@example.com");
    }


}
