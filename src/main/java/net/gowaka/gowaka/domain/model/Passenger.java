package net.gowaka.gowaka.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 4:41 PM <br/>
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

    private String passengerName;
    private String passengerIdNumber;
    private Integer seatNumber;
    private String passengerEmail;
    private String passengerPhoneNumber;

    public Passenger(String passengerName, String passengerIdNumber, Integer seatNumber) {
        this.passengerName = passengerName;
        this.passengerIdNumber = passengerIdNumber;
        this.seatNumber = seatNumber;
    }
}
