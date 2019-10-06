package net.gowaka.gowaka.domain.repository;

import net.gowaka.gowaka.domain.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/5/19 9:19 PM <br/>
 */
@Repository
public interface PayAmGoTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

}
