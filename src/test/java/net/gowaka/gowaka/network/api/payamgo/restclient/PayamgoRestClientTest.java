package net.gowaka.gowaka.network.api.payamgo.restclient;

import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.service.PayAmGoRestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestClientResponseException;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class PayamgoRestClientTest {
    @Autowired
    PayAmGoRestClient payAmGoRestClient;

    Logger logger = LoggerFactory.getLogger(PayamgoRestClientTest.class);
    @Test
    public void whenInitiatePayment_thenReturn_paymentResponse() {
        PayAmGoRequestDTO payAmGoRequestDTO = new PayAmGoRequestDTO();
        payAmGoRequestDTO.setAmount(100.0);
        payAmGoRequestDTO.setAppTransactionNumber("0000000014");
        payAmGoRequestDTO.setAppUserEmail("eddytnk@gmail.com");
        payAmGoRequestDTO.setAppUserFirstName("Obiasong");
        payAmGoRequestDTO.setAppUserLastName("Tanko");
        payAmGoRequestDTO.setAppUserPhoneNumber("678462355");
        payAmGoRequestDTO.setCurrencyCode("XAF");
        payAmGoRequestDTO.setLanguage("en");
        payAmGoRequestDTO.setPaymentReason("Bus ticket");
        payAmGoRequestDTO.setCancelRedirectUrl("http://localhost/cancel");
        payAmGoRequestDTO.setPaymentResponseUrl("http://localhost/response");
        payAmGoRequestDTO.setReturnRedirectUrl("http://localhost/return");
        try{
            PayAmGoRequestResponseDTO payAmGoRequestResponseDTO = payAmGoRestClient.initiatePayment(payAmGoRequestDTO);
            logger.info(payAmGoRequestResponseDTO.toString());
        } catch (ApiException ex) {
            logger.info(ex.toString());
            ex.printStackTrace();
        }
    }
}
