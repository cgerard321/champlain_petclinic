package com.petclinic.auth;

import com.petclinic.auth.Role.*;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	private final static Random rng;

	static {
		rng = new Random();
	}

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
	private RoleController roleController;
	private final RoleIDLessDTO ID_LESS_USER_ROLE = new RoleIDLessDTO("user");

	@BeforeEach
	void setup() {
		roleRepo.deleteAllInBatch();
		userRepo.deleteAllInBatch();
	}

	@Test
	void contextLoads() {
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

	@Test
	@DisplayName("Map id less role to role")
	void map_id_less_role_to_role() {

		final Role role = roleMapper.idLessDTOToModel(ID_LESS_USER_ROLE);
		assertEquals(role.getId(), 0); // defaults to 0 as it is a primitive decimal integer
		assertEquals(role.getName(), ID_LESS_USER_ROLE.getName());
		assertNull(role.getParent());
	}

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
		for (int i = 0; i < rng.nextInt(); i++) {
			roleController.deleteRole(save.getId());
			assertFalse(roleRepo.findById(save.getId()).isPresent());
		}
	}

	@Test
	@DisplayName("Get all roles as admin")
	void get_all_roles_as_admin() {
		
	}
}
