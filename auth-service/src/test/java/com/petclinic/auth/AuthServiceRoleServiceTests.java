package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceRoleServiceTests {

    @Autowired
    private RoleService roleService;

    @Test
    @DisplayName("Create role through role service")
    void create_role_through_role_service() {

        final Role role = new Role(0, "TEST_ROLE");
        final Role saved = roleService.createRole(role);

        assertEquals(saved.getName(), role.getName());
        assertEquals(saved.getParent(), role.getParent());
        assertThat(saved.getId(), instanceOf(Long.TYPE));
    }
}
