package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerForgotPasswordEndpoint();
        mockServerConfigAuthService.registerResetPasswordEndpoint();
        mockServerConfigAuthService.registerUpdateUserRolesEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerCreateUserEndpoint();
        mockServerConfigAuthService.registerGetUserByIdEndpoint();
        mockServerConfigAuthService.registerUpdateUsernameEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenForgotPassword_thenReturnOk() {
        UserEmailRequestDTO requestModel = new UserEmailRequestDTO();
        requestModel.setEmail("test@example.com");
        requestModel.setUrl("http://example.com/reset-password");

        Mono<Void> result = webTestClient.post()
                .uri("/api/v2/gateway/users/forgot_password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestModel), UserEmailRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseBody()
                .singleOrEmpty();

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void whenResetPassword_thenReturnOk() {
        UserPasswordAndTokenRequestModel requestModel = new UserPasswordAndTokenRequestModel();
        requestModel.setPassword("Cookie123!");
        requestModel.setToken("valid-token");

        Mono<Void> result = webTestClient.post()
                .uri("/api/v2/gateway/users/reset_password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestModel), UserPasswordAndTokenRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseBody()
                .singleOrEmpty();

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void whenUpdateUserRoles_asAdmin_thenReturnUpdatedUserResponseDTO() {
        RolesChangeRequestDTO requestModel = new RolesChangeRequestDTO();
        requestModel.setRoles(List.of("OWNER", "ADMIN"));

        webTestClient.patch()
                .uri("/api/v2/gateway/users/e6248486-d3df-47a5-b2e0-84d31c47533a")
                .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestModel), RolesChangeRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }




    @Test
    void whenCreateUserUsingV2Endpoint_EmptyResponse_thenReturnBadRequest() {
        Register register = new Register();
        register.setUsername("");
        register.setPassword("");
        register.setEmail("");

        webTestClient.post()
                .uri("/api/v2/gateway/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(register), Register.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void whenGetUserById_thenReturnUserDetails() {
        String userId = "e6248486-d3df-47a5-b2e0-84d31c47533a";

        webTestClient.get()
                .uri("/api/v2/gateway/users/{userId}", userId)
                .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }




    @Test
    void whenGetUserById_Error_thenReturnInternalServerError() {
        String userId = "error-user-id";

        webTestClient.get()
                .uri("/api/v2/gateway/users/{userId}", userId)
                .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }




    @Test
    void whenUpdateUsername_NotFound_thenReturnNotFound() {
        String userId = "non-existent-user-id";
        String newUsername = "newusername";

        webTestClient.patch()
                .uri("/api/v2/gateway/users/{userId}/username", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidAdmin)
                .bodyValue(newUsername)
                .exchange()
                .expectStatus().isNotFound();
    }
}