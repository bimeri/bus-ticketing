package net.gowaka.gowaka.domain.repository;

import net.gowaka.gowaka.domain.model.JourneyStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyStopRepository extends JpaRepository<JourneyStop, Long> {
}
