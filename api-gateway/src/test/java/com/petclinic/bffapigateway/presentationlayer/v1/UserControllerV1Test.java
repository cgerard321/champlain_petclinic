package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.presentationlayer.v1.UserControllerV1;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.dtos.Auth.UserDetails;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {UserControllerV1.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
class UserControllerV1Test {

    @Autowired
    private WebTestClient client;

    @MockBean
    private AuthServiceClient authServiceClient;

    @Test
    void whenGetUserById_thenReturnUserDetails() {
        String userId = "test-user-id";
        String token = "test-token";
        
        UserDetails userDetails = UserDetails.builder()
                .userId(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(authServiceClient.getUserById(token, userId))
                .thenReturn(Mono.just(userDetails));

        client.get()
                .uri("/api/gateway/users/{userId}", userId)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDetails.class)
                .value(user -> {
                    assertEquals(userId, user.getUserId());
                    assertEquals("testuser", user.getUsername());
                    assertEquals("test@example.com", user.getEmail());
                });

        verify(authServiceClient).getUserById(token, userId);
    }

    @Test
    void whenGetUserById_thenReturnNotFound() {
        String userId = "non-existent-user";
        String token = "test-token";

        when(authServiceClient.getUserById(token, userId))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/users/{userId}", userId)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isNotFound();

        verify(authServiceClient).getUserById(token, userId);
    }

    @Test
    void whenUpdateUsername_thenReturnUpdatedUsername() {
        String userId = "test-user-id";
        String newUsername = "newusername";
        String token = "test-token";

        when(authServiceClient.updateUsername(userId, newUsername, token))
                .thenReturn(Mono.just(newUsername));

        client.patch()
                .uri("/api/gateway/users/{userId}/username", userId)
                .cookie("Bearer", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUsername))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(username -> assertEquals(newUsername, username));

        verify(authServiceClient).updateUsername(userId, newUsername, token);
    }

    @Test
    void whenUpdateUsername_thenReturnNotFound() {
        String userId = "non-existent-user";
        String newUsername = "newusername";
        String token = "test-token";

        when(authServiceClient.updateUsername(userId, newUsername, token))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/users/{userId}/username", userId)
                .cookie("Bearer", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUsername))
                .exchange()
                .expectStatus().isNotFound();

        verify(authServiceClient).updateUsername(userId, newUsername, token);
    }

    @Test
    void whenCheckUsernameAvailability_thenReturnTrue() {
        String username = "testuser";
        String token = "test-token";

        when(authServiceClient.checkUsernameAvailability(username, token))
                .thenReturn(Mono.just(true));

        client.get()
                .uri("/api/gateway/users/username/check?username={username}", username)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(available -> assertEquals(true, available));

        verify(authServiceClient).checkUsernameAvailability(username, token);
    }

    @Test
    void whenCheckUsernameAvailability_thenReturnFalse() {
        String username = "takenuser";
        String token = "test-token";

        when(authServiceClient.checkUsernameAvailability(username, token))
                .thenReturn(Mono.just(false));

        client.get()
                .uri("/api/gateway/users/username/check?username={username}", username)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(available -> assertEquals(false, available));

        verify(authServiceClient).checkUsernameAvailability(username, token);
    }
}
