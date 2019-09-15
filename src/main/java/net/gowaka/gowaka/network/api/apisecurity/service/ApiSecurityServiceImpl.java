package net.gowaka.gowaka.network.api.apisecurity.service;

import net.gowaka.gowaka.network.api.apisecurity.config.ApiSecurityConfig;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityClientUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUsernamePassword;
import net.gowaka.gowaka.service.ApiSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:20 PM <br/>
 */
@Component
public class ApiSecurityServiceImpl implements ApiSecurityService {

    private ApiSecurityConfig apiSecurityConfig;

    @Qualifier("apiSecurityRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    public ApiSecurityServiceImpl(ApiSecurityConfig apiSecurityConfig, RestTemplate restTemplate) {
        this.apiSecurityConfig = apiSecurityConfig;
        this.restTemplate = restTemplate;
    }


    @Override
    public ApiSecurityAccessToken getClientToken(ApiSecurityClientUser apiSecurityClientUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String url = apiSecurityConfig.getHost() + ":" + apiSecurityConfig.getPort() + apiSecurityConfig.getClientAuthorizationPath();

        HttpEntity<ApiSecurityClientUser> request = new HttpEntity<>(apiSecurityClientUser,headers);

        return restTemplate.exchange(url, HttpMethod.POST, request, ApiSecurityAccessToken.class).getBody();

    }

    @Override
    public ApiSecurityAccessToken getUserToken(ApiSecurityUsernamePassword apiSecurityUsernamePassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String url = apiSecurityConfig.getHost() + ":" + apiSecurityConfig.getPort() + apiSecurityConfig.getUserAuthorizationPath();

        HttpEntity<ApiSecurityUsernamePassword> request = new HttpEntity<>(apiSecurityUsernamePassword,headers);

        return restTemplate.exchange(url, HttpMethod.POST, request, ApiSecurityAccessToken.class).getBody();

    }

    @Override
    public ApiSecurityUser registerUser(ApiSecurityUser apiSecurityUser, String clientToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(clientToken);
        headers.set("grant_type", "client_credentials");
        String url = apiSecurityConfig.getHost() + ":" + apiSecurityConfig.getPort() + apiSecurityConfig.getRegisterUserPath();

        HttpEntity<ApiSecurityUser> request = new HttpEntity<>(apiSecurityUser,headers);

        return restTemplate.exchange(url, HttpMethod.POST, request, ApiSecurityUser.class).getBody();

    }


}
