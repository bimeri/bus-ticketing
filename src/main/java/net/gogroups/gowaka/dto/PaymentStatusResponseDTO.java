package net.gogroups.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:59 PM <br/>
 */
@Data
public class PaymentStatusResponseDTO {

    @NotBlank(message = "transaction status is required.")
    private String transactionStatus;
    @NotBlank(message = "processing number is required.")
    private String processingNumber;
    @NotBlank(message = "application transaction number is required.")
    private String appTransactionNumber;
    @NotBlank(message = "payment channel code is required.")
    private String paymentChannelCode;
    @NotBlank(message = "payment channel transaction number is required.")
    private String paymentChannelTransactionNumber;
    @NotBlank(message = "client hash is required.")
    private String clientPaymentHash;

}
