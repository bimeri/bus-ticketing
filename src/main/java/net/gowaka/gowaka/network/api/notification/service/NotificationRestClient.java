package net.gowaka.gowaka.network.api.notification.service;

import net.gowaka.gowaka.network.api.notification.config.NotificationApiProps;
import net.gowaka.gowaka.network.api.notification.model.NotificationTokenRequest;
import net.gowaka.gowaka.network.api.notification.model.NotificationTokenResponse;
import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author nouks
 */
@Component
public class NotificationRestClient {
    private RestTemplate restTemplate;
    private NotificationApiProps notificationApiProps;
    Logger logger = LoggerFactory.getLogger(NotificationRestClient.class);

    @Autowired
    /*Decided to use apiSecurityRestTemplate instead of globalRestTemplate because of testing issues.
    * There is difficulty in creating a mock server that will test two different rest templates
    * UserService contacts both ApiSecurity and NotificationApi
    * */
    public NotificationRestClient(@Qualifier("apiSecurityRestTemplate") RestTemplate restTemplate, NotificationApiProps notificationApiProps) {
        this.restTemplate = restTemplate;
        this.notificationApiProps = notificationApiProps;
    }

    public void sendEmail(SendEmailDTO email) {
        HttpHeaders headers = new HttpHeaders();
        NotificationTokenResponse tokenResponse = getToken();
        headers.setBearerAuth(tokenResponse.getAccessToken());
        logger.info("jwt token: Header - {}, Issuer - {}", tokenResponse.getHeader(), tokenResponse.getIssuer());
        email.setFromAddress(notificationApiProps.getEmailFromAddress());
        HttpEntity<SendEmailDTO> request = new HttpEntity<>(email, headers);
        restTemplate.postForLocation(
                getRequestUri(notificationApiProps.getSendEmailPath()),
                request
        );
    }
    private NotificationTokenResponse getToken(){
        HttpEntity<NotificationTokenRequest> request = new HttpEntity<>(
                new NotificationTokenRequest(
                        notificationApiProps.getEmail(),
                        notificationApiProps.getPassword()
                ));
        return restTemplate.postForEntity(
                getRequestUri(notificationApiProps.getLoginPath()),
                request,
                NotificationTokenResponse.class
        ).getBody();
    }

    private String getRequestUri(String PATH){
        String REQUEST_URI;
        String PORT = notificationApiProps.getPort();
        String HOST = notificationApiProps.getHost();
        if (PORT.equals("")){
            REQUEST_URI = HOST + PATH;
        }else {
            REQUEST_URI = HOST + ":" + PORT + PATH;
        }
        logger.info("Contacting server: " + REQUEST_URI);
        return REQUEST_URI;
    }
}
