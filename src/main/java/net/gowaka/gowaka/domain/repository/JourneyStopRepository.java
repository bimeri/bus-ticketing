package net.gowaka.gowaka.domain.repository;

import net.gowaka.gowaka.domain.model.JourneyStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JourneyStopRepository extends JpaRepository<JourneyStop, Long> {
    @Transactional
    void deleteAllByJourneyId(Long journeyId);
}
