package net.gowaka.gowaka.network.api.cbs.client;

import net.gowaka.gowaka.network.RequestResponseLoggingInterceptor;
import net.gowaka.gowaka.network.client.GlobalRestTemplateResponseErrorHandler;
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
public class CBSClientConfig {

    @Bean(name = "cbsApiRestTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder()
                .errorHandler(new CBSRestTemplateResponseErrorHandler())
                .additionalInterceptors(new RequestResponseLoggingInterceptor())
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }

}
