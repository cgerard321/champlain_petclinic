package com.petclinic.auth.JWT;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.User.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by IntelliJ IDEA.
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JWTServiceTests {

    @Autowired
    private JWTService jwtService;

    private final Set<Role> ROLES = new HashSet<Role>(){{
        add(new Role(1, "TEST"));
    }};

    private final User USER = new User(
            1,
            "uname",
            "pwd",
            "a@b.c",
            false,
            ROLES);

    @Test
    void setup(){}

    @Test
    @DisplayName("Given user, get JWT")
    void get_jwt_from_user() {
        final String token = jwtService.encrypt(USER);
        assertNotNull(token);
    }

    @Test
    @DisplayName("Given token, get user")
    void get_user_from_jwt() {
        final String token = jwtService.encrypt(USER);
        final User decrypt = jwtService.decrypt(token);

        assertEquals(USER.getEmail(), decrypt.getEmail());

        final Set<String> userRolesNameOnly = USER.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        final Set<String> decryptRolesNameOnly = decrypt.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        assertEquals(userRolesNameOnly, decryptRolesNameOnly);
        assertEquals(USER.isVerified(), decrypt.isVerified());
    }

    @Test
    @DisplayName("Given token for user with no roles, decrypted user has empty roles Set")
    void get_user_with_no_roles_from_jwt() {
        final User build = USER.toBuilder().roles(Collections.emptySet()).build();
        final String token = jwtService.encrypt(build);
        final User decrypt = jwtService.decrypt(token);

        assertEquals(build.getEmail(), decrypt.getEmail());

        final Set<String> userRolesNameOnly = build.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        final Set<String> decryptRolesNameOnly = decrypt.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        assertEquals(userRolesNameOnly, decryptRolesNameOnly);
    }

    @Test
    @DisplayName("Given bad token, throw JwtException")
    void jwt_exception_flow() {
        final RuntimeException ex = assertThrows(RuntimeException.class, () -> jwtService.decrypt("this.is.bad"));
        assertEquals("Something wrong with the JWT boss", ex.getMessage());
    }
}
