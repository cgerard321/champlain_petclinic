package com.petclinic.auth.Role;

import com.petclinic.auth.Role.data.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthServiceRoleEntityTests {

    final String
            ROLE_NAME = "role";

    final Role role = new Role(0, ROLE_NAME);

    final Set<Role> ROLES = new HashSet<Role>() {{
        add(role);
    }};
    final long ID = 1L;

    @Test
    @DisplayName("Role setters")
    void role_setters() {

        final Role role = new Role();
        role.setId(ID);
        role.setName(ROLE_NAME);
        role.setParent(null);

        assertEquals(ID, role.getId());
        assertEquals(ROLE_NAME, role.getName());
        assertNull(role.getParent());

        final Role PARENT = new Role();

        role.setParent(PARENT);
        assertEquals(PARENT, role.getParent());
    }

    @Test
    @DisplayName("Role builder")
    void role_builder() {

        final Role role = Role.builder()
                .id(ID)
                .name(ROLE_NAME)
                .parent(null)
                .build();

        assertEquals(ID, role.getId());
        assertEquals(ROLE_NAME, role.getName());
        assertNull(role.getParent());
        assertEquals(
                format("Role.RoleBuilder(id=%d, name=%s, parent=%s)", role.getId(), role.getName(), role.getParent()),
                role.toBuilder().toString()
        );
    }
}