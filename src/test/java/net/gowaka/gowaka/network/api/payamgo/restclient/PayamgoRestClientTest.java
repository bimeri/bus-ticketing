package net.gowaka.gowaka.network.api.payamgo.restclient;

import net.gowaka.gowaka.network.api.payamgo.config.PayAmGoApiProps;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestDTO;
import net.gowaka.gowaka.network.api.payamgo.model.PayAmGoRequestResponseDTO;
import net.gowaka.gowaka.network.api.payamgo.service.PayAmGoRestClient;
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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayamgoRestClientTest {

    @Mock
    private RestTemplate mockRestTemplate;
    private PayAmGoApiProps payAmGoApiProps;

    private PayAmGoRestClient payAmGoRestClient;

    @Before
    public void setUp() throws Exception {
        payAmGoApiProps = new PayAmGoApiProps();
        payAmGoApiProps.setPort("3333");
        payAmGoApiProps.setHost("https://localhost");
        payAmGoApiProps.setPaymentRequest("/initiate_payment");
        payAmGoApiProps.setClientKey("my-client-key");
        payAmGoApiProps.setClientSecret("my-client-secret");
        payAmGoRestClient = new PayAmGoRestClient(mockRestTemplate, payAmGoApiProps);
    }

    @Test
    public void whenInitiatePayment_thenReturn_paymentResponse() {
        PayAmGoRequestDTO payAmGoRequestDTO = new PayAmGoRequestDTO();
        payAmGoRequestDTO.setAmount("100.0");
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

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok(new PayAmGoRequestResponseDTO()));

        payAmGoRestClient.initiatePayment(payAmGoRequestDTO);

        verify(mockRestTemplate).exchange(urlCaptor.capture(), methodCaptor.capture(),
                httpEntityCaptor.capture(), classCaptor.capture());
        assertThat(urlCaptor.getValue()).isEqualTo("https://localhost/initiate_payment");
        assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(classCaptor.getValue()).isEqualTo(PayAmGoRequestResponseDTO.class);

        HttpEntity request = httpEntityCaptor.getValue();
        assertThat(request.getHeaders().toString()).isEqualTo("[Accept:\"application/json\", Content-Type:\"application/json;charset=UTF-8\", Client-Key:\"my-client-key\", Client-Hash:\"20d98664cf82d765b6df3f647d4d39c5\"]");
        assertThat(request.getBody().toString()).isEqualTo("PayAmGoRequestDTO(amount=100.0, appTransactionNumber=0000000014, appUserEmail=eddytnk@gmail.com, appUserFirstName=Obiasong, appUserLastName=Tanko, appUserPhoneNumber=678462355, currencyCode=XAF, language=en, paymentReason=Bus ticket, cancelRedirectUrl=http://localhost/cancel, paymentResponseUrl=http://localhost/response, returnRedirectUrl=http://localhost/return)");
    }
}
