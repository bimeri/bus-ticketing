package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.AppAlertNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 1/24/21 5:51 AM <br/>
 */
@Repository
public interface AppAlertNoticeRepository extends JpaRepository<AppAlertNotice, Long> {

    List<AppAlertNotice> findByStatus(Boolean status);
}
