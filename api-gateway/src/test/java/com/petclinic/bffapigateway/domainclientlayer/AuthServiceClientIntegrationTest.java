package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.Register;
import com.petclinic.bffapigateway.dtos.UserDetails;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
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
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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

    @Test
    @DisplayName("Given valid JWT, verify user")
    void valid_verification() throws JsonProcessingException {
        final String asString = objectMapper.writeValueAsString(
                objectMapper.convertValue(USER_REGISTER, UserDetails.class)
                        .toBuilder()
                        .id(1)
                        .roles(Collections.emptySet())
                        .password(null)
                        .build()
        );
        final String token = "some.valid.token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(asString);

        server.enqueue(mockResponse);

        final UserDetails block = authServiceClient.verifyUser(token).block();

        assertEquals(USER_REGISTER.getEmail(), block.getEmail());
        assertEquals(USER_REGISTER.getUsername(), block.getUsername());
        assertNull(block.getPassword());
        assertNotNull(block.getId());
        assertEquals(0, block.getRoles().size());
    }

    @Test
    @DisplayName("Given valid Login, return JWT")
    void valid_login() {

        final Login login = Login.builder()
                .email("email")
                .password("password")
                .build();
        final String token = "some.valid.token";
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(token);

        server.enqueue(mockResponse);

        final String block = authServiceClient.login(login).block();

        assertEquals(token, block);
    }

    @Test
    @DisplayName("Given invalid Login, throw 401")
    void invalid_login() throws JsonProcessingException {

        final String errorMessage = "bad login >:(";
        final String asString = objectMapper.writeValueAsString(new HttpErrorInfo(UNAUTHORIZED.value(), errorMessage));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(asString)
                .status("HTTP/1.1 401 Unauthorized");

        server.enqueue(mockResponse);

        final GenericHttpException ex = assertThrows(GenericHttpException.class, authServiceClient.login(new Login()).block());

        assertEquals(UNAUTHORIZED, ex.getHttpStatus());
        assertEquals(errorMessage, ex.getMessage());
    }
}
