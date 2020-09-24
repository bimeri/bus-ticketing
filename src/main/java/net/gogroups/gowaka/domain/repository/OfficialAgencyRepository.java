package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.OfficialAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:24 PM <br/>
 */
@Repository
public interface OfficialAgencyRepository extends JpaRepository<OfficialAgency, Long> {
}
