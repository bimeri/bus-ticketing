package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:00 AM <br/>
 */
@Data
@Entity
public class Journey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Location destination;
    private double amount;
    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "country", column = @Column(name = "departure_country")),
            @AttributeOverride(name = "state", column = @Column(name = "departure_state")),
            @AttributeOverride(name = "city", column = @Column(name = "departure_city")),
            @AttributeOverride(name = "address", column = @Column(name = "departure_address")),
    })
    private Location departureLocation;
    @Embedded
    private Driver driver;

    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;

    private Boolean departureIndicator;
    private Boolean arrivalIndicator;
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "journey")
    private List<BookedJourney> bookedJourneys;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "journey_id"),
            inverseJoinColumns = @JoinColumn(name = "transit_stop_id"))
    private List<TransitAndStop> transitAndStops;
    @ManyToOne
    private Car car;

    public Journey() {
        this.transitAndStops = new ArrayList<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
