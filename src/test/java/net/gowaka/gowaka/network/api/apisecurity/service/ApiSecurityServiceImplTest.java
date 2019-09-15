package net.gowaka.gowaka.network.api.apisecurity.service;

import net.gowaka.gowaka.network.api.apisecurity.config.ApiSecurityConfig;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityClientUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUsernamePassword;
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
}
