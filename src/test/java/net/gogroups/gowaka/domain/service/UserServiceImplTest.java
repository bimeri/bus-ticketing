package net.gogroups.gowaka.domain.service;


import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.security.accessconfig.AppGrantedAuthority;
import net.gogroups.security.accessconfig.JwtTokenProvider;
import net.gogroups.security.accessconfig.UserDetailsImpl;
import net.gogroups.security.model.*;
import net.gogroups.security.service.ApiSecurityService;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:57 PM <br/>
 */
@ExtendWith(MockitoExtension.class)
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

    ArgumentCaptor<ApiSecurityUser> apiSecurityUserArgumentCaptor;
    ArgumentCaptor<String> stringArgumentCaptor;
    ArgumentCaptor<User> userArgumentCaptor;
    ArgumentCaptor<ApiSecurityChangePassword> apiSecurityChangePasswordArgumentCaptor;
    ArgumentCaptor<ApiSecurityForgotPassword> apiSecurityForgotPasswordArgumentCaptor;
    ArgumentCaptor<ApiSecurityUsernamePassword> apiSecurityUsernamePasswordArgumentCaptor;
    ArgumentCaptor<ApiSecurityVerifyEmail> apiSecurityVerifyEmailArgumentCaptor;

    @BeforeEach
    void setUp() {

        this.clientUserCredConfig = new ClientUserCredConfig();
        this.clientUserCredConfig.setClientId("client-id");
        this.clientUserCredConfig.setClientId("client-secret");
        this.clientUserCredConfig.setAppName("GoWaka");
        this.clientUserCredConfig.setMobileLoginMilli(222222L);

        userService = new UserServiceImpl(mockUserRepository, mockApiSecurityService, clientUserCredConfig, mockJwtTokenProvider, mockEmailContentBuilder);

        apiSecurityUserArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityUser.class);
        stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        apiSecurityUsernamePasswordArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityUsernamePassword.class);
        ((UserServiceImpl) userService).setNotificationService(mockNotificationService);

    }

    @Test
    void createUser_calls_UserRepository_ApiSecurityService_with_proper_Values() {

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
    void loginUser_calls_ApiSecurityService_forWEB() {

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
        user.setCode("123");
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));

        TokenDTO tokenDTO = userService.loginUser(emailPasswordDTO, "WEB");

        verify(mockApiSecurityService).getUserToken(any());
        assertThat(tokenDTO.getAccessToken()).isEqualTo("jwt-token");
        assertThat(tokenDTO.getHeader()).isEqualTo("Authorization");
        assertThat(tokenDTO.getIssuer()).isEqualTo("Api-Security");
        assertThat(tokenDTO.getType()).isEqualTo("Bearer");
        assertThat(tokenDTO.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(tokenDTO.getExpiredIn()).isEqualTo(1000L);
        assertThat(tokenDTO.getUserDetails().toString()).isEqualTo("UserDTO(id=1111, fullName=Full Name, email=example@example.com, roles=[USERS, AGENCY], phoneNumber=null, idCardNumber=null, qrCodeImage=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA1ElEQVR42u3XQQ6EIAwF0LriGNxU4aY9Biv+0IIxk2HWfBNIF+jbNJY2KPi3ZMuWLe+QIm0dQC4x2TYyiW2zb/L9yCNJYvbXuchBKD1rUtFTGMWrrRIwPwcrxbvEPue8f1ZKX71FZnNnpViFLVkrdW9lHgFqQG0W9Ax6lUglo87hPoxEogf0sg6WFl9ZM4gPleRZJ6GSp0tapAAiGdPF53GJlUt8Kt/tm8jEbwF2BrMHncBGiwfoZHSJzu5VK6VXW/p0AZhk3ALqb9bLZf/PbdnyUvkAgRJ55KwjjG0AAAAASUVORK5CYII=, token=null)");

    }

    @Test
    void loginUser_calls_ApiSecurityService_forMOBILE() {

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

        when(mockApiSecurityService.getUserToken(any(), anyLong()))
                .thenReturn(accessToken);
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setFullName("Full Name");
        userDetails.setUsername("example@example.com");
        userDetails.setId("1111");
        userDetails.setAuthorities(Arrays.asList(new AppGrantedAuthority("USERS"), new AppGrantedAuthority("AGENCY")));
        when(mockJwtTokenProvider.getUserDetails(any()))
                .thenReturn(userDetails);
        User user = new User();
        user.setCode("123");
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));

        TokenDTO tokenDTO = userService.loginUser(emailPasswordDTO, "MOBILE");
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(mockApiSecurityService).getUserToken(apiSecurityUsernamePasswordArgumentCaptor.capture(), longArgumentCaptor.capture());
        assertThat(tokenDTO.getAccessToken()).isEqualTo("jwt-token");
        assertThat(tokenDTO.getHeader()).isEqualTo("Authorization");
        assertThat(tokenDTO.getIssuer()).isEqualTo("Api-Security");
        assertThat(tokenDTO.getType()).isEqualTo("Bearer");
        assertThat(tokenDTO.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(tokenDTO.getExpiredIn()).isEqualTo(1000L);
        assertThat(longArgumentCaptor.getValue()).isEqualTo(222222L);
        assertThat(apiSecurityUsernamePasswordArgumentCaptor.getValue().getUsername()).isEqualTo("example@example.com");
        assertThat(apiSecurityUsernamePasswordArgumentCaptor.getValue().getPassword()).isEqualTo("secret");
        assertThat(apiSecurityUsernamePasswordArgumentCaptor.getValue().getAppName()).isEqualTo("GoWaka");
        assertThat(tokenDTO.getUserDetails().toString()).isEqualTo("UserDTO(id=1111, fullName=Full Name, email=example@example.com, roles=[USERS, AGENCY], phoneNumber=null, idCardNumber=null, qrCodeImage=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAAA1ElEQVR42u3XQQ6EIAwF0LriGNxU4aY9Biv+0IIxk2HWfBNIF+jbNJY2KPi3ZMuWLe+QIm0dQC4x2TYyiW2zb/L9yCNJYvbXuchBKD1rUtFTGMWrrRIwPwcrxbvEPue8f1ZKX71FZnNnpViFLVkrdW9lHgFqQG0W9Ax6lUglo87hPoxEogf0sg6WFl9ZM4gPleRZJ6GSp0tapAAiGdPF53GJlUt8Kt/tm8jEbwF2BrMHncBGiwfoZHSJzu5VK6VXW/p0AZhk3ALqb9bLZf/PbdnyUvkAgRJ55KwjjG0AAAAASUVORK5CYII=, token=null)");

    }


    @Test
    void getNewToken_calls_ApiSecurityService() {

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
    void changeUserPassword_calls_ApiSecurityService() {
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
    void forgotUserPassword_calls_ApiSecurityService() {
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
    void getCurrentAuthUser_getUserInfo_from_securityContextHolder() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Collections.singletonList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        assertThat(currentAuthUser.getId()).isEqualTo("12");
        assertThat(currentAuthUser.getEmail()).isEqualTo("example@example.com");
        assertThat(currentAuthUser.getFullName()).isEqualTo("Jesus Christ");
        assertThat(currentAuthUser.getRoles()).isEqualTo(Collections.singletonList("users"));

    }

    @Test
    void updateProfile_throwException_whenAuthUserNotFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Collections.singletonList(new AppGrantedAuthority("users")));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));

        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.empty());

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();

        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(updateProfileDTO));
        verify(mockUserRepository).findById("12");
        assertThat(resourceNotFoundException.getMessage()).isEqualTo("User not found.");

    }

    @Test
    void updateProfile_updateIDCardAndPhoneNumberButNotFullName_whenAuthUserFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Collections.singletonList(new AppGrantedAuthority("users")));

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
        verifyNoInteractions(mockApiSecurityService);

        User userValue = userArgumentCaptor.getValue();
        assertThat(userValue.getIdCardNumber()).isEqualTo("123456789");
        assertThat(userValue.getPhoneNumber()).isEqualTo("67676767676");
    }

    @Test
    void updateProfile_updateIDCardAndPhoneNumberAndFullName_whenAuthUserFound() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("12");
        userDetails.setFullName("Jesus Christ");
        userDetails.setUsername("example@example.com");
        userDetails.setPassword("secret");
        userDetails.setAuthorities(Collections.singletonList(new AppGrantedAuthority("users")));

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
    void verifyEmail_calls_ApiSecurityService() {

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("example@example.com");

        apiSecurityVerifyEmailArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityVerifyEmail.class);
        userService.verifyEmail(emailDTO);

        verify(mockApiSecurityService).verifyEmail(apiSecurityVerifyEmailArgumentCaptor.capture());

        ApiSecurityVerifyEmail value = apiSecurityVerifyEmailArgumentCaptor.getValue();
        assertThat(value.getApplicationName()).isEqualTo(clientUserCredConfig.getAppName());
        assertThat(value.getUsername()).isEqualTo("example@example.com");
    }

    @Test
    public void validateGWUserByEmail_whenEmailInvalid_shouldThrowResourceNotFoundException(){
        when(mockUserRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());
        EmailDTO dto = new EmailDTO();
        dto.setEmail("example@example.com");
        ApiException apiException = assertThrows(ApiException.class,
                () -> userService.validateGWUserByEmail(dto));
        MatcherAssert.assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        MatcherAssert.assertThat(apiException.getMessage(), is(ErrorCodes.RESOURCE_NOT_FOUND.getMessage()));
    }

    @Test
    public void validateGWUserByEmail_whenEmailValid_shouldReturnGWUserDTO() {
        User you = new User();
        you.setEmail("you@example.com");
        you.setFullName("you");
        EmailDTO dto = new EmailDTO();
        dto.setEmail(you.getEmail());
        when(mockUserRepository.findFirstByEmail(anyString())).thenReturn(Optional.of(you));
        GWUserDTO userDTO = userService.validateGWUserByEmail(dto);
        verify(mockUserRepository).findFirstByEmail(stringArgumentCaptor.capture());
        String emailChecked = stringArgumentCaptor.getValue();
        assertThat(emailChecked).isEqualTo(dto.getEmail());
        assertThat(userDTO.getEmail()).isEqualTo(you.getEmail());
        assertThat(userDTO.getFullName()).isEqualTo(you.getFullName());
    }

    @Test
    public void getAccountInfo_whenEmailInvalid_shouldThrowResourceNotFoundException(){
        when(mockUserRepository.findFirstByCode(anyString())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class,
                () -> userService.getAccountInfo("1234"));
        MatcherAssert.assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        MatcherAssert.assertThat(apiException.getMessage(), is(ErrorCodes.RESOURCE_NOT_FOUND.getMessage()));
    }

    @Test
    public void getAccountInfo_whenEmailValid_shouldReturnGWUserDTO() {
        User you = new User();
        you.setEmail("you@example.com");
        you.setFullName("you");

        when(mockUserRepository.findFirstByCode(anyString())).thenReturn(Optional.of(you));
        GWAccountDTO userDTO = userService.getAccountInfo("1234");
        verify(mockUserRepository).findFirstByCode(stringArgumentCaptor.capture());
        String emailChecked = stringArgumentCaptor.getValue();
        assertThat(emailChecked).isEqualTo("1234");
        assertThat(userDTO.getEmail()).isEqualTo(you.getEmail());
        assertThat(userDTO.getName()).isEqualTo(you.getFullName());
    }

}
