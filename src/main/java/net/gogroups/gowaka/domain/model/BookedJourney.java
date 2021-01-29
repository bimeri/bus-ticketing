package net.gogroups.gowaka.domain.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 7:21 AM <br/>
 */
@Data
@Entity
public class BookedJourney extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "bookedJourney", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Passenger> passengers = new ArrayList<>();

    @ManyToOne
    private Journey journey;

    @ManyToOne
    private TransitAndStop destination;

    @Column(name = "amount")
    private Double amount;
    private Boolean isAgencyBooking;

    @OneToOne(mappedBy = "bookedJourney")
    private PaymentTransaction paymentTransaction;

    private Boolean smsNotification = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "agency_user_id")
    private User agencyUser;

    public void addPassenger(Passenger passenger){
        this.passengers.add(passenger);
        passenger.setBookedJourney(this);
    }

}
