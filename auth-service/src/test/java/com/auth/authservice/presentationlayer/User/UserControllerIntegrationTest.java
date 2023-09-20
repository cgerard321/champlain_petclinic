package com.auth.authservice.presentationlayer.User;

import com.auth.authservice.Util.Exceptions.HTTPErrorMessage;
import com.auth.authservice.businesslayer.UserService;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import com.auth.authservice.security.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;


    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Before("setup")
    public void setup() {
        String baseUri = "http://localhost:" + "9200";
        this.webTestClient = WebTestClient.bindToServer().baseUrl(baseUri).build();
    }

    @Test
    void validateToken_ShouldSucceed() {

        List<User> users = userRepo.findAll();

        User user = users.get(0);

        String token = jwtTokenUtil.generateToken(user);

        webTestClient.post()
                .uri("/users/validate-token")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", token)
                .bodyValue(token)
                .exchange()
                .expectStatus().isOk();
    }



    @Test
    void validateTokenWithInvalidToken_ShouldReturnUnauthorized() {
        String token = "invalidToken";

        webTestClient.post()
                .uri("/users/validate-token")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", token)
                .bodyValue(token)
                .exchange()
                .expectStatus().isUnauthorized();
    }



    @Test
    void validateTokenWithCORSViolation_ShouldReturnForbidden() {
        List<User> users = userRepo.findAll();

        User user = users.get(0);

        String token = jwtTokenUtil.generateToken(user);

        webTestClient.post()
                .uri("/users/validate-token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:3000")//We only accept requests from 8080(frontend) and 9200(for tests)
                .cookie("Bearer", token)
                .bodyValue(token)
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void validateTokenWithNoToken_ShouldReturnUnauthorized() {
        webTestClient.post()
                .uri("/users/validate-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }





    @Test
    void userLogin_ShouldSucceed(){


        UserIDLessUsernameLessDTO userDTO = UserIDLessUsernameLessDTO.builder()
                .email("admin@admin.com")
                .password("pwd")
                .build();


        webTestClient.post()
                .uri("/users/login")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.COOKIE)
                .expectHeader().value(HttpHeaders.COOKIE,s -> {
                    assert s.contains("Bearer");
                })
                .expectBody(UserPasswordLessDTO.class)
                .value(user -> {
                    assertEquals(user.getEmail(),(userDTO.getEmail()));
                    assertEquals(user.getRoles().size(),2);
                });
    }


    @Test
    void userLoginWithInvalidCredentials_ShouldReturnUnauthorized(){
        UserIDLessUsernameLessDTO userDTO = UserIDLessUsernameLessDTO.builder()
                .email("invalidEmail").password("pwd").build();

        webTestClient.post()
                .uri("/users/login")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(HTTPErrorMessage.class).
                value(error -> {
                    assertEquals(error.getMessage(),"Incorrect password for user with email: "+"invalidEmail");
                    assertEquals(error.getStatusCode(),401);
                    assertNotNull(error.getTimestamp());
                });
        }


}