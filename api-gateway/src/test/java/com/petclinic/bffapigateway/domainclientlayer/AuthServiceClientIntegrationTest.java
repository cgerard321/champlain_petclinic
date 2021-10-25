package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.*;
import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.Register;
import com.petclinic.bffapigateway.dtos.Role;
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
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import springfox.documentation.spring.web.json.Json;

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
    void valid_login() throws JsonProcessingException {

        final UserDetails userDetails = objectMapper.convertValue(USER_REGISTER, UserDetails.class)
                .toBuilder()
                .id(1)
                .roles(Collections.emptySet())
                .password(null)
                .build();

        final String asString = objectMapper.writeValueAsString(userDetails);

        final Login login = Login.builder()
                .email(USER_REGISTER.getEmail())
                .password(USER_REGISTER.getPassword())
                .build();
        final String token = "some.valid.token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setBody(asString);

        server.enqueue(mockResponse);

        final Tuple2<String, UserDetails> block = authServiceClient.login(login).block();

        assertEquals(USER_REGISTER.getEmail(), block.getT2().getEmail());
        assertEquals(USER_REGISTER.getUsername(), block.getT2().getUsername());
        assertNull(block.getT2().getPassword());
        assertNotNull(block.getT2().getId());
        assertEquals(0, block.getT2().getRoles().size());
        assertEquals(token, block.getT1());
    }

    @Test
    @DisplayName("Given invalid Login, throw 401")
    void invalid_login() throws JsonProcessingException {

        final String errorMessage = "Unauthorized";
        final String asString = objectMapper.writeValueAsString(new HttpErrorInfo(UNAUTHORIZED.value(), errorMessage));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(asString)
                .status("HTTP/1.1 401 Unauthorized");

        server.enqueue(mockResponse);

        final GenericHttpException ex = assertThrows(GenericHttpException.class, () -> authServiceClient.login(new Login()).block());

        assertEquals(UNAUTHORIZED, ex.getHttpStatus());
        assertEquals(errorMessage, ex.getMessage());
    }

    @Test
    @DisplayName("Should get all roles")
    void shouldGetRoles() throws JsonProcessingException {
        final Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        final Role role = new Role();
        role.setId(2);
        role.setName("vet");
        role.setParent(parentRole);

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(role, Role.class));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(body);

        server.enqueue(mockResponse);

        final Role aRole = authServiceClient.getRoles().blockFirst();

        assertEquals(role.getId(), aRole.getId());
        assertEquals(role.getName(), aRole.getName());
        assertEquals(role.getParent(), aRole.getParent());
    }
  
    @Test
    @DisplayName("Should add a role")
    void shouldAddRole() throws JsonProcessingException {
        final Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        final Role role = new Role();
        role.setId(2);
        role.setName("vet");
        role.setParent(parentRole);

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(role, Role.class));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(body);

        server.enqueue(mockResponse);

        final Role block = authServiceClient.addRole(role).block();

        assertEquals(role.getId(), block.getId());
        assertEquals(role.getName(), block.getName());
        assertEquals(role.getParent(), block.getParent());
    }

    @Test
    @DisplayName("Should delete a role")
    void shouldDeleteRole() throws JsonProcessingException {
        final Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        final Role role = new Role();
        role.setId(2);
        role.setName("vet");
        role.setParent(parentRole);

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(role, Role.class));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(body);

        server.enqueue(mockResponse);

        final Mono<Void> empty = authServiceClient.deleteRole(role.getId());
        assertEquals(empty.block(), null);
    }
}
