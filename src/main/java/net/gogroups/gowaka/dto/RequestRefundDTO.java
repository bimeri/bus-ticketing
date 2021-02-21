package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:19 AM <br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestRefundDTO {

    @NotNull(message = "bookedJourneyId required")
    private Long bookedJourneyId;

    @NotNull(message = "transactionId required")
    private Long transactionId;

    @NotBlank(message = "message required")
    @Size(max = 300, message = "message must be at least 300 characters")
    private String message;

}
