package com.petclinic.auth;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleIDLessDTO;
import com.petclinic.auth.Role.RoleMapper;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.*;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserServiceTests {

    final String
            USER = "user",
            PASS = "pass",
            EMAIL = "email";

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
    void create_new_user() throws Exception {
        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        final User createdUser = userService.createUser(userIDLessDTO);
        assertEquals(createdUser.getUsername(), userIDLessDTO.getUsername());
        assertEquals(createdUser.getPassword(), userIDLessDTO.getPassword());
        assertEquals(createdUser.getEmail(), userIDLessDTO.getEmail());
    }

}
