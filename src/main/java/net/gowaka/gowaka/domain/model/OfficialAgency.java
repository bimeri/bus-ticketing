package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 7:10 AM <br/>
 */
@Data
@Entity
public class OfficialAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String agencyName;
    private String agencyRegistrationNumber;
    private String agencyAuthorizationDocument;

    @OneToMany(mappedBy = "officialAgency")
    private List<Bus> buses;

    @OneToMany(mappedBy = "officialAgency")
    private List<User> users;

}
