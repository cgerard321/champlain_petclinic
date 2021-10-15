package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */
public class AuthServiceClientIntegrationTest {

    private AuthServiceClient authServiceClient;
    private MockWebServer server;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                "http://auth-service",
                "1234"
        );
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("Given valid register information, register user")
    void valid_register() {
        final MockResponse mockResponse = new MockResponse();
        mockResponse.setHeader("Content-Type", "application/json");
        server.enqueue(mockResponse);
    }
}
