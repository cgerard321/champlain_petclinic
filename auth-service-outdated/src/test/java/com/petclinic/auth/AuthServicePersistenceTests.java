/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 */
package com.petclinic.auth;

import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.UserIDLessUsernameLessDTO;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Random;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional(propagation  = Propagation.NOT_SUPPORTED)
@ActiveProfiles("test")
public class AuthServicePersistenceTests {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    private final User DEFAULT_USER =
            new User("username-1", "pas$word-123", "email-1@gmail.com", Collections.EMPTY_SET);


    private final Role
            ROLE_ADMIN = new Role(0, "ADMIN"),
            ROLE_USER = new Role(0, "USER");

    private final static Random rng;

    static {
        rng = new Random();
    }

    @BeforeEach
    void cleanUp() {
        userRepo.deleteAll();
        roleRepo.deleteAll();
    }

    @Test
    @DisplayName("Add one User to database")
    void add_user_to_database() {

        User created = addDefaultUser();

        assertNotNull(created);

        User byId = userRepo.findById(created.getId()).get();

        assertEquals(DEFAULT_USER.getUsername(), byId.getUsername());
        assertEquals(DEFAULT_USER.getEmail(), byId.getEmail());
        assertEquals(DEFAULT_USER.getPassword(), byId.getPassword());
    }

    @Test
    @DisplayName("Add then remove User from database")
    void add_then_remove_user() {

        User created = addDefaultUser();

        assertNotNull(created);

        userRepo.delete(created);

        assertFalse(userRepo.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Add two users with the same email")
    void add_two_users_with_same_email() {

        addDefaultUser();

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class , this::addDefaultUser);
    }

    @Test
    @DisplayName("Create a role")
    void add_role() {
        final Role created = roleRepo.save(ROLE_ADMIN);
        assertNull(created.getParent());
    }

    @Test
    @DisplayName("Create then delete a role")
    void create_then_delete_role() {

        final Role created = roleRepo.save(ROLE_ADMIN);
        roleRepo.delete(created);
    }

    @Test
    @DisplayName("Create parent with many children roles")
    void add_parent_with_many_children_roles() {

        final int CHILD_COUNT = 3;

        final Role parent = addUserRole();

        for (int i = 0; i < CHILD_COUNT; i++) {
            roleRepo.save(new Role(i, format("role-%d", i), parent));
        }

        assertEquals(roleRepo.getRolesByParent(parent).size(), CHILD_COUNT);
    }

    @Test
    @DisplayName("Add two roles with the same name")
    void add_two_roles_with_same_name() {

        addAdminRole();
        assertThrows(DataIntegrityViolationException.class, this::addAdminRole);
    }

    @Test
    @DisplayName("Add extended role")
    void add_extended_role() {

        final Role parent = addUserRole();

        final Role child = new Role(1, "cool_user", parent);

        final Role created = roleRepo.save(child);

        assertEquals(parent.getId(), created.getParent().getId());
    }

    @Test
    @DisplayName("Add role to user")
    void add_role_to_user() {

        final User user = addDefaultUser();
        final Role userRole = addUserRole();
        user.getRoles().add(userRole);

        final User updated = userRepo.save(user);

        assertTrue(updated.getRoles().stream().anyMatch(n -> n.getId() == userRole.getId()));
    }

    @Test
    @DisplayName("Delete parent without deleting child role")
    void delete_parent_without_deleting_child_role() {

        final Role parent = addUserRole();
        final Role child = new Role(-1, "cool_user", parent);
        final Role created = roleRepo.save(child);
        assertEquals(parent.getId(), created.getParent().getId());

        assertThrows(DataIntegrityViolationException.class, () -> roleRepo.delete(parent));
    }

    @Test
    @DisplayName("Delete child then parent")
    void delete_child_then_parent_role() {

        final Role parent = addUserRole();
        final Role child = new Role(1, "cool_user", parent);
        final Role created = roleRepo.save(child);
        assertEquals(parent.getId(), created.getParent().getId());

        roleRepo.delete(created);
        assertFalse(roleRepo.findById(child.getId()).isPresent());

        roleRepo.delete(parent);
        assertFalse(roleRepo.findById(parent.getId()).isPresent());
    }

    @Test
    @DisplayName("Delete role referenced by user")
    void delete_role_referenced_by_user() {

        User user = addDefaultUser();

        final Role role = addUserRole();

        user.getRoles().add(role);

        user = userRepo.save(user);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(n -> n.getId() == role.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> roleRepo.delete(role));
    }

    @Test
    @DisplayName("Add User with username, password and email")
    void add_user() {


        final User testUser = userRepo.save(new User("testUsername", "testPa$sword123", "test@email.com"));

        assertEquals(testUser.getUsername(), "testUsername");
        assertEquals(testUser.getPassword(), "testPa$sword123");
        assertEquals(testUser.getEmail(), "test@email.com");
    }


    //This Test will be used when the UserServiceImpl class is fully implemented
//    @Test
//    @DisplayName("Add User with username only.")
//    void add_username_only() throws Exception{
//
//        final User testUser = userRepo.save(new User("", "", ""));
//
//        assertThrows(NullPointerException.class, () -> testUser.setUsername(""));
//    }

    private Role addRoleAsClone(Role r) {
        return roleRepo.save(r.toBuilder().id(AuthServicePersistenceTests.rng.nextInt()).build());
    }

    private Role addAdminRole() {
        return addRoleAsClone(ROLE_ADMIN);
    }

    private Role addUserRole() {
        return addRoleAsClone(ROLE_USER);
    }

    private User addDefaultUser() {
        User deepCopy = DEFAULT_USER.toBuilder()
                .id(AuthServicePersistenceTests.rng.nextInt())
                .build();
        return userRepo.save(deepCopy);
    }

}
