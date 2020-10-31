package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.ServiceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:08 AM <br/>
 */
@Repository
public interface ServiceChargeServiceRepository extends JpaRepository<ServiceCharge, String> {

}
