package net.gogroups.gowaka.network.api.cache.service;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.dto.JourneyResponseDTO;
import net.gogroups.gowaka.network.api.cache.service.config.GwCacheServiceProps;
import net.gogroups.gowaka.service.GwCacheLoaderService;
import net.gogroups.security.service.AuthorizedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static net.gogroups.security.Constants.X_CORRELATION_ID;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 8:59 PM <br/>
 */
@Service
@Slf4j
public class GwCacheLoaderServiceImpl implements GwCacheLoaderService {

    private RestTemplate restTemplate;
    private GwCacheServiceProps gwCacheServiceProps;
    private AuthorizedUserService authorizedUserService;

    @Autowired
    public GwCacheLoaderServiceImpl(@Qualifier("ggClientRestTemplate") RestTemplate restTemplate, GwCacheServiceProps gwCacheServiceProps, AuthorizedUserService authorizedUserService) {
        this.restTemplate = restTemplate;
        this.gwCacheServiceProps = gwCacheServiceProps;
        this.authorizedUserService = authorizedUserService;
    }

    @Override
    public void seatsChange(Long journeyId, List<Integer> bookedSeats) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<List<Integer>> request = new HttpEntity<>(bookedSeats, headers);

        String uri = gwCacheServiceProps.getLoadJourneySeatsPath().replaceAll("\\{journeyId}", journeyId.toString());
        restTemplate.exchange(getRequestUri(uri),
                HttpMethod.POST, request, Void.class);
    }

    @Override
    public void addUpdateJourney(JourneyResponseDTO journeyResponseDTO) {

        HttpHeaders headers = getHttpHeaders();
        HttpEntity<JourneyResponseDTO> request = new HttpEntity<>(journeyResponseDTO, headers);

        restTemplate.exchange(getRequestUri(gwCacheServiceProps.getLoadJourneyPath()),
                HttpMethod.POST, request, Void.class);
    }

    @Override
    public void deleteJourneyJourney(Long agencyId, Long branchId, Long journeyId) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String uri = gwCacheServiceProps.getDeleteJourneyPath()
                .replaceAll("\\{journeyId}", journeyId.toString())
                .replaceAll("\\{branchId}", branchId.toString())
                .replaceAll("\\{agencyId}", agencyId.toString());

        restTemplate.exchange(getRequestUri(uri),
                HttpMethod.DELETE, request, Void.class);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authorizedUserService.getAccessToken());
        headers.set(X_CORRELATION_ID, authorizedUserService.getXCorrelationId());
        return headers;
    }

    private String getRequestUri(String path) {
        String url = gwCacheServiceProps.getHost() + ":" + gwCacheServiceProps.getPort() + path;
        log.info("Connecting to url: {}", url);
        return url;
    }

}
