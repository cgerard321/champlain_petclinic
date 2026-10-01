package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.Util.Configuration.Security.SecurityConfig;
import com.petclinic.authservice.Util.Configuration.Security.CustomBasicAuthenticationEntryPoint;
import com.petclinic.authservice.businesslayer.UserService;
import com.petclinic.authservice.datamapperlayer.UserMapper;
import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datalayer.user.UserRepo;
import com.petclinic.authservice.security.JwtTokenFilter;
import com.petclinic.authservice.security.JwtTokenUtil;
import com.petclinic.authservice.security.SecurityConst;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtTokenFilter.class, CustomBasicAuthenticationEntryPoint.class})
class PublicUserProfileTest {
    @Autowired MockMvc mvc;
    @MockBean UserService users;
    @MockBean UserMapper mapper;
    @MockBean UserRepo userRepo;
    @MockBean JwtTokenUtil tokens;
    @MockBean SecurityConst constants;
    @MockBean UserDetailsService details;

    private void profile() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("Reviewer");
        when(users.getUserByUserId("author")).thenReturn(user);
    }

    @Test
    void guestGetsOnlyPublicUsername() throws Exception {
        profile();
        mvc.perform(get("/users/author/public-profile"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"Reviewer\"}", true));
        verifyNoInteractions(tokens, mapper);
    }

    @Test
    void staleCookieDoesNotBlockPublicProfile() throws Exception {
        profile();
        mvc.perform(get("/users/author/public-profile").cookie(new Cookie("Bearer", "expired")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"Reviewer\"}", true));
        verifyNoInteractions(tokens);
    }

    @Test
    void fullProfileStillRequiresAuthentication() throws Exception {
        mvc.perform(get("/users/author")).andExpect(status().isUnauthorized());
        verifyNoInteractions(users);
    }

    @Test
    void publicExceptionDoesNotAllowWrites() throws Exception {
        mvc.perform(post("/users/author/public-profile")).andExpect(status().isUnauthorized());
        verifyNoInteractions(users);
    }
}
