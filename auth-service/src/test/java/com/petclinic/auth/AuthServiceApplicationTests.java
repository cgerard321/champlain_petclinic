package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleIDLessDTO;
import com.petclinic.auth.Role.RoleMapper;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	final String
			USER = "user",
			PASS = "pass",
			EMAIL = "email",
			ROLE_NAME = "role";
	final Role role = new Role(0, ROLE_NAME);

	final Set<Role> ROLES = new HashSet<Role>() {{
		add(role);
	}};

	final long ID = 1L;


	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private RoleMapper roleMapper;

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
	@DisplayName("Map id less role to role")
	void map_id_less_role_to_role() {

		final Role role = roleMapper.idLessDTOToModel(ID_LESS_USER_ROLE);
		assertEquals(role.getId(), 0); // defaults to 0 as it is a primitive decimal integer
		assertEquals(role.getName(), ID_LESS_USER_ROLE.getName());
		assertNull(role.getParent());
	}

	@Test
	@DisplayName("User setters")
	void user_setters() {

		final User user = new User();
		user.setUsername(USER);
		user.setRoles(ROLES);
		user.setEmail(EMAIL);
		user.setPassword(PASS);
		user.setId(ID);

		assertEquals(USER, user.getUsername());
		assertEquals(PASS, user.getPassword());
		assertEquals(EMAIL, user.getEmail());
		assertEquals(ID, user.getId());
		assertTrue(user.getRoles().stream().anyMatch(n -> n.getName().equals(ROLE_NAME)));
	}

	@Test
	@DisplayName("User builder")
	void user_builder() {
		final User user = User.builder()
				.roles(ROLES)
				.id(ID)
				.email(EMAIL)
				.password(PASS)
				.username(USER)
				.build();

		assertEquals(
				format(
				"User.UserBuilder(id=%d, username=%s, password=%s, email=%s, roles=%s)",
						user.getId(), user.getUsername(), user.getPassword(), user.getEmail(),
						user.getRoles().toString()),
				user.toBuilder().toString());

		assertEquals(USER, user.getUsername());
		assertEquals(PASS, user.getPassword());
		assertEquals(EMAIL, user.getEmail());
		assertEquals(ID, user.getId());
		assertTrue(user.getRoles().stream().anyMatch(n -> n.getName().equals(ROLE_NAME)));
	}

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
}
