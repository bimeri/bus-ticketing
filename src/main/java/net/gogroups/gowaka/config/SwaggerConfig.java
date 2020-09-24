package net.gogroups.gowaka.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.HashSet;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 8:58 PM <br/>
 */

@Configuration
@EnableSwagger2
@Profile("!prod")
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    @Bean
    public Docket api() {
        HashSet<String> consumesAndProduces =
                new HashSet<>(Arrays.asList("application/json"));

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(metadata())
                .consumes(consumesAndProduces)
                .produces(consumesAndProduces)
                .pathMapping("/")
                .globalOperationParameters(
                        Arrays.asList(new ParameterBuilder()
                                .name("Authorization")
                                .defaultValue("Bearer ")
                                .description("Security token")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(true)
                                .build()))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .paths(Predicates.not(PathSelectors.regex("/actuator.*")))
                .build();
    }

    private ApiInfo metadata() {
        return new ApiInfoBuilder()
                .title(applicationName+" API")
                .description(applicationName+ ": Api Service")
                .version("1.0")
                .build();
    }
}
