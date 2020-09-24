package net.gogroups.gowaka.network.api.cbs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: Edward Tanko <br/>
 * Date: 6/21/20 4:52 PM <br/>
 */
@Component
@ConfigurationProperties("cbs")
@Data
public class CBSProps {
    private String host;
    private String port;
    private String email;
    private String password;
    private String loginPath;
    private String availableBenefitsPath;
    private String userBenefitsPath;
    private String userRewardPointsPath;


}
