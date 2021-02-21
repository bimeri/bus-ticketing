package net.gogroups.gowaka.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:01 AM <br/>
 */
@Data
@Entity
@EqualsAndHashCode
public class TransitAndStop extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Location location;

    @OneToMany(mappedBy = "transitAndStop", cascade = {CascadeType.ALL})
    private List<JourneyStop> journeyStops;

    @OneToMany(mappedBy = "destination")
    private List<BookedJourney> bookedJourneys;

    public TransitAndStop() {
        this.journeyStops = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "TransitAndStop{" +
                "id=" + id +
                ", location=" + location +
                '}';
    }
}
