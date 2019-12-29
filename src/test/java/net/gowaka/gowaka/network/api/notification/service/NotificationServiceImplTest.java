package net.gowaka.gowaka.network.api.notification.service;

import net.gowaka.gowaka.dto.EmailDTO;
import net.gowaka.gowaka.network.api.notification.config.NotificationApiProps;
import net.gowaka.gowaka.network.api.notification.model.EmailAddress;
import net.gowaka.gowaka.network.api.notification.model.NotificationTokenResponse;
import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;
import net.gowaka.gowaka.service.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author nouks
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {
    @Mock
    private RestTemplate mockRestTemplate;
    private NotificationApiProps notificationApiProps;
    private NotificationService notificationService;

    @Before
    public void  setUp() {
        notificationApiProps = new NotificationApiProps();
        notificationApiProps.setHost("http://localhost");
        notificationApiProps.setPort("8080");
        notificationApiProps.setEmailFromAddress("test@go-groups.net");
        notificationApiProps.setEmail("gowaka@something.com");
        notificationApiProps.setPassword("good-pass");
        notificationApiProps.setLoginPath("/api/public/login");
        notificationApiProps.setSendEmailPath("/api/protected/sendEmail");
        notificationService = new NotificationServiceImpl(
                new NotificationRestClient(mockRestTemplate, notificationApiProps)
        );
    }

    @Test
    public void send_email_should_call_notification_rest_template_with_proper_header_and_body() throws Exception {
        ArgumentCaptor<String> loginStrArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<HttpEntity> mailEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<String> sendStrArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Class> clazzCaptor = ArgumentCaptor.forClass(Class.class);
        URI uri = new URI("http://localhost");
        NotificationTokenResponse tokenResponse = new NotificationTokenResponse();
        tokenResponse.setAccessToken("access-token");
        tokenResponse.setHeader("some-header");
        tokenResponse.setIssuer("some-issuer");
        tokenResponse.setType("some-type");

        when(mockRestTemplate.postForLocation(anyString(), any())).thenReturn(uri);
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(ResponseEntity.of(Optional.of(tokenResponse)));
        notificationService.sendEmail(new SendEmailDTO());
        verify(mockRestTemplate).postForLocation(sendStrArgumentCaptor.capture(), mailEntityArgumentCaptor.capture());
        verify(mockRestTemplate).postForEntity(loginStrArgumentCaptor.capture(),
                entityArgumentCaptor.capture(), clazzCaptor.capture());
        assertThat(sendStrArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/protected/sendEmail");
        assertThat(loginStrArgumentCaptor.getValue()).isEqualTo("http://localhost:8080/api/public/login");
        assertThat(mailEntityArgumentCaptor.getValue().getHeaders().get("Authorization").get(0))
                .isEqualTo("Bearer access-token");
        assertThat(((SendEmailDTO) mailEntityArgumentCaptor.getValue().getBody()).getFromAddress())
                .isEqualTo("test@go-groups.net");
    }
}
