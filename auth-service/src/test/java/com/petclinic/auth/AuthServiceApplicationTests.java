package com.petclinic.auth;

import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@MockBean
	private UserRepo mockUserRepo;

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
					format("email-%d", i)
			));
		}
		when(mockUserRepo.findAll())
				.thenReturn(MOCK_USERS);
	}

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("Retrieve all Users from mock database")
	void retrieve_all_users_from_mock_database() {
		assertEquals(mockUserRepo.findAll().size(), 10);
	}

	//Signup Service Tests
	@Test
	@DisplayName("Given necessary data is inputted, register user.")
	void register_user(){
	}

	@Test
	@DisplayName("Validate email uniqueness in database.")
	void validate_email_uniqueness(){

	}

}
