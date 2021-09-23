package com.petclinic.auth;


import com.petclinic.auth.User.*;
import com.petclinic.auth.Role.*;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;
import java.util.Random;
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

	private Validator validator;

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

	@Autowired
	private UserMapper userMapper;

	private final RoleIDLessDTO ID_LESS_USER_ROLE = new RoleIDLessDTO("user");

	private final UserIDLessDTO ID_LESS_USER_USER = new UserIDLessDTO("usernameTest", "passwordTest", "emailTest");

	@BeforeEach
	void setup() {
		roleRepo.deleteAllInBatch();
		userRepo.deleteAllInBatch();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void contextLoads(){}

	@Test
	@DisplayName("Map id less role to role")
	void map_id_less_role_to_role() {

		final Role role = roleMapper.idLessDTOToModel(ID_LESS_USER_ROLE);
		assertEquals(role.getId(), 0); // defaults to 0 as it is a primitive decimal integer
		assertEquals(role.getName(), ID_LESS_USER_ROLE.getName());
		assertNull(role.getParent());
	}

	@Test
	@DisplayName("Map id less user to user")
	void map_id_less_user_to_user() {

		final User user = userMapper.idLessDTOToModel(ID_LESS_USER_USER);
		assertEquals(user.getId(), 0); // defaults to 0 as it is a primitive decimal integer
		assertEquals(user.getUsername(), ID_LESS_USER_USER.getUsername());
		assertEquals(user.getPassword(), ID_LESS_USER_USER.getPassword());
		assertEquals(user.getEmail(), ID_LESS_USER_USER.getEmail());
	}

	@Test
	@DisplayName("Map null to role")
	void map_null_to_role() {
		assertNull(roleMapper.idLessDTOToModel(null));
	}

	@Test
	@DisplayName("Map null to user")
	void map_null_to_user() {
		assertNull(userMapper.idLessDTOToModel(null));
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

	@Test
	@DisplayName("Role builder")
	void role_builder() {

		final Role role = Role.builder()
				.id(ID)
				.name(ROLE_NAME)
				.parent(null)
				.build();

		assertEquals(ID, role.getId());
		assertEquals(ROLE_NAME, role.getName());
		assertNull(role.getParent());
		assertEquals(
				format("Role.RoleBuilder(id=%d, name=%s, parent=%s)", role.getId(), role.getName(), role.getParent()),
				role.toBuilder().toString()
		);
	}

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

	@Test
	@DisplayName("Verify if the email is valid and succeed")
	void verify_valid_email_success() throws Exception {
		User user = new User();
		user.setUsername(USER);
		user.setPassword(PASS);
		user.setId(ID);

		user.setEmail("testemail@gmail.com");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertTrue(violations.isEmpty());
	}

	@Test
	@DisplayName("Verify if the email is valid and fail because missing @")
	void detect_invalid_email_missing_at() throws Exception {
		User user = new User();
		user.setUsername(USER);
		user.setPassword(PASS);
		user.setId(ID);
		user.setEmail("testemailgmail.com");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertEquals(violations.size(), 1);

		ConstraintViolation<User> violation = violations.iterator().next();
		assertEquals("the email must be a valid email", violation.getMessage());
		assertEquals("email", violation.getPropertyPath().toString());
		assertEquals("testemailgmail.com", violation.getInvalidValue());
	}

	@Test
	@DisplayName("Verify if the email is valid and fail because of an uppercase")
	void detect_invalid_email_upper_case() throws Exception {
		User user = new User();
		user.setUsername(USER);
		user.setPassword(PASS);
		user.setId(ID);
		user.setEmail("Testemail@gmail.com");
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertEquals(violations.size(), 1);
		ConstraintViolation<User> violation = violations.iterator().next();
		assertEquals("the email must be a valid email", violation.getMessage());
		assertEquals("email", violation.getPropertyPath().toString());
		assertEquals("Testemail@gmail.com", violation.getInvalidValue());
	}

	@Test
	@DisplayName("Submit a completed signup form")
	void submit_completed_signup_form() throws Exception {
		User user = new User(USER, PASS, EMAIL);
		assertEquals(USER, user.getUsername());
		assertEquals(PASS, user.getPassword());
		assertEquals(EMAIL, user.getEmail());
	}
	@Test
	@DisplayName("Submit signup form through constructor of UserIDLessDTO")
	void submit_form_with_constructor_without_id() throws Exception{
		UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
		assertEquals(USER, userIDLessDTO.getUsername());
		assertEquals(PASS, userIDLessDTO.getPassword());
		assertEquals(EMAIL, userIDLessDTO.getEmail());
	}

	@Test
	@DisplayName("User setters")
	void user_id_less_dto_setters() {

		final UserIDLessDTO userIDLessDTO = new UserIDLessDTO();
		userIDLessDTO.setUsername(USER);
		userIDLessDTO.setEmail(EMAIL);
		userIDLessDTO.setPassword(PASS);

		assertEquals(USER, userIDLessDTO.getUsername());
		assertEquals(PASS, userIDLessDTO.getPassword());
		assertEquals(EMAIL, userIDLessDTO.getEmail());
	}

	@Test
	@DisplayName("User dto builder")
	void user_dto_builder() {
		final UserIDLessDTO userIDLessDTO = UserIDLessDTO.builder()
				.email(EMAIL)
				.password(PASS)
				.username(USER)
				.build();

		assertEquals(
				format(
						"UserIDLessDTO.UserIDLessDTOBuilder(username=%s, password=%s, email=%s)",
						userIDLessDTO.getUsername(), userIDLessDTO.getPassword(), userIDLessDTO.getEmail()),
				userIDLessDTO.toBuilder().toString());

		assertEquals(USER, userIDLessDTO.getUsername());
		assertEquals(PASS, userIDLessDTO.getPassword());
		assertEquals(EMAIL, userIDLessDTO.getEmail());
	}
}

