package net.gogroups.gowaka.dto;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:59 PM <br/>
 */
@Data
public class PaymentStatusResponseDTO {

    private String transactionStatus;
    private String processingNumber;
    private String appTransactionNumber;
    private String paymentChannelCode;
    private String paymentChannelTransactionNumber;
    private String clientPaymentHash;

}
