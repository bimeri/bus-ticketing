package net.gowaka.gowaka.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
    private String checkedInCode;
    private String qRCheckedInImage;
    private String paymentReason;
    private String paymentChannel;
    private String paymentChannelTransactionNumber;
    private LocalDateTime paymentDate;
    private boolean checkedIn;

    private String passengerName;
    private String passengerIdNumber;
    private Integer passengerSeatNumber;
    private String passengerEmail;
    private String passengerPhoneNumber;

    private String carName;
    private String carLicenseNumber;
    private String carDriverName;

    private String departureLocation;
    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;
    private String destinationLocation;

}
