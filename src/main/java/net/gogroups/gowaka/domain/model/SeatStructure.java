package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class SeatStructure extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "number_of_seats")
    private int numberOfSeats;
    private String image;
    @Column(name = "seat_structure_code")
    private String seatStructureCode;
}
