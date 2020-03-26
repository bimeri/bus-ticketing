package net.gowaka.gowaka.network.api.payamgo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 10:18 PM <br/>
 */
@Component
@ConfigurationProperties("payamgo")
@Data
public class PayAmGoApiProps {

    private String host;
    private String port;
    private String paymentRequest;
    private String clientKey;
    private String clientSecret;


}
