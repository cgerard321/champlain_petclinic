package com.petclinic.auth.Role;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.Role.data.RoleIDLessDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceRoleDTOTests {

    final String
            ROLE_NAME = "role";
    final Role role = new Role(0, ROLE_NAME);

    @Test
    @DisplayName("Role id less DTO builder")
    void role_id_less_dto_builder() {

        final RoleIDLessDTO dto = RoleIDLessDTO.builder()
                .name(ROLE_NAME)
                .parent(null)
                .build();

        assertEquals(ROLE_NAME, dto.getName());
        assertNull(role.getParent());
        assertEquals(
                format("RoleIDLessDTO.RoleIDLessDTOBuilder(name=%s, parent=%s)", dto.getName(), dto.getParent()),
                dto.toBuilder().toString()
        );
    }

    @Test
    @DisplayName("Role id less DTO setters")
    void role_id_less_dto_setters() {

        final RoleIDLessDTO roleIDLessDTO = new RoleIDLessDTO();

        roleIDLessDTO.setName(ROLE_NAME);
        roleIDLessDTO.setParent(null);

        assertEquals(ROLE_NAME, roleIDLessDTO.getName());
        assertNull(roleIDLessDTO.getParent());
    }
}
