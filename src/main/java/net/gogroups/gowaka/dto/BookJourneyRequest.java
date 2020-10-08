package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:01 AM <br/>
 */
@Data
@Validated
public class BookJourneyRequest {

    private boolean destinationIndicator;
    private Long transitAndStopId;

    @Valid
    private List<Passenger> passengers = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Passenger {

        @NotBlank(message = "passenger name required.")
        private String passengerName;
        private String passengerIdNumber;
        private Integer seatNumber;

        @NotBlank(message = "passenger phone number required.")
        private String phoneNumber;

        @NotBlank(message = "passenger email required.")
        @Email(message = "valid email required.")
        private String email;

    }
}
