package com.petclinic.auth;

import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@Transactional(propagation  = Propagation.NOT_SUPPORTED)
@ActiveProfiles("test")
public class AuthServicePersistenceTests {

    @Autowired
    private UserRepo userRepo;

    private final User DEFAULT_USER =
            new User(1, "username-1", "password-1", "email-1");

    @BeforeEach
    void cleanUp() {
        userRepo.deleteAllInBatch();
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

    private User addDefaultUser() {
        return userRepo.save(DEFAULT_USER);
    }
}
