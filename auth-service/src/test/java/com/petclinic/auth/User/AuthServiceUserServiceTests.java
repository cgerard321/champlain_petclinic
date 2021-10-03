package com.petclinic.auth.User;

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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
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
            PASS = "pas$word123",
            EMAIL = "email@gmail.com";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

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

        final User u = new User(USER, PASS, EMAIL);
        userRepo.save(u);

        assertThrows(NotFoundException.class, () -> userService.passwordReset(10000, ""));
    }

    @Test
    @DisplayName("Create new user with email already in use")
    void create_new_user_with_same_email() {
        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        User userMap = userMapper.idLessDTOToModel(userIDLessDTO);
        userRepo.save(userMap);
        assertThrows(DuplicateKeyException.class, () -> userService.createUser(userIDLessDTO));
    }

    @Test
    @DisplayName("get user by id and succeed")
    void get_user_by_id() throws NotFoundException {
        User user = new User(USER, PASS, EMAIL);
        User saved = userRepo.save(user);
        User found = userService.getUserById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("get user by id and fail")
    void get_user_by_id_and_fail(){
        long id = 1;
        assertFalse(userRepo.findById(id).isPresent());
        assertThrows(NotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    @DisplayName("Get all users")
    void get_all_roles() {

        final int USER_COUNT = 10;

        for (int i = 0; i < USER_COUNT; i++) {
            userRepo.save(new User(USER, PASS, EMAIL + i));
        }

        assertEquals(USER_COUNT, userRepo.count());
        assertEquals(USER_COUNT, userService.findAll(PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    @DisplayName("Delete user by id")
    void delete_role_by_id() {

        final UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        final User saved = userService.createUser(userIDLessDTO);

        userService.deleteUser(saved.getId());

        assertEquals(0, userRepo.count());
    }

    @Test
    @DisplayName("Delete user by id and fail")
    void delete_role_by_id_and_fail() {
        assertEquals(Optional.empty(), userRepo.findById(1l));
    }

}
