package com.petclinic.auth.User;

import com.petclinic.auth.Role.RoleIDLessDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserDTOTests {

    final String
            USER = "user",
            PASS = "Pas$word123",
            EMAIL = "email";

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