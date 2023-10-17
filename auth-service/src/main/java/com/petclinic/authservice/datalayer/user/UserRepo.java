/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 * <p>
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.authservice.datalayer.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    User findUserById(long id);

    Optional<User> findByUsername(String username);

    User findUserByUserIdentifier_UserId(String userId);

    //Optional<User> findByUserId(String userId);

}

