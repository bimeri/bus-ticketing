package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:19 PM <br/>
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByIdAndBookedJourney_IdAndBookedJourney_User_UserId(Long transactionId, Long bookedJourneyId, String userId);
}
