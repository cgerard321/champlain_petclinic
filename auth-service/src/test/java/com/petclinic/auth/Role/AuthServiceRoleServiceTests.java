/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 */
package com.petclinic.auth.Role;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.Role.data.RoleIDLessDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        final RoleIDLessDTO roleIDLessDTO = new RoleIDLessDTO("TEST_ROLE");
        final Role saved = roleService.createRole(roleIDLessDTO);

        assertEquals(saved.getName(), roleIDLessDTO.getName());
        assertEquals(saved.getParent(), roleIDLessDTO.getParent());
        assertThat(saved.getId(), instanceOf(Long.TYPE));
    }

    @Test
    @DisplayName("Delete role by id")
    void delete_role_by_id() {

        final RoleIDLessDTO roleIDLessDTO = new RoleIDLessDTO("TEST_ROLE");
        final Role saved = roleService.createRole(roleIDLessDTO);

        roleService.deleteById(saved.getId());

        assertEquals(0, roleRepo.count());
    }

    @Test
    @DisplayName("Get all roles")
    void get_all_roles() {

        final int ROLE_COUNT = 10;

        for (int i = 0; i < ROLE_COUNT; i++) {
            roleRepo.save(new Role(0, format("test-%d", i)));
        }

        assertEquals(ROLE_COUNT, roleRepo.count());
        assertEquals(ROLE_COUNT, roleService.findAll(PageRequest.of(0, 10)).getTotalElements());
    }
}
