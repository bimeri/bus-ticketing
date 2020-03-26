package net.gowaka.gowaka.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:56 AM <br/>
 */
@Data
public class ApiException extends RuntimeException {
    private HttpStatus httpStatus;
    private String errorCode;
    public ApiException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "message=" + getMessage() +
                "httpStatus=" + httpStatus +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
