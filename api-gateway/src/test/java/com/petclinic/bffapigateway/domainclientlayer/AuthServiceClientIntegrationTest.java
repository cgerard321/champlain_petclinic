package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Register;
import com.petclinic.bffapigateway.dtos.UserDetails;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
    private final Register USER_REGISTER = Register.builder()
            .username("username")
            .password("password")
            .email("email")
            .build();

    @BeforeEach
    void setup() {

        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort())
        );
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("Given valid register information, register user")
    void valid_register() throws JsonProcessingException {
        final String asString = objectMapper.writeValueAsString(
                objectMapper.convertValue(USER_REGISTER, UserDetails.class)
                        .toBuilder()
                        .id(1)
                        .roles(Collections.emptySet())
                        .password(null)
                        .build()
        );

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(asString);

        server.enqueue(mockResponse);

        final UserDetails block = authServiceClient.createUser(USER_REGISTER).block();

        assertEquals(USER_REGISTER.getEmail(), block.getEmail());
        assertEquals(USER_REGISTER.getUsername(), block.getUsername());
        assertNull(block.getPassword());
        assertNotNull(block.getId());
        assertEquals(0, block.getRoles().size());
    }
}
