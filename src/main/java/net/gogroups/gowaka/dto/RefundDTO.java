package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:25 PM <br/>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {

    private Long id;
    private Double amount;

    private String refundRequestMessage;
    private LocalDateTime requestedDate;

    private String refundResponseMessage;
    private LocalDateTime respondedDate;

    private Boolean isRefundApproved;
    private Boolean isRefunded;

    private String approvalName;
    private String approvalEmail;

    private String refunderName;
    private String refunderEmail;
    private LocalDateTime refundedDate;

    private User user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User{

        private String fullName;
        private String email;
        private String phoneNumber;
    }
}
