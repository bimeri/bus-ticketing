package net.gowaka.gowaka.domain.repository;

import net.gowaka.gowaka.domain.model.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long> {
    List<Journey> findAllByOrderByTimestampDescArrivalIndicatorAsc();
}
