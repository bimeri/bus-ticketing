package net.gogroups.gowaka.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nnouka Stephen
 * @date 30 Sep 2019
 */
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors = new HashMap<>();

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
