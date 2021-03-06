package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:59 AM <br/>
 */
@Data
@Entity
public class Bus extends Car{

    @Column(name = "num_of_seats")
    private Integer numberOfSeats;  // to reduce join queries

    @ManyToOne
    private OfficialAgency officialAgency;

    @OneToMany(mappedBy = "bus")
    private List<Seat> seats;

    @ManyToOne
    @JoinColumn(name = "seat_structure_id", referencedColumnName = "id")
    private SeatStructure seatStructure;

    public Bus() {
        super();
        this.seats = new ArrayList<>();
    }
}
