package net.gowaka.gowaka.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:01 AM <br/>
 */
@Data
public class BookJourneyRequest {

    @NotBlank(message = "passenger name required.")
    private String passengerName;
    private String passengerIdNumber;
    private Integer seatNumber;
    @NotBlank(message = "passenger phone number required.")
    private String phoneNumber;
    @NotBlank(message = "passenger email required.")
    @Email(message = "valid email required.")
    private String email;
    private boolean destinationIndicator;
    private Long transitAndStopId;

}
