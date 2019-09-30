package net.gowaka.gowaka.exception;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nnouka Stephen
 * @date 30 Sep 2019
 */
public class ValidationErrorResponse extends ErrorResponse{
    private Map<String, List<ValidationError>> errors = new HashMap<>();

    public Map<String, List<ValidationError>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<ValidationError>> errors) {
        this.errors = errors;
    }
}
