package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.RefundPaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/20/20 4:12 AM <br/>
 */
@Repository
public interface RefundPaymentTransactionRepository extends JpaRepository<RefundPaymentTransaction, Long> {

    Optional<RefundPaymentTransaction> findByIdAndPaymentTransaction_BookedJourney_User_UserId(Long id, String userId);List<RefundPaymentTransaction> findByPaymentTransaction_BookedJourney_Journey_IdAndPaymentTransaction_BookedJourney_User_UserId(Long journeyId, String userId);
    List<RefundPaymentTransaction> findByPaymentTransaction_BookedJourney_Journey_Id(Long journeyId);
}
