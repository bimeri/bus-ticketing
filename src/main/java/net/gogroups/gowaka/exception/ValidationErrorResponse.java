package net.gogroups.gowaka.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 30 Sep 2019
 */
@Data
public class ValidationErrorResponse extends ErrorResponse {

    private List<ErrorItem> errors = new ArrayList<>();

    void addError(ErrorItem errorItem){
        this.errors.add(errorItem);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorItem {
        private String field;
        private String message;
    }
}
