package net.gogroups.gowaka.network.api.cache.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 9:09 PM <br/>
 */
@Component
@ConfigurationProperties("gwcache")
@Data
public class GwCacheServiceProps {

    private String host;
    private String port;
    private String loadJourneyPath;
    private String loadJourneySeatsPath;
    private String deleteJourneyPath;

}
