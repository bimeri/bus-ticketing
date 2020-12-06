package net.gogroups.gowaka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Author: Edward Tanko <br/>
 * Date: 8/11/19 7:43 PM <br/>
 */
@Configuration
public class AppConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}

