package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.Role.RoleService;
import org.junit.jupiter.api.BeforeEach;
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
    private RoleRepo roleRepo;

    @Autowired
    private RoleService roleService;

    @BeforeEach
    void setup() {
        roleRepo.deleteAll();
    }

    @Test
    @DisplayName("Create role")
    void create_role() {

        final Role role = new Role(0, "TEST_ROLE");
        final Role saved = roleService.createRole(role);

        assertEquals(saved.getName(), role.getName());
        assertEquals(saved.getParent(), role.getParent());
        assertThat(saved.getId(), instanceOf(Long.TYPE));
    }

    @Test
    @DisplayName("Delete role by id")
    void delete_role_by_id() {

        final Role role = new Role(0, "TEST_ROLE");
        final Role saved = roleService.createRole(role);

        roleService.deleteById(saved.getId());

        assertEquals(0, roleRepo.count());
    }
}
