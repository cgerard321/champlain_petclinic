/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.e2e.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Mail.MailService;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserIDLessDTO;
import com.petclinic.auth.User.UserMapper;
import com.petclinic.auth.User.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceE2ETests {

    @MockBean
    private MailService mailService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final User USER = User.builder()
            .email("e@mail.com")
            .password("MyV3ryC00lP@$$w0rd")
            .username("my cool username")
            .build();

    private UserIDLessDTO ID_LESS_USER;

    @BeforeEach
    void setup() {
        ID_LESS_USER = objectMapper.convertValue(USER, UserIDLessDTO.class);
        userRepo.deleteAllInBatch();
        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");
    }

    @Test
    @DisplayName("When valid user info, register")
    void register_user() throws Exception {

        final String asString = objectMapper.writeValueAsString(ID_LESS_USER);

        mockMvc.perform(post("/users").contentType(APPLICATION_JSON).content(asString))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(USER.getEmail()))
                .andExpect(jsonPath("$.username").value(USER.getUsername()))
                .andExpect(jsonPath("$.roles.length()").value(0));
    }
}
