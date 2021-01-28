package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gogroups.gowaka.constant.RefundStatus;

import java.time.LocalDateTime;

import static net.gogroups.gowaka.constant.RefundStatus.PENDING;

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

    private String approvalName;
    private String approvalEmail;

    private String refunderName;
    private String refunderEmail;
    private LocalDateTime refundedDate;

    private User user;
    private RefundStatus status = PENDING;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User{

        private String fullName;
        private String email;
        private String phoneNumber;
    }

    public static class bookedJourney{
        private int id;
        private String departure;
        private String destination;
        private int agencyCharge;
        private String departureTime;
    }
}
