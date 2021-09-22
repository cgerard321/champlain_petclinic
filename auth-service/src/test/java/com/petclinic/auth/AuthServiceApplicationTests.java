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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthServiceApplicationTests {

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
	@DisplayName("User setters test")
	void user_setters() {

		final String
				USER = "user",
				PASS = "pass",
				EMAIL = "email",
				ROLE_NAME = "role";
		final Role role = new Role(0, ROLE_NAME);

		final long ID = 1L;

		final Set<Role> ROLES = new HashSet<Role>() {{
			add(role);
		}};

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
}
