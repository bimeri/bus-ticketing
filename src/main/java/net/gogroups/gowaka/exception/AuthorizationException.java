package net.gogroups.gowaka.exception;

import lombok.Data;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 8:51 PM <br/>
 */
@Data
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}
