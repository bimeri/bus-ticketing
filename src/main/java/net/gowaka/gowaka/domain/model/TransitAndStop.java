package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:01 AM <br/>
 */
@Data
@Entity
public class TransitAndStop {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Location location;

    @ManyToMany(mappedBy = "transitAndStops")
    private List<Journey> journeys;

    @OneToMany(mappedBy = "destination")
    private List<BookedJourney> bookedJourneys;
    private double amount;

    public TransitAndStop() {
        this.journeys = new ArrayList<>();
    }
}
