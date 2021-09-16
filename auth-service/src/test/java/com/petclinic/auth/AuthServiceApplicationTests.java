package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	private HashMap<Long, Role> roleDB;

	{
		roleDB = new HashMap<>();
	}

	@MockBean
	private UserRepo mockUserRepo;

	@MockBean
	private RoleRepo mockRoleRepo;

	private List<User> MOCK_USERS;
	private final int MOCK_USER_LEN = 10;

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
		when(mockUserRepo.findAll())
				.thenReturn(MOCK_USERS);

		when(mockRoleRepo.save(any(Role.class)))
				.thenAnswer(s -> {

					final long id = roleDB.size() + 1;

					final Role argument = s.getArgument(0, Role.class);
					final Role clone = argument.toBuilder().id(id).build();

					roleDB.put(id, clone);

					return clone;
				});

		when(mockRoleRepo.getRolesByParent(any(Role.class)))
				.thenAnswer(s -> {

					final Role argument = s.getArgument(0, Role.class);
					Set<Role> toReturn = new HashSet<>();

					for(Role r : roleDB.values()) {
						if(r.getParent() == null) continue;
						if(r.getParent().getId() == argument.getId())
							toReturn.add(r);
					}

					return toReturn;
				});
	}

	@Test
	void contextLoads() {
	}


	@Test
	@DisplayName("Retrieve all Users from mock database")
	void retrieve_all_users_from_mock_database() {
		assertEquals(mockUserRepo.findAll().size(), 10);
	}

	@Test
	@DisplayName("Add orphan role")
	void add_orphan_role() {

		final Role testRole = mockRoleRepo.save(new Role(0, "test", null));

		assertEquals(testRole.getName(), "test");
		assertEquals(roleDB.size(), testRole.getId());
		assertNull(testRole.getParent());
	}

	@Test
	@DisplayName("Add parented role")
	void add_parented_role() {

		final Role parent = mockRoleRepo.save(new Role(0, "parent", null));
		assertEquals(parent.getName(), "parent");
		assertEquals(roleDB.size(), parent.getId());
		assertNull(parent.getParent());

		final Role child = mockRoleRepo.save(new Role(0, "child", parent));
		assertEquals(child.getName(), "child");
		assertEquals(roleDB.size(), child.getId());
		assertEquals(child.getParent().getId(), parent.getId());
	}

	@Test
	@DisplayName("Get children of parent role")
	void get_all_children_of_parent_role() {

		final int CHILD_COUNT = 3;

		final Role parent = mockRoleRepo.save(new Role(0, "parent", null));
		assertEquals(parent.getName(), "parent");
		assertEquals(roleDB.size(), parent.getId());
		assertNull(parent.getParent());

		for (int i = 0; i < CHILD_COUNT; i++) {

			final Role child = mockRoleRepo.save(new Role(0, "child" + i, parent));
			assertEquals(child.getName(), "child" + i);
			assertEquals(roleDB.size(), child.getId());
			assertEquals(child.getParent().getId(), parent.getId());
		}

		assertEquals(mockRoleRepo.getRolesByParent(parent).size(), CHILD_COUNT);
	}
}
