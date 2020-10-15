package net.gogroups.gowaka.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 4:41 PM <br/>
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "checked_in_code")
    private String checkedInCode;

    @Column(name = "checked_in_ind")
    private Boolean passengerCheckedInIndicator;

    @ManyToOne
    private BookedJourney bookedJourney;

    public Passenger(String name, String idNumber, Integer seatNumber, String email, String phoneNumber, String checkedInCode, Boolean passengerCheckedInIndicator) {
        this.name = name;
        this.idNumber = idNumber;
        this.seatNumber = seatNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.checkedInCode = checkedInCode;
        this.passengerCheckedInIndicator = passengerCheckedInIndicator;
    }
}
