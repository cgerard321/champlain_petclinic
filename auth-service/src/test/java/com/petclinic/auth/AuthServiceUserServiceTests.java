package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleIDLessDTO;
import com.petclinic.auth.Role.RoleMapper;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.*;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Throw;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserServiceTests {

    final String
            USER = "user",
            PASS = "pass",
            EMAIL = "email@gmail.com";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;


    private UserController userController;
    private final UserIDLessDTO ID_LESS_USER = new UserIDLessDTO();

    @BeforeEach
    void setup() {
        userRepo.deleteAllInBatch();
    }

    @Test
    @DisplayName("Create new user")
    void create_new_user() {
        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        final User createdUser = userService.createUser(userIDLessDTO);
        assertEquals(createdUser.getUsername(), userIDLessDTO.getUsername());
        assertEquals(createdUser.getPassword(), userIDLessDTO.getPassword());
        assertEquals(createdUser.getEmail(), userIDLessDTO.getEmail());
    }
    @SneakyThrows
    @Test
    @DisplayName("Reset user password")
    void test_user_password_reset() {

        final String CHANGE = "change";
        final User u = new User(USER, PASS, EMAIL);
        userRepo.save(u);

        User user = userService.passwordReset(u.getId(), CHANGE);

        Optional<User> find = userRepo.findById(user.getId());
        assertTrue(find.isPresent());
        assertEquals(CHANGE, user.getPassword());
    }
    @SneakyThrows
    @Test
    @DisplayName("Reset password, passed wrong ID")
    void test_user_password_reset_ID() {

        final String CHANGE = "change";
        final User u = new User(USER, PASS, EMAIL);
        userRepo.save(u);

        assertThrows(NotFoundException.class, () -> userService.passwordReset(10000, ""));
    }

    @Test
    @DisplayName("Create new user with email already in use")
    void create_new_user_with_same_email() {
        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        User userMap = userMapper.idLessDTOToModel(userIDLessDTO);
        User saved = userRepo.save(userMap);
        assertThrows(DuplicateKeyException.class, () -> userService.createUser(userIDLessDTO));
    }
}
