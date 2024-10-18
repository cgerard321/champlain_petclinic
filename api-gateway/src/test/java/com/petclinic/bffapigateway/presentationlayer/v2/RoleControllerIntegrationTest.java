package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Auth.RoleRequestModel;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoleControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerGetAllRolesEndpoint();
        mockServerConfigAuthService.registerCreateRoleEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenGetAllRoles_thenReturnAllRoles() {
        Flux<Role> result = webTestClient.get()
                .uri("/api/v2/gateway/roles")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Role.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNextMatches(role -> "OWNER".equals(role.getName()))
                .expectNextMatches(role -> "ADMIN".equals(role.getName()))
                .verifyComplete();
    }

    @Test
    void whenCreateRole_thenReturnRole() {
        RoleRequestModel roleRequestModel = RoleRequestModel.builder().name("SUPPORT").build();

        Mono<Role> result = webTestClient.post()
                .uri("/api/v2/gateway/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(roleRequestModel), RoleRequestModel.class)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Role.class)
                .getResponseBody()
                .singleOrEmpty();

        StepVerifier.create(result)
                .expectNextMatches(role -> "SUPPORT".equals(role.getName()))
                .verifyComplete();
    }
}