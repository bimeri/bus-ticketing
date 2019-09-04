package net.gowaka.gowaka.exception;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:56 AM <br/>
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
