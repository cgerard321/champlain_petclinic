package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleIDLessDTO;
import com.petclinic.auth.Role.RoleMapper;
import com.petclinic.auth.Role.RoleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserMapperTests {

    @Autowired
    private UserMapper userMapper;

    private final UserIDLessDTO ID_LESS_USER_USER = new UserIDLessDTO("usernameTest", "passwordTest", "emailTest");

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
    @DisplayName("Map null to user")
    void map_null_to_user() {
        assertNull(userMapper.idLessDTOToModel(null));
    }
}