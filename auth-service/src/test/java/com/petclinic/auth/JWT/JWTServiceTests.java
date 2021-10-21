/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.JWT;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.User.data.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Given token for unverified, get user")
    void get_unverified_user_from_jwt() {
        final String token = jwtService.encrypt(USER);
        final User decrypt = jwtService.decrypt(token);

        assertEquals(USER.getEmail(), decrypt.getEmail());

        final Set<String> userRolesNameOnly = USER.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        final Set<String> decryptRolesNameOnly = decrypt.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        assertEquals(userRolesNameOnly, decryptRolesNameOnly);
        assertEquals(USER.isVerified(), decrypt.isVerified());
    }

    @Test
    @DisplayName("Given token for verified, get user")
    void get_verified_user_from_jwt() {
        final User verifiedUser = USER.toBuilder().verified(true).build();
        final String token = jwtService.encrypt(verifiedUser);
        final User decrypt = jwtService.decrypt(token);

        assertEquals(verifiedUser.getEmail(), decrypt.getEmail());

        final Set<String> userRolesNameOnly = verifiedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        final Set<String> decryptRolesNameOnly = decrypt.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        assertEquals(userRolesNameOnly, decryptRolesNameOnly);
        assertTrue(decrypt.isVerified());
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
        assertThrows(JwtException.class, () -> jwtService.decrypt("this.is.bad"));
    }
}
