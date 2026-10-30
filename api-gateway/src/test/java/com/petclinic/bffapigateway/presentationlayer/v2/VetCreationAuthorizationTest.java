package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class VetCreationAuthorizationTest {
    @Autowired private WebTestClient http;
    @MockBean private AuthServiceClient auth;
    private static final String ENDPOINT = "/api/v2/gateway/vets/users/vets";

    @ParameterizedTest
    @ValueSource(strings = {"VET", "CUST", "RECEPTIONIST"})
    void rejectsNonAdminsEvenWithValidCsrf(String role) {
        authorize(role);
        http.post().uri(ENDPOINT).cookie("Bearer", "token").cookie("XSRF-TOKEN", "csrf")
                .header("X-XSRF-TOKEN", "csrf").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isForbidden();
        verify(auth, never()).addVetUser(any(), anyString());
    }
    @Test
    void rejectsUnauthenticatedCreation() {
        http.post().uri(ENDPOINT).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .bodyValue("{}").exchange().expectStatus().isUnauthorized();
        verify(auth, never()).addVetUser(any(), anyString());
    }
    @Test
    void allowsAdminAndForwardsAuthenticationForCleanup() {
        authorize("ADMIN");
        when(auth.addVetUser(any(), eq("token"))).thenReturn(Mono.just(VetResponseDTO.builder().vetId("created-vet").build()));
        http.post().uri(ENDPOINT).cookie("Bearer", "token").cookie("XSRF-TOKEN", "csrf")
                .header("X-XSRF-TOKEN", "csrf").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.vetId").isEqualTo("created-vet");
        verify(auth).addVetUser(any(), eq("token"));
    }
    private void authorize(String role) {
        when(auth.validateToken("token")).thenReturn(Mono.just(ResponseEntity.ok(TokenResponseDTO.builder()
                .userId("user").roles(List.of(role)).build())));
    }
}
