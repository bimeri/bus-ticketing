package net.gowaka.gowaka.network.api.payamgo.service;

import net.gowaka.gowaka.network.api.payamgo.config.PayAmGoApiProps;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenRequest;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoTokenResponse;
import net.gowaka.gowaka.service.PayAmGoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 11:01 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class PayAmGoServiceImplTest {

    @Mock
    private RestTemplate mockRestTemplate;
    private PayAmGoApiProps payAmGoApiProps;

    private PayAmGoService payAmGoService;

    @Before
    public void setUp() throws Exception {

        payAmGoApiProps = new PayAmGoApiProps();
        payAmGoApiProps.setHost("http://localhost");
        payAmGoApiProps.setPort("8080");
        payAmGoApiProps.setTokenRequest("/api/public/login");
        payAmGoApiProps.setPaymentRequest("/api/protected/users/init_payment");
        payAmGoApiProps.setUsername("gw-username");
        payAmGoApiProps.setPassword("gw-password");
        payAmGoService = new PayAmGoServiceImpl(payAmGoApiProps, mockRestTemplate);
    }

    @Test
    public void initiatePayment_calls_RestTemplate_with_proper_params() {

        PayAmGoRequestDTO payAmGoRequestDTO = new PayAmGoRequestDTO();
        payAmGoRequestDTO.setAmount(100.0);
        payAmGoRequestDTO.setAppTransactionNumber("app-txn");
        payAmGoRequestDTO.setAppUserEmail("app@example.com");
        payAmGoRequestDTO.setAppUserFirstName("John");
        payAmGoRequestDTO.setAppUserLastName("Doe");
        payAmGoRequestDTO.setAppUserPhoneNumber("676279260");
        payAmGoRequestDTO.setCurrencyCode("XAF");
        payAmGoRequestDTO.setLanguage("en");
        payAmGoRequestDTO.setPaymentReason("Bus ticket");
        payAmGoRequestDTO.setCancelRedirectUrl("http://localhost/cancel");
        payAmGoRequestDTO.setPaymentResponseUrl("http://localhost/response");
        payAmGoRequestDTO.setReturnRedirectUrl("http://localhost/return");

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Class> clazzCaptor = ArgumentCaptor.forClass(Class.class);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> payEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Class> payClazzCaptor = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<HttpMethod> payMethodCaptor = ArgumentCaptor.forClass(HttpMethod.class);


        PayAmGoTokenResponse tokenResponse = new PayAmGoTokenResponse();
        tokenResponse.setAccessToken("access-token");
        when(mockRestTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.of(Optional.of(tokenResponse)));

        PayAmGoRequestResponseDTO requestResponse = new PayAmGoRequestResponseDTO();
        requestResponse.setAppTransactionNumber("app-txn");
        requestResponse.setProcessingNumber("123456789");
        requestResponse.setPaymentUrl("http://payamgo.com/payment");
        when(mockRestTemplate.exchange(anyString(), any(), any(), any(Class.class)))
                .thenReturn(ResponseEntity.of(Optional.of(requestResponse)));

        PayAmGoRequestResponseDTO payAmGoRequestResponseDTO = payAmGoService.initiatePayment(payAmGoRequestDTO);

        verify(mockRestTemplate).postForEntity(stringArgumentCaptor.capture(), entityArgumentCaptor.capture(), clazzCaptor.capture());
        verify(mockRestTemplate).exchange(urlArgumentCaptor.capture(), payMethodCaptor.capture(),
                payEntityArgumentCaptor.capture(), payClazzCaptor.capture());

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/login");
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Content-Type").get(0)).isEqualTo("application/json;charset=UTF-8");
        assertThat(entityArgumentCaptor.getValue().getHeaders().get("Accept").get(0)).isEqualTo("application/json");
        PayAmGoTokenRequest tokenBody = (PayAmGoTokenRequest) entityArgumentCaptor.getValue().getBody();
        assertThat(tokenBody.getEmail()).isEqualTo("gw-username");
        assertThat(tokenBody.getPassword()).isEqualTo("gw-password");
        assertThat(clazzCaptor.getValue()).isEqualTo(PayAmGoTokenResponse.class);

        assertThat(urlArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/users/init_payment");
        assertThat(payMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(payEntityArgumentCaptor.getValue().getHeaders().get("Content-Type").get(0)).isEqualTo("application/json;charset=UTF-8");
        assertThat(payEntityArgumentCaptor.getValue().getHeaders().get("Accept").get(0)).isEqualTo("application/json");
        assertThat(payEntityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0)).isEqualTo("Bearer access-token");
        PayAmGoRequestDTO paymentRequestBody = (PayAmGoRequestDTO) payEntityArgumentCaptor.getValue().getBody();
        assertThat(paymentRequestBody.getAmount()).isEqualTo(100);
        assertThat(paymentRequestBody.getAppTransactionNumber()).isEqualTo("app-txn");
        assertThat(paymentRequestBody.getAppUserEmail()).isEqualTo("app@example.com");
        assertThat(paymentRequestBody.getAppUserFirstName()).isEqualTo("John");
        assertThat(paymentRequestBody.getAppUserLastName()).isEqualTo("Doe");
        assertThat(paymentRequestBody.getAppUserPhoneNumber()).isEqualTo("676279260");
        assertThat(paymentRequestBody.getCancelRedirectUrl()).isEqualTo("http://localhost/cancel");
        assertThat(paymentRequestBody.getCurrencyCode()).isEqualTo("XAF");
        assertThat(paymentRequestBody.getLanguage()).isEqualTo("en");
        assertThat(paymentRequestBody.getPaymentReason()).isEqualTo("Bus ticket");
        assertThat(paymentRequestBody.getPaymentResponseUrl()).isEqualTo("http://localhost/response");
        assertThat(paymentRequestBody.getReturnRedirectUrl()).isEqualTo("http://localhost/return");
        assertThat(payClazzCaptor.getValue()).isEqualTo(PayAmGoRequestResponseDTO.class);


        assertThat(payAmGoRequestResponseDTO.getAppTransactionNumber()).isEqualTo("app-txn");
        assertThat(payAmGoRequestResponseDTO.getProcessingNumber()).isEqualTo("123456789");
        assertThat(payAmGoRequestResponseDTO.getPaymentUrl()).isEqualTo("http://payamgo.com/payment");
    }

}