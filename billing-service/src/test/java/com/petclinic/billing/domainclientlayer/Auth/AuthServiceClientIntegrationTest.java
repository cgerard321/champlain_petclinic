package com.petclinic.billing.domainclientlayer.Auth;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.UserDetails;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
@RequiredArgsConstructor
public class AuthServiceClientIntegrationTest {
    private MockWebServer server;
    private AuthServiceClient authServiceClient;
    @BeforeEach
    void setup() {
        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort()));
    }

    @AfterEach
    void shutdown() throws IOException {
        if (server != null) {
            server.shutdown();
        }
    }




    @Test
    @DisplayName("Should return user details when valid userId is provided")
    void shouldReturnUserDetails_WhenValidUserIdIsProvided() throws IOException {
        // Arrange
        UserDetails expectedUser = UserDetails.builder()
                .username("username")
                .userId("userId")
                .email("email")
                .build();
        String jwtToken = "jwtToken";
        String userId = "userId";

        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(new ObjectMapper().writeValueAsString(expectedUser)));

        // Act
        Mono<UserDetails> result = authServiceClient.getUserById(jwtToken, userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedUser)
                .verifyComplete();
    }
}
