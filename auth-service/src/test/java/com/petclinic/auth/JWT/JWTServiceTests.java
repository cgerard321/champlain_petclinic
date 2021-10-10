package com.petclinic.auth.JWT;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.User.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */

@TestComponent
public class JWTServiceTests {

    @Autowired
    private JWTService jwtService;

    private final User USER = new User(
            1,
            "uname",
            "pwd",
            "a@b.c",
            false,
            null);

    private final Set<Role> ROLES = new HashSet<Role>(){{
        add(new Role(1, "TEST"));
    }};

    @Test
    void setup(){}

    @Test
    @DisplayName("Given user, get JWT")
    void get_jwt_from_user() {

        assertNotNull(jwtService.encrypt(USER));
    }
}
