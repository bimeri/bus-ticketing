package net.gogroups.gowaka.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 1:13 PM <br/>
 */
@ConfigurationProperties("payment")
@Configuration
@Data
public class PaymentUrlResponseProps {

    private String payAmGoPaymentCancelUrl;
    private String payAmGoPaymentRedirectUrl;
    private String payAmGoPaymentResponseUrl;

}
