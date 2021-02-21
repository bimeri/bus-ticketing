package net.gogroups.gowaka.network.api.cbs.service;

import net.gogroups.gowaka.network.api.cbs.config.CBSProps;
import net.gogroups.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gogroups.gowaka.network.api.cbs.model.CBSRewardPointDTO;
import net.gogroups.gowaka.service.CBSService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@ExtendWith(MockitoExtension.class)
public class CBSServiceImplTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private CBSService cbsService;

    private final ArgumentCaptor<String> strArgumentCaptor = ArgumentCaptor.forClass(String.class);
    private final ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    private final ArgumentCaptor<HttpMethod> methodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    private ArgumentCaptor<Class> clazzCaptor = ArgumentCaptor.forClass(Class.class);

    @BeforeEach
    void setUp() {
        CBSProps cbsProps = new CBSProps();
        cbsProps.setPort("8080");
        cbsProps.setHost("http://localhost");
        cbsProps.setUserBenefitsPath("/api/protected/benefits/users");
        cbsProps.setUserRewardPointsPath("/api/protected/reward_points/users");

        cbsService = new CBSServiceImpl(mockRestTemplate, cbsProps);
    }

    @Test
    void getAllUserAvailableBenefit_callsRestTemplate() {

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(Arrays.asList(new CBSBenefitDTO())));

        cbsService.getAllUserAvailableBenefit("my-token");

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/benefits/users");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer my-token");
        assertThat(clazzCaptor.getValue()).isEqualTo(List.class);
    }

    @Test
    void getUserRewardPoints_callsRestTemplate() {

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(new CBSRewardPointDTO()));

        cbsService.getUserRewardPoints("my-token");
        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/reward_points/users");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer my-token");
        assertThat(clazzCaptor.getValue()).isEqualTo(CBSRewardPointDTO.class);
    }


}
