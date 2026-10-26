package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.config.GlobalServletExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.utils.Utility;
import com.petclinic.bffapigateway.utils.Security.Filters.*;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = InventoryControllerV1.class, properties = {
        "frontend.url=http://localhost:4200", "TOKEN_PREFIX=Bearer", "EXPIRATION_TIME_MINUTES=60"
})
@ContextConfiguration(classes = {
        InventoryControllerV1.class, Utility.class, JwtTokenFilter.class, JwtTokenUtil.class,
        SecurityConst.class, RoleFilter.class, IsUserFilter.class, CsrfFilter.class,
        GlobalServletExceptionHandler.class
})
class InventoryFavoriteAuthorizationTest {
    private static final String ID = "dfa0a7e3-5a40-4b86-881e-9549ecda5e4b";
    private static final String URL = "/api/gateway/inventories/" + ID + "/important";

    @Autowired private WebTestClient client;
    @MockBean private AuthServiceClient authServiceClient;
    @MockBean private InventoryServiceClient inventoryServiceClient;

    private void authenticate(String role) {
        when(authServiceClient.validateToken("valid-token")).thenReturn(Mono.just(
                ResponseEntity.ok(TokenResponseDTO.builder()
                        .userId("employee-id").roles(List.of(role)).build())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"VET", "ADMIN", "INVENTORY_MANAGER"})
    void favoriteAndUnfavoriteAreAuthorized(String role) {
        authenticate(role);
        AtomicBoolean savedFavorite = new AtomicBoolean(false);
        when(inventoryServiceClient.updateImportantStatus(eq(ID), anyBoolean()))
                .thenAnswer(invocation -> Mono.fromRunnable(() ->
                        savedFavorite.set(invocation.getArgument(1))));

        for (boolean important : new boolean[]{true, false}) {
            client.patch().uri(URL)
                    .cookie("Bearer", "valid-token").cookie("XSRF-TOKEN", "csrf-token")
                    .header("X-XSRF-TOKEN", "csrf-token")
                    .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("important", important))
                    .exchange().expectStatus().isOk();
            assertEquals(important, savedFavorite.get());
            verify(inventoryServiceClient).updateImportantStatus(ID, important);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"RECEPTIONIST", "CUST"})
    void unauthorizedRolesCannotChangeFavorites(String role) {
        authenticate(role);
        client.patch().uri(URL)
                .cookie("Bearer", "valid-token").cookie("XSRF-TOKEN", "csrf-token")
                .header("X-XSRF-TOKEN", "csrf-token")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("important", true))
                .exchange().expectStatus().isForbidden();
        verify(authServiceClient).validateToken("valid-token");
        verifyNoInteractions(inventoryServiceClient);
    }

    @Test
    void unauthenticatedRequestRemainsUnauthorized() {
        client.patch().uri(URL)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("important", true))
                .exchange().expectStatus().isUnauthorized();
        verifyNoInteractions(authServiceClient, inventoryServiceClient);
    }
}
