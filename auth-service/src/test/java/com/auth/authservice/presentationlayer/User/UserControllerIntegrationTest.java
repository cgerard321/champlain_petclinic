package com.auth.authservice.presentationlayer.User;

import com.auth.authservice.Util.Exceptions.HTTPErrorMessage;
import com.auth.authservice.businesslayer.UserService;
import com.auth.authservice.datalayer.user.ResetPasswordToken;
import com.auth.authservice.datalayer.user.ResetPasswordTokenRepository;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import com.auth.authservice.domainclientlayer.Mail.MailService;
import com.auth.authservice.security.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    @MockBean
    private ResetPasswordTokenRepository resetPasswordTokenRepository;


    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private MailService mailService;

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
                .expectHeader().exists(HttpHeaders.SET_COOKIE)
                .expectHeader().value(HttpHeaders.SET_COOKIE,s -> {
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



        @Test
        void processResetPassword_ShouldSucceed(){

            when(resetPasswordTokenRepository.findResetPasswordTokenByToken(anyString())).thenReturn(new ResetPasswordToken(1L,"testToken"));

            UserResetPwdWithTokenRequestModel resetRequest = new UserResetPwdWithTokenRequestModel();
            resetRequest.setToken("testToken");
            resetRequest.setPassword("newPassword");

            webTestClient.post()
                    .uri("/users/reset_password")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(resetRequest)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        void processResetPassword_ShouldFindNoToken(){

            when(resetPasswordTokenRepository.findResetPasswordTokenByToken(anyString())).thenReturn(null);

            UserResetPwdWithTokenRequestModel resetRequest = new UserResetPwdWithTokenRequestModel();
            resetRequest.setToken("testToken");
            resetRequest.setPassword("newPassword");

            webTestClient.post()
                    .uri("/users/reset_password")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(resetRequest)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }



    @Test
    void sendForgottenPasswordLink_ShouldSucceed(){

        UserResetPwdRequestModel resetPwdRequestModel = UserResetPwdRequestModel.builder()
                .email("admin@admin.com")
                        .build();

        when(mailService.sendMail(any())).thenReturn("Your verification link: someFakeLink");


        webTestClient.post()
                .uri("/users/forgot_password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(resetPwdRequestModel)
                .exchange()
                .expectStatus().isOk();

    }


    @Test
    void sendForgottenPasswordLink_ShouldFail(){

        UserResetPwdRequestModel resetPwdRequestModel = UserResetPwdRequestModel.builder()
                .email("invalidEmail")
                .build();

        when(mailService.sendMail(any())).thenReturn("Your verification link: someFakeLink");

        webTestClient.post()
                .uri("/users/forgot_password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(resetPwdRequestModel)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HTTPErrorMessage.class)
                .value(error -> {
                    assertEquals("Could not find any customer with the email invalidEmail",error.getMessage());
                    assertEquals(404,error.getStatusCode());
                    assertNotNull(error.getTimestamp());
                });

    }

}