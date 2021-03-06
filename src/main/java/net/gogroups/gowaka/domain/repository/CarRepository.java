package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByLicensePlateNumberIgnoreCase(String licensePlateNumber);
    List<Car> findByIsCarApproved(Boolean isCarApproved);
}
