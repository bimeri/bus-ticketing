package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:58 AM <br/>
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Car extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "license_plate_number")
    private String licensePlateNumber;
    @Column(name = "is_official_agency_Ind")
    private Boolean isOfficialAgencyIndicator;

    @ElementCollection
    private List<String> images = new ArrayList<>();

    @Column(name = "is_car_approved")
    private Boolean isCarApproved;


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
