package net.gogroups.gowaka.network.api.cache.service;

import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.network.api.cache.service.config.GwCacheServiceProps;
import net.gogroups.gowaka.service.GwCacheLoaderService;
import net.gogroups.security.service.AuthorizedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static net.gogroups.security.Constants.X_CORRELATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 9:54 PM <br/>
 */
@ExtendWith(MockitoExtension.class)
class GwCacheLoaderServiceImplTest {

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private AuthorizedUserService mockAuthorizedUserService;

    private GwCacheLoaderService gwCacheLoaderService;

    private final ArgumentCaptor<String> strArgumentCaptor = ArgumentCaptor.forClass(String.class);
    private final ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    private final ArgumentCaptor<HttpMethod> methodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
    private ArgumentCaptor<Class> clazzCaptor = ArgumentCaptor.forClass(Class.class);


    @BeforeEach
    void setUp() {
        GwCacheServiceProps gwCacheServiceProps = new GwCacheServiceProps();
        gwCacheServiceProps.setHost("http://localhost");
        gwCacheServiceProps.setPort("8080");
        gwCacheServiceProps.setLoadJourneyPath("api/protected/scheduled_journey");
        gwCacheServiceProps.setLoadJourneySeatsPath("api/protected/booked_seats/{journeyId}");
        gwCacheServiceProps.setDeleteJourneyPath("api/protected/scheduled_journey/{agencyId}/{branchId}/{journeyId}");
        gwCacheLoaderService = new GwCacheLoaderServiceImpl(mockRestTemplate, gwCacheServiceProps, mockAuthorizedUserService);
    }

    @Test
    void seatsChange_make_http_call() {

        when(mockAuthorizedUserService.getAccessToken())
                .thenReturn("access-token");
        when(mockAuthorizedUserService.getXCorrelationId())
                .thenReturn("correlation-id");

        gwCacheLoaderService.seatsChange(10L, Arrays.asList(12, 23, 18));

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080api/protected/booked_seats/10");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer access-token");
        assertThat(entityArgumentCaptor.getValue().getHeaders().get(X_CORRELATION_ID).get(0)).isEqualTo("correlation-id");
        assertThat(clazzCaptor.getValue()).isEqualTo(Void.class);
    }

    @Test
    void addUpdateJourney_make_http_call() {
        when(mockAuthorizedUserService.getAccessToken())
                .thenReturn("access-token");
        when(mockAuthorizedUserService.getXCorrelationId())
                .thenReturn("correlation-id");

        gwCacheLoaderService.addUpdateJourney(new JourneyResponseDTO());

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080api/protected/scheduled_journey");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer access-token");
        assertThat(entityArgumentCaptor.getValue().getHeaders().get(X_CORRELATION_ID).get(0)).isEqualTo("correlation-id");
        assertThat(clazzCaptor.getValue()).isEqualTo(Void.class);
    }

    @Test
    void deleteJourneyJourney() {
        when(mockAuthorizedUserService.getAccessToken())
                .thenReturn("access-token");
        when(mockAuthorizedUserService.getXCorrelationId())
                .thenReturn("correlation-id");

        gwCacheLoaderService.deleteJourneyJourney(10L, 11L, 12L);

        verify(mockRestTemplate).exchange(strArgumentCaptor.capture(), methodArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());

        assertThat(strArgumentCaptor.getValue()).isEqualTo("http://localhost:8080api/protected/scheduled_journey/10/11/12");
        assertThat(methodArgumentCaptor.getValue()).isEqualTo(HttpMethod.DELETE);
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer access-token");
        assertThat(entityArgumentCaptor.getValue().getHeaders().get(X_CORRELATION_ID).get(0)).isEqualTo("correlation-id");
        assertThat(clazzCaptor.getValue()).isEqualTo(Void.class);
    }
}
