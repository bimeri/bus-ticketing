package net.gowaka.gowaka.network.api.apisecurity.service;

import net.gowaka.gowaka.network.api.apisecurity.config.ApiSecurityConfig;
import net.gowaka.gowaka.network.api.apisecurity.model.*;
import net.gowaka.gowaka.service.ApiSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 7:08 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiSecurityServiceImplTest {

    private ApiSecurityConfig apiSecurityConfig;
    @Mock
    private RestTemplate mockRestTemplate;

    private ApiSecurityService apiSecurityService;

    ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor;
    ArgumentCaptor<String> stringArgumentCaptor;
    ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor;
    ArgumentCaptor<Class> classArgumentCaptor;


    @Before
    public void setUp() throws Exception {
        apiSecurityConfig = new ApiSecurityConfig();
        apiSecurityConfig.setHost("http://localhost");
        apiSecurityConfig.setPort("8080");
        apiSecurityConfig.setClientAuthorizationPath("/api/public/v1/clients/authorized");
        apiSecurityConfig.setUserAuthorizationPath("/api/public/v1/users/authorized");
        apiSecurityConfig.setRegisterUserPath("/api/protected/v1/users");
        apiSecurityConfig.setChangeUserPasswordPath("/api/public/v1/users/password");
        apiSecurityConfig.setForgotPasswordPath("/api/public/v1/users/otp");
        apiSecurityConfig.setGetUserByUsernamePath("/api/public/v1/users");
        apiSecurityConfig.setGetUserByUserIdPath("/api/public/v1/users/{userId}");
        apiSecurityConfig.setUpdateUserInfo("/api/protected/v1/users/{userId}/{field}");
        apiSecurityService = new ApiSecurityServiceImpl(apiSecurityConfig, mockRestTemplate);

        httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
    }

    @Test
    public void getClientToken_calls_RestTemplate() {
        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId("client-id");
        apiSecurityClientUser.setClientSecret("client-secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiSecurityClientUser> expectedRequest = new HttpEntity<>(apiSecurityClientUser,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityAccessToken(), HttpStatus.OK));

        apiSecurityService.getClientToken(apiSecurityClientUser);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/clients/authorized");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(ApiSecurityAccessToken.class);

    }

    @Test
    public void getUserToken_calls_RestTemplate() {
        ApiSecurityUsernamePassword apiSecurityUsernamePassword = new ApiSecurityUsernamePassword();
        apiSecurityUsernamePassword.setUsername("username");
        apiSecurityUsernamePassword.setPassword("secret");
        apiSecurityUsernamePassword.setAppName("GoWaka");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiSecurityUsernamePassword> expectedRequest = new HttpEntity<>(apiSecurityUsernamePassword,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityAccessToken(), HttpStatus.OK));

        apiSecurityService.getUserToken(apiSecurityUsernamePassword);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/users/authorized");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(ApiSecurityAccessToken.class);

    }

    @Test
    public void registerUser_calls_RestTemplate() {

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setFullName("Jesus Christ");
        apiSecurityUser.setPassword("secret");
        apiSecurityUser.setRoles("users;agency");
        apiSecurityUser.setUsername("jchrist");

        String token  = "jwt-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);
        headers.set("grant_type", "client_credentials");
        HttpEntity<ApiSecurityUser> expectedRequest = new HttpEntity<>(apiSecurityUser,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityUser(), HttpStatus.OK));

        apiSecurityService.registerUser(apiSecurityUser, token);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/v1/users");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(ApiSecurityUser.class);

    }

    @Test
    public void changePassword_calls_RestTemplate() {

        ApiSecurityChangePassword apiSecurityChangePassword = new ApiSecurityChangePassword();
        apiSecurityChangePassword.setUsername("example@example.com");
        apiSecurityChangePassword.setPassword("secret");
        apiSecurityChangePassword.setOldPassword("new-secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiSecurityChangePassword> expectedRequest = new HttpEntity<>(apiSecurityChangePassword,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityChangePassword(), HttpStatus.NO_CONTENT));

        apiSecurityService.changePassword(apiSecurityChangePassword);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/users/password");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(Void.class);

    }

    @Test
    public void forgotPassword_calls_RestTemplate() {

        ApiSecurityForgotPassword apiSecurityChangePassword = new ApiSecurityForgotPassword();
        apiSecurityChangePassword.setUsername("example@example.com");
        apiSecurityChangePassword.setApplicationName("GoWaka");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<ApiSecurityForgotPassword> expectedRequest = new HttpEntity<>(apiSecurityChangePassword,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityForgotPassword(), HttpStatus.NO_CONTENT));

        apiSecurityService.forgotPassword(apiSecurityChangePassword);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/users/otp");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(Void.class);

    }

    @Test
    public void getUserByUsername_calls_RestTemplate() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth("token");
        headers.set("grant_type", "client_credentials");
        HttpEntity<Void> expectedRequest = new HttpEntity<>(null,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityUser(), HttpStatus.OK));

        apiSecurityService.getUserByUsername("example@example.com", "token");

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/users?username=example@example.com");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(ApiSecurityUser.class);


    }

    @Test
    public void getUserByUserId_calls_RestTemplate() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth("token");
        headers.set("grant_type", "client_credentials");
        HttpEntity<Void> expectedRequest = new HttpEntity<>(null,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new ApiSecurityUser(), HttpStatus.OK));

        apiSecurityService.getUserByUserId("12", "token");

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/v1/users/12");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(ApiSecurityUser.class);


    }

    @Test
    public void updateUserInfo_calls_RestTemplate() {

        String userId = "12";
        String field = "ROLES";
        String value = "user;agency_user";
        String token  = "jwt-token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);
        headers.set("grant_type", "client_credentials");
        HttpEntity<Void> expectedRequest = new HttpEntity<>(null,headers);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class),any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        apiSecurityService.updateUserInfo(userId,field, value, token);

        verify(mockRestTemplate).exchange(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                httpEntityArgumentCaptor.capture(), classArgumentCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/v1/users/12/ROLES?value=user;agency_user");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.PATCH);
        assertThat(httpEntityArgumentCaptor.getValue()).isEqualTo(expectedRequest);
        assertThat(classArgumentCaptor.getValue()).isEqualTo(Void.class);


    }
}
