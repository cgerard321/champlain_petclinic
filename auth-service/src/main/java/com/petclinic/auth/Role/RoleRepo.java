package com.petclinic.auth.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    Set<Role> getRolesByParent(Role parent);
}
