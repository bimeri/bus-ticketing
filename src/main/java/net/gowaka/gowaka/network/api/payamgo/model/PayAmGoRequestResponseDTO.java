package net.gowaka.gowaka.network.api.payamgo.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:29 PM <br/>
 */
@Data
public class PayAmGoRequestResponseDTO {

    private String appTransactionNumber;
    private String processingNumber;
    private String paymentUrl;

}
