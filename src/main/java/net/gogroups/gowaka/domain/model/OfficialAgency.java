package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 7:10 AM <br/>
 */
@Data
@Entity
public class OfficialAgency extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String agencyName;
    private String agencyRegistrationNumber;
    private String agencyAuthorizationDocument;
    private String logo;
    private Boolean isDisabled;

    @OneToMany(mappedBy = "officialAgency")
    private List<Bus> buses;

    @OneToMany(mappedBy = "officialAgency")
    private List<User> users;

    public OfficialAgency() {
        this.buses = new ArrayList<>();
        this.users = new ArrayList<>();
    }
}
