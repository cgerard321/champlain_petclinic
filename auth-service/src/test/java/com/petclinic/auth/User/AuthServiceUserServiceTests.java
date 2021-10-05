/**
 * Created by IntelliJ IDEA.
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 *
 */
package com.petclinic.auth.User;

import lombok.SneakyThrows;
import org.aspectj.weaver.ast.Not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import com.petclinic.auth.Exceptions.NotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


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
        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(userIDLessDTO));
    }

    @Test
    @DisplayName("get user by id and succeed")
    void get_user_by_id() throws NotFoundException {
        User user = new User(USER, PASS, EMAIL);
        User saved = userRepo.save(user);
        User found = userService.findUserById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("get user by id and fail")
    void get_user_by_id_and_fail(){
        long id = 1;
        assertFalse(userRepo.findById(id).isPresent());
        assertThrows(NotFoundException.class, () -> userService.findUserById(id));
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
