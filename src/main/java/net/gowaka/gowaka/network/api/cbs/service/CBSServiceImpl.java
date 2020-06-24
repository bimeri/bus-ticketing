package net.gowaka.gowaka.network.api.cbs.service;

import lombok.extern.slf4j.Slf4j;
import net.gowaka.gowaka.network.api.cbs.config.CBSProps;
import net.gowaka.gowaka.network.api.cbs.model.CBSAccessToken;
import net.gowaka.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gowaka.gowaka.network.api.cbs.model.CBSEmailPassword;
import net.gowaka.gowaka.network.api.cbs.model.CBSRewardPointDTO;
import net.gowaka.gowaka.service.CBSService;
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
    public CBSServiceImpl(@Qualifier("cbsApiRestTemplate") RestTemplate restTemplate, CBSProps cbsProps) {
        this.cbsProps = cbsProps;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CBSBenefitDTO> getAllAvailableBenefit() {

        CBSAccessToken cbsAccessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cbsAccessToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return (List<CBSBenefitDTO>) restTemplate.exchange(
                getRequestUri(cbsProps.getAvailableBenefitsPath()),
                HttpMethod.GET,
                request,
                List.class).getBody();

    }

    @Override
    public List<CBSBenefitDTO> getAllUserAvailableBenefit(String userId) {
        CBSAccessToken cbsAccessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cbsAccessToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return (List<CBSBenefitDTO>) restTemplate.exchange(
                String.format(getRequestUri(cbsProps.getUserBenefitsPath()), userId),
                HttpMethod.GET,
                request,
                List.class).getBody();
    }

    @Override
    public CBSRewardPointDTO getUserRewardPoints(String userId) {
        CBSAccessToken cbsAccessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cbsAccessToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                String.format(getRequestUri(cbsProps.getUserRewardPointsPath()), userId),
                HttpMethod.GET,
                request,
                CBSRewardPointDTO.class).getBody();
    }

    private CBSAccessToken login() {
        HttpEntity<CBSEmailPassword> request = new HttpEntity<>(new CBSEmailPassword(cbsProps.getEmail(), cbsProps.getPassword()));
        return restTemplate.postForEntity(getRequestUri(cbsProps.getLoginPath()), request, CBSAccessToken.class).getBody();
    }

    private String getRequestUri(String path) {
        return cbsProps.getHost() + ":" + cbsProps.getPort() + path;
    }

}
