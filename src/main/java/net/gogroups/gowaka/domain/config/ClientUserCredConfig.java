package net.gogroups.gowaka.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 9:19 PM <br/>
 */
@ConfigurationProperties("client")
@Component
@Data
public class ClientUserCredConfig {
    private String clientId;
    private String clientSecret;
    private String appName;
    private String url;
}
