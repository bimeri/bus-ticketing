package net.gowaka.gowaka.network.api.cbs;

import lombok.extern.slf4j.Slf4j;
import net.gowaka.gowaka.network.api.cbs.config.CBSProps;
import net.gowaka.gowaka.network.api.cbs.model.CBSAccessToken;
import net.gowaka.gowaka.network.api.cbs.model.CBSBenefitDTO;
import net.gowaka.gowaka.network.api.cbs.model.CBSEmailPassword;
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
    public CBSServiceImpl(@Qualifier("apiSecurityRestTemplate") RestTemplate restTemplate, CBSProps cbsProps) {
        this.cbsProps = cbsProps;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CBSBenefitDTO> getAllAvailableBenefit() {

        CBSAccessToken cbsAccessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cbsAccessToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return (List<CBSBenefitDTO>) restTemplate.postForEntity(
                getRequestUri(cbsProps.getAvailableBenefitsPath()),
                request,
                List.class).getBody();

    }

    @Override
    public List<CBSBenefitDTO> getAllUserAvailableBenefit(String userId) {
        CBSAccessToken cbsAccessToken = login();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cbsAccessToken.getAccessToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        return (List<CBSBenefitDTO>) restTemplate.postForEntity(
                String.format(getRequestUri(cbsProps.getUserBenefitsPath()), userId),
                request,
                List.class).getBody();
    }

    private CBSAccessToken login() {
        HttpEntity<CBSEmailPassword> request = new HttpEntity<>(new CBSEmailPassword(cbsProps.getEmail(), cbsProps.getPassword()));
        return restTemplate.exchange(getRequestUri(cbsProps.getLoginPath()), HttpMethod.POST, request, CBSAccessToken.class).getBody();
    }

    private String getRequestUri(String path) {
        return cbsProps.getHost() + ":" + cbsProps.getPort() + path;
    }

}
