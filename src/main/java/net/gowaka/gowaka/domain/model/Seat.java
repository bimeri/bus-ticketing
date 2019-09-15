package net.gowaka.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 6:00 AM <br/>
 */
@Data
@Entity
public class Seat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bus bus;
}
