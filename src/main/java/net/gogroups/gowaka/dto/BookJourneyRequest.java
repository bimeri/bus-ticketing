package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:01 AM <br/>
 */
@Data
public class BookJourneyRequest {

    private boolean destinationIndicator;
    private Long transitAndStopId;
    private String directToAccount;
    private Boolean subscribeToSMSNotification = Boolean.FALSE;

    @Valid
    private List<Passenger> passengers = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Passenger {

        @NotBlank(message = "passenger name required.")
        @Size(max = 50, message = "passenger's name can not be more than 50 characters")
        private String passengerName;

//        @NotBlank(message = "passenger name required.")
        @Size(max = 50, message = "passenger's ID number can not be more than 50 characters")
        private String passengerIdNumber;
        @Max(value = 100, message = "seat number can not be more than 100")
        private Integer seatNumber;

        @NotBlank(message = "passenger phone number required.")
        @Size(max = 15, message = "passenger's phone number can not be more than 15 characters")
        private String phoneNumber;

//        @NotBlank(message = "passenger email required.")
        @Email(message = "valid email required.")
        @Size(max = 100, message = "passenger's email can not be more than 100 characters")
        private String email;

    }
}
