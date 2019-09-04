package net.gowaka.gowaka.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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

    @Transient
    List<String> roles;

    public User() {
        this.roles = new ArrayList<>();
        this.bookedJourneys = new ArrayList<>();
    }
}
