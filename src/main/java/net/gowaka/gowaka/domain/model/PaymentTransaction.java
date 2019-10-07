package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:07 PM <br/>
 */
@Data
@Entity
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
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

    private String processingNumber;
    private String paymentChannel;
    private String paymentChannelTransactionNumber;
    private String transactionStatus;

}
