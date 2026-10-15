/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 *
 */
package com.petclinic.authservice.datalayer.roles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    // Lock in a consistent order; concurrent startup migrations must read current names.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r order by r.id")
    List<Role> findAllForMigration();

    Role findRoleByName(String name);
}
