package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.BookedJourney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface BookedJourneyRepository extends JpaRepository<BookedJourney, Long> {

    List<BookedJourney> findAllByUserUserId(String userId);
    List<BookedJourney> findAllByJourneyId(Long journeyId);
    Optional<BookedJourney> findTopByUser_UserIdOrderByIdDesc(String userId);

}
