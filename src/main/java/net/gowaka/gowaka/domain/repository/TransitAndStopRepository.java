package net.gowaka.gowaka.domain.repository;

import net.gowaka.gowaka.domain.model.Location;
import net.gowaka.gowaka.domain.model.TransitAndStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface TransitAndStopRepository extends JpaRepository<TransitAndStop, Long> {
    Optional<TransitAndStop> findDistinctFirstByLocation(Location location);
    List<TransitAndStop> findByLocationCityIgnoreCase(String city);
}
