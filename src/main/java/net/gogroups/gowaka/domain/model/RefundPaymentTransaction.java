package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 5:40 AM <br/>
 */
@Data
@Entity
public class RefundPaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;

    @Lob
    private String refundRequestMessage;
    private LocalDateTime requestedDate;

    @Lob
    private String refundResponseMessage;
    private LocalDateTime respondedDate;

    private Boolean isRefundApproved = false;
    private Boolean isRefunded = false;

    private String approvalName;
    private String approvalEmail;

    private String refundName;
    private String refundEmail;
    private LocalDateTime refundedDate;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH} )
    @JoinColumn(name = "payment_transaction_id", referencedColumnName = "id")
    private PaymentTransaction paymentTransaction;

}
