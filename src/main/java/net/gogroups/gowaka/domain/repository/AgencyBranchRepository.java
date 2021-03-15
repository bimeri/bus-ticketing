package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.AgencyBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/13/21 5:20 PM <br/>
 */
@Repository
public interface AgencyBranchRepository extends JpaRepository<AgencyBranch, Long> {
    List<AgencyBranch> findByOfficialAgency_Id(Long officialAgencyId);

}
