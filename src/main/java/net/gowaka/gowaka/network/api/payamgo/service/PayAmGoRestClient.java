package net.gowaka.gowaka.network.api.payamgo.service;

import net.gowaka.gowaka.network.api.payamgo.config.PayAmGoApiProps;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.utils.Hashes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @author nouks
 */
@Component
public class PayAmGoRestClient {
    private RestTemplate restTemplate;
    private PayAmGoApiProps payAmGoApiProps;
    Logger logger = LoggerFactory.getLogger(PayAmGoRestClient.class);

    @Autowired
    public PayAmGoRestClient(@Qualifier("globalApiRestTemplate") RestTemplate restTemplate, PayAmGoApiProps payAmGoApiProps) {
        this.restTemplate = restTemplate;
        this.payAmGoApiProps = payAmGoApiProps;
    }

    public PayAmGoRequestResponseDTO initiatePayment(PayAmGoRequestDTO requestDTO) {
        String paymentRequestUrl = payAmGoApiProps.getHost()+payAmGoApiProps.getPaymentRequest();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Client-Key", payAmGoApiProps.getClientKey());
        headers.set("Client-Hash", Hashes.getClientInitiationHash(
                requestDTO.getAmount(), requestDTO.getCurrencyCode(), requestDTO.getAppTransactionNumber(),
                requestDTO.getAppUserPhoneNumber(), requestDTO.getPaymentResponseUrl(), payAmGoApiProps.getClientSecret()
        ));

        HttpEntity<PayAmGoRequestDTO> request = new HttpEntity<>(requestDTO, headers);
        /*logger.info(Hashes.getClientInitiationHash(
                requestDTO.getAmount(), requestDTO.getCurrencyCode(), requestDTO.getAppTransactionNumber(),
                requestDTO.getAppUserPhoneNumber(), requestDTO.getPaymentResponseUrl(), payAmGoApiProps.getClientSecret()
        ));*/
        return restTemplate.exchange(paymentRequestUrl, HttpMethod.POST, request, PayAmGoRequestResponseDTO.class).getBody();

    }

    private String getBaseUrl() {
        return payAmGoApiProps.getHost() +  ":" + payAmGoApiProps.getPort();
    }

}
