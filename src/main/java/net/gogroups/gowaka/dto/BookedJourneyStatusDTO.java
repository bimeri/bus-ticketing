package net.gogroups.gowaka.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 7:24 PM <br/>
 */
@Data
public class BookedJourneyStatusDTO {

    private Long id;
    private Long transactionId;
    private Long journeyId;

    private Double amount;
    private String currencyCode;
    private String paymentStatus;
    private String paymentReason;
    private String paymentChannel;
    private String paymentChannelTransactionNumber;
    private LocalDateTime paymentDate;

    private List<PassengerDTO> passengers = new ArrayList<>();

    private String carName;
    private String carLicenseNumber;
    private String carDriverName;
    private String agencyName;
    private String agencyLogo;

    private String departureLocation;
    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;
    private String destinationLocation;
    private Boolean departureIndicator;
    private Boolean arrivalIndicator;

    private Double agencyAmount;
    private Double serviceChargeAmount;

    private Boolean hasRefundRequest;
    private Long refundId;
    private RefundStatus refundStatus;
    private Double refundAmount;

    public enum RefundStatus {
        PENDING,
        APPROVED,
        DECLINED,
        REFUNDED
    }

}
