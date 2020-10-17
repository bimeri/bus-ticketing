package net.gogroups.gowaka.network.api.cbs.service;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.network.api.cbs.config.CBSProps;
import net.gogroups.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gogroups.gowaka.network.api.cbs.model.CBSRewardPointDTO;
import net.gogroups.gowaka.service.CBSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 5:00 PM <br/>
 */
@Service
@Slf4j
public class CBSServiceImpl implements CBSService {

    private RestTemplate restTemplate;
    private CBSProps cbsProps;

    @Autowired
    public CBSServiceImpl(@Qualifier("ggClientRestTemplate") RestTemplate restTemplate, CBSProps cbsProps) {
        this.cbsProps = cbsProps;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CBSBenefitDTO> getAllUserAvailableBenefit(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return (List<CBSBenefitDTO>) restTemplate.exchange(getRequestUri(cbsProps.getUserBenefitsPath()),
                HttpMethod.GET,
                request,
                List.class).getBody();
    }

    @Override
    public CBSRewardPointDTO getUserRewardPoints(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(getRequestUri(cbsProps.getUserRewardPointsPath()),
                HttpMethod.GET,
                request,
                CBSRewardPointDTO.class).getBody();
    }

    private String getRequestUri(String path) {
        return cbsProps.getHost() + ":" + cbsProps.getPort() + path;
    }

}
