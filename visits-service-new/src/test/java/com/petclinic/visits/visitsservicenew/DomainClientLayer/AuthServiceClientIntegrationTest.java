package com.petclinic.visits.visitsservicenew.DomainClientLayer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.AuthServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.Rethrower;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.UserDetails;
import com.petclinic.visits.visitsservicenew.Exceptions.GenericHttpException;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

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
        server.shutdown();
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

    @Test
    @DisplayName("Should rethrow exception provided by exceptionProvider")
    void shouldRethrowException() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        Rethrower rethrower = new Rethrower(objectMapper);

        ClientResponse clientResponse = ClientResponse.create(HttpStatus.BAD_REQUEST)
                .body("{\"message\":\"Error message\"}")
                .build();

        Function<Map, ? extends Throwable> exceptionProvider = map -> new GenericHttpException(map.get("message").toString(), HttpStatus.NOT_FOUND);

        // Act
        Mono<? extends Throwable> result = rethrower.rethrow(clientResponse, exceptionProvider);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof GenericHttpException && throwable.getMessage().equals("Error message"))
                .verify();
    }
}
