package net.gogroups.gowaka.config;

import net.gogroups.notification.service.NotificationService;
import net.gogroups.notification.service.impl.NotificationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.time.Clock;
import java.util.Collections;

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

