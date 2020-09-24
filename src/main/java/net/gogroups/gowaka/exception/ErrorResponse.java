package net.gogroups.gowaka.exception;

import lombok.*;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:58 AM <br/>
 */
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private String endpoint;
}
