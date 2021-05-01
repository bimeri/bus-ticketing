package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/20 11:23 PM <br/>
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByBookedJourney_Journey_Id(Long journeyId);

    Optional<Passenger> findByCheckedInCode(String code);

    @Query("SELECT p FROM Passenger p WHERE p.phoneNumber=:phoneNumber OR p.name LIKE %:name%")
    List<Passenger> findAllByPhoneNumberOrName(@Param("phoneNumber") String phoneNumber, @Param("name") String name);

}
