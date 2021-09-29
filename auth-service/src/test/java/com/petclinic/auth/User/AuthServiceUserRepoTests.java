package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserRepoTests {

    final long ID = 1L;
    final String
            USER = "user",
            PASS = "pas$word123",
            EMAIL = "email@gmail.com";

    private Validator validator;

    @Autowired
    private RoleRepo roleRepo;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        roleRepo.deleteAllInBatch();
    }

    @Test
    @DisplayName("Verify if the email is valid and succeed")
    void verify_valid_email_success() {
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
    void detect_invalid_email_missing_at() {
        User user = new User();
        user.setUsername(USER);
        user.setPassword(PASS);
        user.setId(ID);
        user.setEmail("testemailgmail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Email must be valid", violation.getMessage());
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("testemailgmail.com", violation.getInvalidValue());
    }

    @Test
    @DisplayName("Submit a completed signup form")
    void submit_completed_signup_form() {

        User user = new User(USER, PASS, EMAIL);
        assertEquals(USER, user.getUsername());
        assertEquals(PASS, user.getPassword());
        assertEquals(EMAIL, user.getEmail());
    }
    @Test
    @DisplayName("Submit signup form through constructor of UserIDLessDTO")
    void submit_form_with_constructor_without_id() {

        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        assertEquals(USER, userIDLessDTO.getUsername());
        assertEquals(PASS, userIDLessDTO.getPassword());
        assertEquals(EMAIL, userIDLessDTO.getEmail());
    }
}
