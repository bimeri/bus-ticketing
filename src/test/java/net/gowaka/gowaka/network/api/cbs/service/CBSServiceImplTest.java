package net.gowaka.gowaka.network.api.cbs.service;

import net.gowaka.gowaka.network.api.cbs.config.CBSProps;
import net.gowaka.gowaka.network.api.cbs.model.CBSAccessToken;
import net.gowaka.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gowaka.gowaka.network.api.cbs.model.CBSEmailPassword;
import net.gowaka.gowaka.network.api.cbs.service.CBSServiceImpl;
import net.gowaka.gowaka.service.CBSService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 6:19 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class CBSServiceImplTest {

    @Mock
    private RestTemplate mockRestTemplate;
    private CBSProps cbsProps;

    private CBSService cbsService;

    private ArgumentCaptor<String> strArgumentCaptor = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    private ArgumentCaptor<HttpMethod> methodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    private ArgumentCaptor<Class> clazzCaptor = ArgumentCaptor.forClass(Class.class);

    @Before
    public void setUp() {
        cbsProps = new CBSProps();
        cbsProps.setEmail("example@example.com");
        cbsProps.setPort("8080");
        cbsProps.setHost("http://localhost");
        cbsProps.setPassword("secret");
        cbsProps.setAvailableBenefitsPath("/api/protected/benefits");
        cbsProps.setUserBenefitsPath("/api/protected/benefits/users?gowakaUserId=%s");
        cbsProps.setLoginPath("/api/public/login");

        cbsService = new CBSServiceImpl(mockRestTemplate, cbsProps);
    }

    @Test
    public void getAllAvailableBenefit_callsRestTemplate() {

        CBSAccessToken cbsAccessToken = new CBSAccessToken();
        cbsAccessToken.setAccessToken("my-token");
        cbsAccessToken.setIssuer("APISecurity");

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(cbsAccessToken));

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(Arrays.asList(new CBSBenefitDTO())));
        cbsService.getAllAvailableBenefit();

        verify(mockRestTemplate).postForEntity(strArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/login");
        assertThat(((CBSEmailPassword) entityArgumentCaptor.getValue().getBody()).getEmail()).isEqualTo("example@example.com");
        assertThat(((CBSEmailPassword) entityArgumentCaptor.getValue().getBody()).getPassword()).isEqualTo("secret");
        assertThat(clazzCaptor.getValue()).isEqualTo(CBSAccessToken.class);

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/benefits");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer my-token");
        assertThat(clazzCaptor.getValue()).isEqualTo(List.class);

    }

    @Test
    public void getAllUserAvailableBenefit_callsRestTemplate() {

        CBSAccessToken cbsAccessToken = new CBSAccessToken();
        cbsAccessToken.setAccessToken("my-token");
        cbsAccessToken.setIssuer("APISecurity");

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(cbsAccessToken));

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(Arrays.asList(new CBSBenefitDTO())));

        cbsService.getAllUserAvailableBenefit("12345");

        verify(mockRestTemplate).postForEntity(strArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/login");
        assertThat(((CBSEmailPassword) entityArgumentCaptor.getValue().getBody()).getEmail()).isEqualTo("example@example.com");
        assertThat(((CBSEmailPassword) entityArgumentCaptor.getValue().getBody()).getPassword()).isEqualTo("secret");
        assertThat(clazzCaptor.getValue()).isEqualTo(CBSAccessToken.class);

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/benefits/users?gowakaUserId=12345");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer my-token");
        assertThat(clazzCaptor.getValue()).isEqualTo(List.class);
    }


}
