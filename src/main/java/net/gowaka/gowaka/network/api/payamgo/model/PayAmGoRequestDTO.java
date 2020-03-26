package net.gowaka.gowaka.network.api.payamgo.model;

import lombok.Data;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:29 PM <br/>
 */
@Data
public class PayAmGoRequestDTO {
    private String amount;
    private String appTransactionNumber;
    private String appUserEmail;
    private String appUserFirstName;
    private String appUserLastName;
    private String appUserPhoneNumber;
    private String currencyCode;
    private String language;
    private String paymentReason;
    private String cancelRedirectUrl;
    private String paymentResponseUrl;
    private String returnRedirectUrl;
}
