package net.gowaka.gowaka.network.api.apisecurity.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.AuthorizationException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 6:24 PM <br/>
 */
@Component
public class ApiSecurityRestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return (clientHttpResponse.getStatusCode().series() == CLIENT_ERROR
                || clientHttpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

        ApiSecurityErrorResponse errorResponse = null;
        String content = null;

        switch (clientHttpResponse.getStatusCode()) {

            case FORBIDDEN:
                throw new AuthorizationException("Access Denied.");
            case GATEWAY_TIMEOUT:
                throw new ApiException("External service down.", ErrorCodes.EXT_SERVICE_UNAVAILABLE.toString(), HttpStatus.SERVICE_UNAVAILABLE);
            case BAD_GATEWAY:
                throw new ApiException("External service error.", ErrorCodes.EXT_SERVER_ERROR.toString(), HttpStatus.BAD_GATEWAY);
            case UNPROCESSABLE_ENTITY:
                content = StreamUtils.copyToString(clientHttpResponse.getBody(), Charset.defaultCharset());
                errorResponse = new ObjectMapper().readValue(content, ApiSecurityErrorResponse.class);
                throw new ApiException(errorResponse.getMessage(), errorResponse.getCode(), HttpStatus.UNPROCESSABLE_ENTITY);
            case NOT_FOUND:
                content = StreamUtils.copyToString(clientHttpResponse.getBody(), Charset.defaultCharset());
                errorResponse = new ObjectMapper().readValue(content, ApiSecurityErrorResponse.class);
                throw new ApiException(errorResponse.getMessage(), ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
            case UNAUTHORIZED:
                throw new ApiException("Wrong credentials.", ErrorCodes.BAD_CREDENTIALS.toString(), HttpStatus.UNAUTHORIZED);
            default:
                throw new ApiException("An unexpected error occurred.", ErrorCodes.INT_SERVER_ERROR.toString(), clientHttpResponse.getStatusCode());

        }

    }
}
