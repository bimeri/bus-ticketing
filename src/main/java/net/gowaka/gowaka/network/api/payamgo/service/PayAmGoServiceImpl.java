package net.gowaka.gowaka.network.api.payamgo.service;

import net.gowaka.gowaka.network.api.payamgo.config.PayAmGoApiProps;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenRequest;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenResponse;
import net.gowaka.gowaka.service.PayAmGoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:26 PM <br/>
 */
@Service
public class PayAmGoServiceImpl implements PayAmGoService {

    private RestTemplate restTemplate;
    private PayAmGoApiProps payAmGoApiProps;

    public PayAmGoServiceImpl(PayAmGoApiProps payAmGoApiProps, @Qualifier("globalApiRestTemplate") RestTemplate restTemplate) {
        this.payAmGoApiProps = payAmGoApiProps;
        this.restTemplate = restTemplate;
    }

    @Override
    public PayAmGoRequestResponseDTO initiatePayment(PayAmGoRequestDTO requestResponseDTO) {

        String tokenUrl = payAmGoApiProps.getHost()+":"+payAmGoApiProps.getPort()+payAmGoApiProps.getTokenRequest();
        String paymentRequestUrl = payAmGoApiProps.getHost()+":"+payAmGoApiProps.getPort()+payAmGoApiProps.getPaymentRequest();
        PayAmGoTokenResponse token = getToken(payAmGoApiProps.getUsername(), payAmGoApiProps.getPassword(), tokenUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setBearerAuth(token.getAccessToken());

        HttpEntity<PayAmGoRequestDTO> request = new HttpEntity<>(requestResponseDTO, headers);

        return restTemplate.exchange(paymentRequestUrl, HttpMethod.POST, request, PayAmGoRequestResponseDTO.class).getBody();

    }

    private PayAmGoTokenResponse getToken(String username, String password, String url) {

        PayAmGoTokenRequest payAmGoTokenRequest = new PayAmGoTokenRequest();
        payAmGoTokenRequest.setEmail(username);
        payAmGoTokenRequest.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HttpEntity<PayAmGoTokenRequest> request = new HttpEntity<>(payAmGoTokenRequest, headers);

        return restTemplate.postForEntity(url, request, PayAmGoTokenResponse.class).getBody();

    }

}
