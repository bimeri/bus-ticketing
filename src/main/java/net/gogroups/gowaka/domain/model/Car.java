package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:58 AM <br/>
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Car {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String licensePlateNumber;
    private Boolean isOfficialAgencyIndicator;
    @ElementCollection
    private List<String> images = new ArrayList<>();
    private Boolean isCarApproved;
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "car")
    private List<Journey> journeys;

    public Car() {
        this.journeys = new ArrayList<>();
    }

    public void addJourney(Journey journey){
        this.journeys.add(journey);
        journey.setCar(this);
    }
}
