package net.gowaka.gowaka.network.api.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author nouks
 * @date 29 Dec 2019
 */
@Component
@ConfigurationProperties("notification")
@Data
public class NotificationApiProps {
    private String host;
    private String port;
    private String email;
    private String password;
    private String loginPath;
    private String sendEmailPath;
    private String emailFromAddress;
}
