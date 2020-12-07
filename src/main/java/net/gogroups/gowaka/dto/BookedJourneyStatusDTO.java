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

    private Double agencyAmount;
    private Double serviceChargeAmount;


}
