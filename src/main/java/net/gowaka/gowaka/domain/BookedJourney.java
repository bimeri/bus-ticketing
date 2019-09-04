package net.gowaka.gowaka.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 7:21 AM <br/>
 */
@Data
@Entity
public class BookedJourney {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    @Embedded
    private Passenger passenger;
    @ManyToOne
    private Journey journey;
    @ManyToOne
    private TransitAndStop destination;

    private Double amount;
    private Boolean passengerCheckedInIndicator;

    private String checkedInCode;

}
