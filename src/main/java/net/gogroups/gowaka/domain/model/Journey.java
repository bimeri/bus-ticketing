package net.gogroups.gowaka.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode
public class Journey extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "destination_stop_id")
    private TransitAndStop destination;
    private double amount;

    @ManyToOne
    @JoinColumn(name = "departure_stop_id")
    private TransitAndStop departureLocation;

    @Embedded
    private Driver driver;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "departure_indicator")
    private Boolean departureIndicator;

    @Column(name = "arrival_indicator")
    private Boolean arrivalIndicator;

    @Column(name = "is_journey_finished")
    private Boolean isJourneyFinished;
    @Column(name = "arrival_time")
    private  LocalDateTime arrivalTime;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookedJourney> bookedJourneys;

    @OneToMany(mappedBy = "journey", cascade = {CascadeType.ALL})
    private List<JourneyStop> journeyStops;

    @ManyToOne
    private AgencyBranch agencyBranch;

    @ManyToOne
    private Car car;

    public Journey() {
        this.journeyStops = new ArrayList<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
