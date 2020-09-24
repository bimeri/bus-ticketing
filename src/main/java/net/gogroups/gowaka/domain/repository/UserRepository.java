package net.gogroups.gowaka.domain.repository;

import net.gogroups.gowaka.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:11 PM <br/>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
