package com.petclinic.bffapigateway.presentationlayer.v1.Users;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.dtos.Auth.UserResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.UserControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenUtil;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = UserControllerV1.class)
@Import({Utility.class, JwtTokenFilter.class, RoleFilter.class})
class UserRolesSecurityTest {
    @Autowired WebTestClient client;
    @MockBean AuthServiceClient auth;
    @MockBean JwtTokenUtil tokens;

    private void loggedInAs(String role) {
        when(tokens.getTokenFromRequest(any())).thenReturn("token");
        when(auth.validateToken("token")).thenReturn(Mono.just(ResponseEntity.ok(
                TokenResponseDTO.builder().token("token").userId("caller").roles(List.of(role)).build())));
    }

    private WebTestClient.ResponseSpec patchRoles() {
        return client.patch().uri("/api/gateway/users/target-id")
                .cookie("Bearer", "token")
                .cookie("XSRF-TOKEN", "csrf")          // CsrfFilter needs the cookie and header to match
                .header("X-XSRF-TOKEN", "csrf")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"roles\":[\"ADMIN\"]}")
                .exchange();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUST", "VET", "RECEPTIONIST", "INVENTORY_MANAGER"})
    void nonAdminCannotChangeRoles(String role) {
        loggedInAs(role);
        patchRoles().expectStatus().isForbidden();
        verify(auth, never()).updateUsersRoles(any(), any(), any());
    }

    @Test
    void adminCanChangeRoles() {
        loggedInAs("ADMIN");
        when(auth.updateUsersRoles(eq("target-id"), any(), any()))
                .thenReturn(Mono.just(UserResponseDTO.builder().build()));
        patchRoles().expectStatus().isOk();
    }
}