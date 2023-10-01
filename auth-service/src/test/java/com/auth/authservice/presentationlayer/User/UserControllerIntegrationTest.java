package com.auth.authservice.presentationlayer.User;

import com.auth.authservice.Util.Exceptions.HTTPErrorMessage;
import com.auth.authservice.businesslayer.UserService;
import com.auth.authservice.datalayer.user.*;
import com.auth.authservice.domainclientlayer.Mail.MailService;
import com.auth.authservice.security.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Base64;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtService;

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
                    assertEquals(user.getRoles().size(),1);
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
    void sendForgottenPasswordLinkWithAnExistingToken_ShouldSucceed(){
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


        webTestClient.post()
                .uri("/users/forgot_password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(resetPwdRequestModel)
                .exchange()
                .expectStatus().isOk();

    }


    @Test
    void sendForgotPasswordLinkWithInvalidEmail_ShouldReturnNotFound(){
        UserResetPwdRequestModel resetPwdRequestModel = UserResetPwdRequestModel.builder()
                .email("fake@admin.com")
                .build();

        when(mailService.sendMail(any())).thenReturn("Your verification link: someFakeLink");


        webTestClient.post()
                .uri("/users/forgot_password")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(resetPwdRequestModel)
                .exchange()
                .expectStatus().isNotFound();
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

    @Test
    void createUser_ShouldSucceed() {
        UserIDLessRoleLessDTO userDTO = UserIDLessRoleLessDTO.builder()
                .email("richard2004danon@gmail.com")
                .password("pwd%jfjfjDkkkk8")
                .username("Ricky")
                .userId(new UserIdentifier().getUserId())
                .build();

        webTestClient.post()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPasswordLessDTO.class)
                .value(user -> {
                    assertEquals(userDTO.getEmail(), user.getEmail());
                    assertEquals(userDTO.getUsername(),user.getUsername());

                });

        User user = userRepo.findByEmail(userDTO.getEmail()).get();

        final String base64Token = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(jwtService.generateToken(user).getBytes());

        webTestClient.get()
                        .uri("/users/verification/"+base64Token)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk();

        assertTrue(userRepo.findByEmail(userDTO.getEmail()).get().isVerified());

        userRepo.delete(userRepo.findByEmail(userDTO.getEmail()).get());
    }


    @Test
    void createUser_ShouldFail() {
        UserIDLessRoleLessDTO userDTO = UserIDLessRoleLessDTO.builder()
                .email("email@email.com")
                .password("weakPwd")
                .username("Ricky")
                .build();

        webTestClient.post()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isBadRequest();
        }
    @Test
    void verifyInvalidToken_ShouldReturnBadRequest(){
        User user = userRepo.findByEmail("admin@admin.com").get();

        final String base64Token = "suckyToken&*@()5O9))&)@";

        webTestClient.get()
                .uri("/users/verification/"+base64Token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();


    }



    @Test
    void createUser_ShouldThrowEmailAlreadyExistsException() {
        UserIDLessRoleLessDTO userDTO = UserIDLessRoleLessDTO.builder()
                .email("richard200danon@gmail.com")
                .password("pwd%jfjfjDkkkk8")
                .username("Ric")
                .build();

        User existingUser = new User();
        existingUser.setEmail("richard200danon@gmail.com");
        existingUser.setUsername("existingUsername");
        existingUser.setPassword("pwd%jfjfjDkkkk8");


        userRepo.save(existingUser);


        webTestClient
                .post()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo(String.format("User with e-mail %s already exists", userDTO.getEmail()));



    }

    @Test
    void getAllUsers_ShouldSucceed(){
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));


        webTestClient.get()
                .uri("/users/withoutPages")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer",token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDetails.class)
                .value(users -> {
                    assertEquals(12,users.size());
                });
    }




}