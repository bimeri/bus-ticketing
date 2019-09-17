package net.gowaka.gowaka.domain.model;

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
public class User {
    @Id
    private String userId;
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "user")
    List<BookedJourney> bookedJourneys;

    @ManyToOne
    private OfficialAgency officialAgency;
    @OneToOne
    private PersonalAgency personalAgency;

    @Transient
    List<String> roles;

    public User() {
        this.roles = new ArrayList<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
