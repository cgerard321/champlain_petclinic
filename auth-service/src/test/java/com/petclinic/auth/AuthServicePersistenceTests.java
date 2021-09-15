package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional(propagation  = Propagation.NOT_SUPPORTED)
@ActiveProfiles("test")
public class AuthServicePersistenceTests {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    private final User DEFAULT_USER =
            new User(0, "username-1", "password-1", "email-1");

    private final Role ROLE_ADMIN = new Role(0, "ADMIN");

    private final static Random rng;

    static {
        rng = new Random();
    }

    @BeforeEach
    void cleanUp() {
        userRepo.deleteAllInBatch();
        roleRepo.deleteAllInBatch();
    }

    @Test
    @DisplayName("Add one User to database")
    void add_user_to_database() {

        User created = addDefaultUser();

        assertNotNull(created);

        User byId = userRepo.findById(created.getId()).get();

        assertEquals(byId.getUsername(), DEFAULT_USER.getUsername());
    }

    @Test
    @DisplayName("Add then remove User from database")
    void add_then_remove_user() {

        User created = addDefaultUser();

        assertNotNull(created);

        userRepo.delete(created);

        assertFalse(userRepo.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Add two users with the same email")
    void add_two_users_with_same_email() {

        addDefaultUser();

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class , this::addDefaultUser);
    }

    @Test
    @DisplayName("Create a role")
    void add_role() {
        roleRepo.save(ROLE_ADMIN);
    }

    @Test
    @DisplayName("Create then delete a role")
    void create_then_delete_role() {

        final Role created = roleRepo.save(ROLE_ADMIN);
        roleRepo.delete(created);
    }

    @Test
    @DisplayName("Add two roles with the same name")
    void add_two_roles_with_same_name() {

        addAdminRole();
        assertThrows(DataIntegrityViolationException.class, this::addAdminRole);
    }

    private Role addRoleAsClone(Role r) {
        return roleRepo.save(r.toBuilder().id(AuthServicePersistenceTests.rng.nextInt()).build());
    }

    private Role addAdminRole() {
        return addRoleAsClone(ROLE_ADMIN);
    }

    private User addDefaultUser() {
        User deepCopy = DEFAULT_USER.toBuilder()
                .id(AuthServicePersistenceTests.rng.nextInt())
                .build();
        return userRepo.save(deepCopy);
    }

}
