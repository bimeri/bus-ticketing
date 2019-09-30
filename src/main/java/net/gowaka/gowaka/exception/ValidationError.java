package net.gowaka.gowaka.exception;

import lombok.Data;
/**
 * @author Nnouka Stephen
 * @date 30 Sep 2019
 */
@Data
public class ValidationError {
    private String Code;
    private String message;
}
