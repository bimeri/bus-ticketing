package net.gowaka.gowaka.repository;

import net.gowaka.gowaka.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
}
