package com.petclinic.auth;

import com.petclinic.auth.Role.*;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {
	
	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
	private RoleController roleController;

	private List<User> MOCK_USERS;
	private final int MOCK_USER_LEN = 10;
	private final RoleIDLessDTO ID_LESS_USER_ROLE = new RoleIDLessDTO("user");

	@BeforeEach
	void setup() {

		MOCK_USERS = new ArrayList<>();
		for (int i = 1; i <= MOCK_USER_LEN; i++) {

			MOCK_USERS.add(new User(
					i,
					format("username-%d", i),
					format("password-%d", i),
					format("email-%d", i),
					Collections.EMPTY_SET
			));
		}
//		when(mockUserRepo.findAll())
//				.thenReturn(MOCK_USERS);
//
//		when(mockRoleRepo.save(any(Role.class)))
//				.thenAnswer(s -> {
//
//					final long id = roleDB.size() + 1;
//
//					final Role argument = s.getArgument(0, Role.class);
//					final Role clone = argument.toBuilder().id(id).build();
//
//					roleDB.put(id, clone);
//
//					return clone;
//				});
//
//		when(mockRoleRepo.getRolesByParent(any(Role.class)))
//				.thenAnswer(s -> {
//
//					final Role argument = s.getArgument(0, Role.class);
//					Set<Role> toReturn = new HashSet<>();
//
//					for(Role r : roleDB.values()) {
//						if(r.getParent() == null) continue;
//						if(r.getParent().getId() == argument.getId())
//							toReturn.add(r);
//					}
//
//					return toReturn;
//				});
//
//		when(mockRoleRepo.findAll(any(Pageable.class)))
//				.thenAnswer(s -> new PageImpl<>(new ArrayList<>(roleDB.values())));
//
//		when(mockRoleRepo.findById(any(Long.TYPE)))
//				.thenAnswer(s -> Optional.of(roleDB.get(s.getArgument(0, Long.class))));
	}

	@Test
	void contextLoads() {
	}


	@Test
	@DisplayName("Retrieve all Users from mock database")
	void retrieve_all_users_from_mock_database() {
		assertEquals(userRepo.findAll().size(), 10);
	}

	@Test
	@DisplayName("Add orphan role")
	void add_orphan_role() {

		final Role testRole = roleRepo.save(new Role(0, "test", null));

		assertEquals(testRole.getName(), "test");
		assertEquals(roleDB.size(), testRole.getId());
		assertNull(testRole.getParent());
	}

	@Test
	@DisplayName("Add parented role")
	void add_parented_role() {

		final Role parent = roleRepo.save(new Role(0, "parent", null));
		assertEquals(parent.getName(), "parent");
		assertEquals(roleDB.size(), parent.getId());
		assertNull(parent.getParent());

		final Role child = roleRepo.save(new Role(0, "child", parent));
		assertEquals(child.getName(), "child");
		assertEquals(roleDB.size(), child.getId());
		assertEquals(child.getParent().getId(), parent.getId());
	}

	@Test
	@DisplayName("Get children of parent role")
	void get_all_children_of_parent_role() {

		final int CHILD_COUNT = 3;

		final Role parent = roleRepo.save(new Role(0, "parent", null));
		assertEquals(parent.getName(), "parent");
		assertEquals(roleDB.size(), parent.getId());
		assertNull(parent.getParent());

		for (int i = 0; i < CHILD_COUNT; i++) {

			final Role child = roleRepo.save(new Role(0, "child" + i, parent));
			assertEquals(child.getName(), "child" + i);
			assertEquals(roleDB.size(), child.getId());
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


}
