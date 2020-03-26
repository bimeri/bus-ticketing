package net.gowaka.gowaka.network.api.payamgo.service;

import net.gowaka.gowaka.network.api.payamgo.config.PayAmGoApiProps;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenRequest;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenResponse;
import net.gowaka.gowaka.service.PayAmGoService;
import org.springframework.beans.factory.annotation.Autowired;
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

    private PayAmGoRestClient restClient;

    @Autowired
    public PayAmGoServiceImpl(PayAmGoRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PayAmGoRequestResponseDTO initiatePayment(PayAmGoRequestDTO payAmGoRequestDTO) {
        return restClient.initiatePayment(payAmGoRequestDTO);
    }

}
