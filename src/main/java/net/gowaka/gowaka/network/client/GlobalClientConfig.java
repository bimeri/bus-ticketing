package net.gowaka.gowaka.network.client;

import net.gowaka.gowaka.network.RequestResponseLoggingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 5:47 PM <br/>
 */
@Configuration
public class GlobalClientConfig {

    @Bean(name = "globalApiRestTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder()
                .errorHandler(new GlobalRestTemplateResponseErrorHandler())
                .additionalInterceptors(new RequestResponseLoggingInterceptor())
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }

}
