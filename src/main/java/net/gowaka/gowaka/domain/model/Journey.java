package net.gowaka.gowaka.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:00 AM <br/>
 */
@Data
@Entity
@EqualsAndHashCode
public class Journey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*@Embedded
    private Location destination;*/
    @ManyToOne
    @JoinColumn(name = "destination_stop_id")
    private TransitAndStop destination;
    private double amount;
    /*@Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "country", column = @Column(name = "departure_country")),
            @AttributeOverride(name = "state", column = @Column(name = "departure_state")),
            @AttributeOverride(name = "city", column = @Column(name = "departure_city")),
            @AttributeOverride(name = "address", column = @Column(name = "departure_address")),
    })
    private Location departureLocation;*/
    @ManyToOne
    @JoinColumn(name = "departure_stop_id")
    private TransitAndStop departureLocation;
    @Embedded
    private Driver driver;

    private LocalDateTime departureTime;
    private LocalDateTime estimatedArrivalTime;

    private Boolean departureIndicator;
    private Boolean arrivalIndicator;
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "journey")
    private List<BookedJourney> bookedJourneys;

    @OneToMany(mappedBy = "journey", cascade = {CascadeType.ALL})
    private Set<JourneyStop> journeyStops;
    @ManyToOne
    private Car car;

    public Journey() {
        this.journeyStops = new HashSet<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
