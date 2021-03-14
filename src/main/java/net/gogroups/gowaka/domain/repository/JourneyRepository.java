package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.Journey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long> {

    List<Journey> findAllByOrderByCreatedAtDescArrivalIndicatorAsc();

    List<Journey> findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc();

    List<Journey> findByArrivalIndicatorTrue();

    Page<Journey> findByCar_IdIsInOrderByCreatedAtDescArrivalIndicatorAsc(List<Long> carId, Pageable pageable);

    Page<Journey> findByAgencyBranch_IdOrderByCreatedAtDescArrivalIndicatorAsc(Long branchId, Pageable pageable);

}
