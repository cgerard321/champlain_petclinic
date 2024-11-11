package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
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
        RolesChangeRequestDTO rolesChangeRequestDTO = new RolesChangeRequestDTO();
        rolesChangeRequestDTO.setRoles(List.of("OWNER", "ADMIN"));

        Mono<UserResponseDTO> result = webTestClient.patch()
                .uri("/api/v2/gateway/users/{userId}", "e6248486-d3df-47a5-b2e0-84d31c47533a")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(rolesChangeRequestDTO), RolesChangeRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(UserResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(userResponseDTO -> {
                    assertNotNull(userResponseDTO);
                    List<String> roleNames = userResponseDTO.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toList());
                    assertEquals(List.of("OWNER", "ADMIN"), roleNames);
                    return true;
                })
                .verifyComplete();
    }
}