package com.petclinic.auth.UserRole;


import com.petclinic.auth.UserRole.data.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepo extends JpaRepository<UserRole, Long> {
}
