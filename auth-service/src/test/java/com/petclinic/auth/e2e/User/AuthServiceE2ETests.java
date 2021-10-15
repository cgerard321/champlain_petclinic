/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.e2e.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserIDLessRoleLessDTO;
import com.petclinic.auth.User.UserRepo;
import com.petclinic.auth.User.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceE2ETests {

    @MockBean
    private MailService mailService;

    @SpyBean
    private UserService userService;

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

    private UserIDLessRoleLessDTO ID_LESS_USER;

    @BeforeEach
    void setup() {
        ID_LESS_USER = objectMapper.convertValue(USER, UserIDLessRoleLessDTO.class);
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
                .andExpect(jsonPath("$.verified").doesNotExist())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(USER.getEmail()))
                .andExpect(jsonPath("$.username").value(USER.getUsername()))
                .andExpect(jsonPath("$.roles.length()").value(0));
    }

    @Test
    @DisplayName("When given verification URL, verify email")
    void login_user() throws Exception {

        final String asString = objectMapper.writeValueAsString(ID_LESS_USER);
        AtomicReference<String> verificationJWT = new AtomicReference<>();


        doAnswer(n -> {
            final Mail mail = (Mail) n.callRealMethod();
            verificationJWT.set(mail.getMessage().split("/verification/")[1]);
            return mail;
        }).when(userService).generateVerificationMail(any());

//        when(spy.generateVerificationMail(
//                argThat( n -> n.getEmail().equals(ID_LESS_USER.getEmail() ))
//        )).thenAnswer(n -> {
//            final Mail mail = (Mail) n.callRealMethod();
//            verificationJWT.set(mail.getMessage().split("/verification/")[1]);
//            return mail;
//        });


        mockMvc.perform(post("/users").contentType(APPLICATION_JSON).content(asString))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.verified").doesNotExist())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(USER.getEmail()))
                .andExpect(jsonPath("$.username").value(USER.getUsername()))
                .andExpect(jsonPath("$.roles.length()").value(0));


        mockMvc.perform(get("/users/verification/" + verificationJWT))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.verified").doesNotExist())
                .andExpect(jsonPath("$.email").value(USER.getEmail()))
                .andExpect(jsonPath("$.username").value(USER.getUsername()))
                .andExpect(jsonPath("$.roles.length()").value(0));
    }
}
