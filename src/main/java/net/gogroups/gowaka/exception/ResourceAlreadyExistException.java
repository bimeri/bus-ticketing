package net.gogroups.gowaka.exception;

import lombok.Data;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:56 AM <br/>
 */
@Data
public class ResourceAlreadyExistException extends RuntimeException {
    public ResourceAlreadyExistException(String message) {
        super(message);
    }
}
