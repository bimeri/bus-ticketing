package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 4:41 PM <br/>
 */
@Data
@Embeddable
public class Passenger {

    private String passengerName;
    private String passengerIdNumber;
    private String seatNumber;

}
