package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 7:11 AM <br/>
 */
@Entity
@Data
public class PersonalAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "personalAgency")
    private List<SharedRide> sharedRides;

    @OneToOne(mappedBy = "personalAgency")
    private User user;
}
