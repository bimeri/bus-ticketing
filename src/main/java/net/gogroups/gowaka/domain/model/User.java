package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:09 AM <br/>
 */
@Data
@Entity
public class User extends BaseEntity{

    @Id
    private String userId;
    private String code;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String idCardNumber;

    @OneToMany(mappedBy = "user")
    List<BookedJourney> bookedJourneys;

    @ManyToOne
    private OfficialAgency officialAgency;

    private Boolean  isAgencyAdminIndicator = false;

    @ManyToOne
    private AgencyBranch agencyBranch;

    @OneToOne
    private PersonalAgency personalAgency;

    @Transient
    List<String> roles;

    public User() {
        this.roles = new ArrayList<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
