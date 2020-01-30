package net.gowaka.gowaka.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:01 AM <br/>
 */
@Data
@Entity
@EqualsAndHashCode
public class TransitAndStop {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Location location;

    @OneToMany(mappedBy = "transitAndStop", cascade = {CascadeType.ALL})
    private Set<JourneyStop> journeyStops;

    @OneToMany(mappedBy = "destination")
    private List<BookedJourney> bookedJourneys;

    public TransitAndStop() {
        this.journeyStops = new HashSet<>();
    }

    @Override
    public String toString() {
        return "TransitAndStop{" +
                "id=" + id +
                ", location=" + location +
                '}';
    }
}
