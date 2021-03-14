package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/13/21 3:59 PM <br/>
 */
@Entity
@Data
public class AgencyBranch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    @OneToMany(mappedBy = "agencyBranch", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    @ManyToOne
    private OfficialAgency officialAgency;

}
