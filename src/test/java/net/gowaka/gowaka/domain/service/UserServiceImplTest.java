package net.gowaka.gowaka.domain.service;


import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityChangePassword;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ClientUserCredConfig clientUserCredConfig;
    private UserService userService;

    ArgumentCaptor<ApiSecurityUser> apiSecurityUserArgumentCaptor;
    ArgumentCaptor<String> stringArgumentCaptor;
    ArgumentCaptor<User> userArgumentCaptor;
    ArgumentCaptor<ApiSecurityChangePassword> apiSecurityChangePasswordArgumentCaptor;

    @Before
    public void setUp() {

        this.clientUserCredConfig = new ClientUserCredConfig();
        this.clientUserCredConfig.setClientId("client-id");
        this.clientUserCredConfig.setClientId("client-secret");
        this.clientUserCredConfig.setAppName("GoWaka");

        userService = new UserServiceImpl(mockUserRepository, mockApiSecurityService, clientUserCredConfig);

        apiSecurityUserArgumentCaptor =ArgumentCaptor.forClass(ApiSecurityUser.class);
        stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        userArgumentCaptor = ArgumentCaptor.forClass(User.class);

    }

    @Test
    public void createUser_calls_UserRepository_ApiSecurityService_with_proper_Values() {

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("example@example.com");
        createUserRequest.setFullName("Jesus Christ");
        createUserRequest.setPassword("secret");

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");

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

        assertThat(apiSecurityUserValue.getRoles()).isEqualTo("users");
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
        assertThat(userDTO.getRoles().get(0)).isEqualTo("users");

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
        accessToken.setVersion("v1");

        when(mockApiSecurityService.getUserToken(any()))
                .thenReturn(accessToken);

        TokenDTO tokenDTO = userService.loginUser(emailPasswordDTO);

        verify(mockApiSecurityService).getUserToken(any());
        assertThat(tokenDTO.getAccessToken()).isEqualTo("jwt-token");
        assertThat(tokenDTO.getHeader()).isEqualTo("Authorization");
        assertThat(tokenDTO.getIssuer()).isEqualTo("Api-Security");
        assertThat(tokenDTO.getType()).isEqualTo("Bearer");


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
}