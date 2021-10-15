/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.User;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.User.data.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserEntityTests {

    final String
            USER = "user",
            PASS = "Pas$word123",
            EMAIL = "email",
            ROLE_NAME = "role";

    final Role role = new Role(0, ROLE_NAME);

    final Set<Role> ROLES = new HashSet<Role>() {{
        add(role);
    }};
    final long ID = 1L;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void setup() {
        userRepo.deleteAllInBatch();
    }

    @Test
    @DisplayName("User setters")
    void user_setters() {

        final User user = new User();
        user.setUsername(USER);
        user.setRoles(ROLES);
        user.setEmail(EMAIL);
        user.setPassword(PASS);
        user.setId(ID);

        assertEquals(USER, user.getUsername());
        assertEquals(PASS, user.getPassword());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(ID, user.getId());
        assertTrue(user.getRoles().stream().anyMatch(n -> n.getName().equals(ROLE_NAME)));
    }

    @Test
    @DisplayName("User builder")
    void user_builder() {
        final User user = User.builder()
                .roles(ROLES)
                .id(ID)
                .email(EMAIL)
                .password(PASS)
                .username(USER)
                .build();


        assertEquals(USER, user.getUsername());
        assertEquals(PASS, user.getPassword());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(ID, user.getId());
        assertTrue(user.getRoles().stream().anyMatch(n -> n.getName().equals(ROLE_NAME)));
    }

    @Test
    @DisplayName("Given user with roles, get authorities")
    void getAuthorities() {

        final Role parent = new Role(-1, "parent"),
                kid = new Role(-1, "kid", parent);

        HashSet<Role> basedRoles = new HashSet<>(ROLES);
        basedRoles.add(kid);

        final User build = User.builder()
                .roles(basedRoles)
                .build();

        final Set<String> roleNames = basedRoles.parallelStream()
                .map(n -> format("ROLE_%s", n.getName()))
                .collect(Collectors.toSet());
        roleNames.add(format("ROLE_%s", parent.getName())); // Manually add parent because we are not crawling up the hierarchy

        assertEquals(roleNames.size(), build.getAuthorities().size());

        for (GrantedAuthority authority : build.getAuthorities()) {
            assertTrue(roleNames.contains(authority.getAuthority()));
        }
    }

    @Test
    @DisplayName("Given any user, account is not expired")
    void isAccountNonExpired() {
        assertTrue(new User().isAccountNonExpired());
    }

    @Test
    @DisplayName("Given any user, account is not locked")
    void isAccountNonLocked() {
        assertTrue(new User().isAccountNonLocked());
    }

    @Test
    @DisplayName("Given any user, credentials is non expired")
    void isCredentialsNonExpired() {
        assertTrue(new User().isAccountNonExpired());
    }

    @Test
    @DisplayName("Given verified user, account is enabled")
    void isEnabled() {
        assertTrue(User.builder().verified(true).build().isEnabled());
    }

    @Test
    @DisplayName("Given unverified user, account is enabled")
    void isNotEnabled() {
        assertFalse(new User().isEnabled());
    }
}