package net.gowaka.gowaka.network.api.apisecurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:37 PM <br/>
 */
@ConfigurationProperties("apisecurity")
@Component
@Data
public class ApiSecurityConfig {
    private String host;
    private String port;
    private String clientAuthorizationPath;
    private String userAuthorizationPath;
    private String registerUserPath;
    private String changeUserPasswordPath;
    private String forgotPasswordPath;
}
