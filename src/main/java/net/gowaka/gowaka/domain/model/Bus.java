package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:59 AM <br/>
 */
@Data
@Entity
public class Bus extends Car{

    private Integer numberOfSeats;

    @ManyToOne
    private OfficialAgency officialAgency;

    @OneToMany(mappedBy = "bus")
    private List<Seat> seats;

    public Bus() {
        super();
        this.seats = new ArrayList<>();
    }
}
