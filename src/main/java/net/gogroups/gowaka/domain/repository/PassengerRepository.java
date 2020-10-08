package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/20 11:23 PM <br/>
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByBookedJourney_Journey_Id(Long journeyId);
}
