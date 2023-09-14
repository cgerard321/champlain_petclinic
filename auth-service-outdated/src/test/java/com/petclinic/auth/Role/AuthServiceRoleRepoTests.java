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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceRoleRepoTests {

    @Autowired
    private RoleRepo roleRepo;

    @BeforeEach
    void setup() {
        roleRepo.deleteAllInBatch();
    }

    @Test
    @DisplayName("Add orphan role")
    void add_orphan_role() {

        final Role testRole = roleRepo.save(new Role(0, "test", null));

        assertEquals(testRole.getName(), "test");
        assertThat(testRole.getId(), instanceOf(Long.TYPE));
        assertNull(testRole.getParent());
    }

    @Test
    @DisplayName("Add parented role")
    void add_parented_role() {

        final Role parent = roleRepo.save(new Role(0, "parent", null));
        assertEquals(parent.getName(), "parent");
        assertThat(parent.getId(), instanceOf(Long.TYPE));
        assertNull(parent.getParent());

        final Role child = roleRepo.save(new Role(0, "child", parent));
        assertEquals(child.getName(), "child");
        assertThat(child.getId(), instanceOf(Long.TYPE));
        assertEquals(child.getParent().getId(), parent.getId());
    }

    @Test
    @DisplayName("Get children of parent role")
    void get_all_children_of_parent_role() {

        final int CHILD_COUNT = 3;

        final Role parent = roleRepo.save(new Role(0, "parent", null));
        assertEquals(parent.getName(), "parent");
        assertThat(parent.getId(), instanceOf(Long.TYPE));
        assertNull(parent.getParent());

        for (int i = 0; i < CHILD_COUNT; i++) {

            final Role child = roleRepo.save(new Role(0, "child" + i, parent));
            assertEquals(child.getName(), "child" + i);
            assertThat(child.getId(), instanceOf(Long.TYPE));
            assertEquals(child.getParent().getId(), parent.getId());
        }

        assertEquals(roleRepo.getRolesByParent(parent).size(), CHILD_COUNT);
    }
}
