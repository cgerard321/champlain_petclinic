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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Random;

import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceRoleControllerTests {

    private final static Random rng;

    static {
        rng = new Random();
    }

    @BeforeEach
    void setup() {
        roleRepo.deleteAllInBatch();
    }

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private RoleController roleController;

    private final RoleIDLessDTO ID_LESS_USER_ROLE = new RoleIDLessDTO("user");

    @Test
    @DisplayName("Create a role from controller")
    void create_role_from_controller() {
        final Role role = roleController.createRole(ID_LESS_USER_ROLE);
        assertNotNull(role);
        assertThat(role.getId(), instanceOf(Long.TYPE));
        assertTrue(roleRepo.findById(role.getId()).isPresent());
    }

    @Test
    @DisplayName("Get all roles from controller")
    void get_all_roles_from_controller() {

        final int
                ROLE_COUNT = 20,
                PAGE_LIM = 10,
                STARTING_PAGE = 1;

        for (int i = 0; i < ROLE_COUNT; i++) {
            roleRepo.save(new Role(0, "test"+i, null));
        }

        assertEquals(ROLE_COUNT, roleRepo.count());

        Page<Role> rolePage = roleController.getAllRoles(STARTING_PAGE, PAGE_LIM);
        assertNotNull(rolePage);
        assertEquals(ROLE_COUNT, rolePage.getTotalElements());
        assertEquals(ROLE_COUNT / PAGE_LIM, rolePage.getTotalPages());
    }

    @Test
    @DisplayName("Add then delete role from controller")
    void add_then_delete_role_from_controller() {

        final Role save = roleRepo.save(new Role(0, "test", null));
        final Optional<Role> found = roleRepo.findById(save.getId());
        assertTrue(found.isPresent());
        assertEquals("test", found.get().getName());
        assertNull(found.get().getParent());

        // Idempotency check
        for (int i = 0; i < rng.nextInt(100); i++) {
            roleController.deleteRole(save.getId());
            assertFalse(roleRepo.findById(save.getId()).isPresent());
        }
    }

    @Test
    @DisplayName("Get all roles as admin")
    @WithMockUser(roles = {"ADMIN"})
    void get_all_roles_as_admin() throws Exception {

        final int
                ROLE_COUNT = 1 + rng.nextInt(50),
                PAGE_LIM = 10;

        final int PAGE_COUNT = (int)ceil(ROLE_COUNT * 1.0 / PAGE_LIM);

        for (int i = 0; i < ROLE_COUNT; i++) {
            roleRepo.save(new Role(0, "test"+i, null));
        }

        assertEquals(ROLE_COUNT, roleRepo.count());

        mockMvc.perform(get("/roles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.content.length()", is(min(ROLE_COUNT, PAGE_LIM))))
                .andExpect(jsonPath("$.content[0].parent", nullValue()))
                .andExpect(jsonPath("$.totalElements", is(ROLE_COUNT)))
                .andExpect(jsonPath("$.totalPages", is(PAGE_COUNT)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    @DisplayName("Get all roles as user")
    @WithMockUser(roles = {"USER"})
    void get_all_roles_as_user() throws Exception {

        final int
                ROLE_COUNT = 1 + rng.nextInt(50),
                PAGE_LIM = 10;

        final int PAGE_COUNT = (int)ceil(ROLE_COUNT * 1.0 / PAGE_LIM);

        for (int i = 0; i < ROLE_COUNT; i++) {
            roleRepo.save(new Role(0, "test"+i, null));
        }

        assertEquals(ROLE_COUNT, roleRepo.count());

        mockMvc.perform(get("/roles"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all roles as user")
    void get_all_roles_unauthenticated() throws Exception {

        final int
                ROLE_COUNT = 1 + rng.nextInt(50),
                PAGE_LIM = 10;

        final int PAGE_COUNT = (int)ceil(ROLE_COUNT * 1.0 / PAGE_LIM);

        for (int i = 0; i < ROLE_COUNT; i++) {
            roleRepo.save(new Role(0, "test"+i, null));
        }

        assertEquals(ROLE_COUNT, roleRepo.count());

        mockMvc.perform(get("/roles"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
