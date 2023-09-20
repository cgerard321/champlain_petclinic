package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Auth.Login;
import com.petclinic.bffapigateway.dtos.Auth.Register;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Auth.UserPasswordLessDTO;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */
@RequiredArgsConstructor
public class AuthServiceClientIntegrationTest {

    private final SecurityConst securityConst = new SecurityConst(60
            ,"MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDTnkqggdMFu5O58pB8U0f8D4pab_7j5T8jNZfEep23DbvoMCjR6X1cFe4qCsvaY4aDF6d6Vn3TVY2INHMuyOTXqe5vhBWiRgaI3TIPkGjgHZ1f6Up_ZPQFJCGTo2b3OiXBq3LTK7PXvMj2EIQPJrHuX099ACDvO"
            ,"Bearer");

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
                String.valueOf(server.getPort()),
                new RestTemplate(), securityConst);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

//    @Test
//    @DisplayName("Given valid register information, register user")
//    void valid_register() throws JsonProcessingException {
//        final String asString = objectMapper.writeValueAsString(
//                objectMapper.convertValue(USER_REGISTER, UserDetails.class)
//                        .toBuilder()
//                        .id(1)
//                        .roles(Collections.emptySet())
//                        .password(null)
//                        .build()
//        );
//
//        final MockResponse mockResponse = new MockResponse();
//        mockResponse
//                .setHeader("Content-Type", "application/json")
//                .setBody(asString);
//
//        server.enqueue(mockResponse);
//
//        final UserDetails block = authServiceClient.createUser(USER_REGISTER).block();
//
//        assertEquals(USER_REGISTER.getEmail(), block.getEmail());
//        assertEquals(USER_REGISTER.getUsername(), block.getUsername());
//        assertNull(block.getPassword());
//        assertNotNull(block.getId());
//        assertEquals(0, block.getRoles().size());
//    }
//
//    @Test
//    @DisplayName("Given valid JWT, verify user")
//    void valid_verification() throws JsonProcessingException {
//        final String asString = objectMapper.writeValueAsString(
//                objectMapper.convertValue(USER_REGISTER, UserDetails.class)
//                        .toBuilder()
//                        .id(1)
//                        .roles(Collections.emptySet())
//                        .password(null)
//                        .build()
//        );
//        final String token = "some.valid.token";
//
//        final MockResponse mockResponse = new MockResponse();
//        mockResponse
//                .setHeader("Content-Type", "application/json")
//                .setBody(asString);
//
//        server.enqueue(mockResponse);
//
//        final UserDetails block = authServiceClient.verifyUser(token).block();
//
//        assertEquals(USER_REGISTER.getEmail(), block.getEmail());
//        assertEquals(USER_REGISTER.getUsername(), block.getUsername());
//        assertNull(block.getPassword());
//        assertNotNull(block.getId());
//        assertEquals(0, block.getRoles().size());
//    }
//
////    @Test
//    @DisplayName("Given valid Login, return JWT")
//    void valid_login() throws Exception {
//
//        final UserDetails userDetails = objectMapper.convertValue(USER_REGISTER, UserDetails.class)
//                .toBuilder()
//                .id(1)
//                .roles(Collections.emptySet())
//                .password(null)
//                .build();
//
//        final String asString = objectMapper.writeValueAsString(userDetails);
//
//        final Login login = Login.builder()
//                .email(USER_REGISTER.getEmail())
//                .password(USER_REGISTER.getPassword())
//                .build();
//        final String token = "some.valid.token";
//
//        final MockResponse mockResponse = new MockResponse();
//        mockResponse
//                .setHeader("Content-Type", "application/json")
//                .setHeader("Authorization", token)
//                .setBody(asString);
//
//        server.enqueue(mockResponse);
//
//        HttpEntity<UserPasswordLessDTO> response = authServiceClient.login(login);
//
//        assertEquals(USER_REGISTER.getEmail(), block.getT2().getEmail());
//        assertEquals(USER_REGISTER.getUsername(), block.getT2().getUsername());
//        assertNull(block.getT2().getPassword());
//        assertNotNull(block.getT2().getId());
//        assertEquals(0, block.getT2().getRoles().size());
//        assertEquals(token, block.getT1());
//    }

//    @Test
//    @DisplayName("Given invalid Login, throw 401")
//    void invalid_login() throws JsonProcessingException {
//
//        final String errorMessage = "Unauthorized";
//        final String asString = objectMapper.writeValueAsString(new HttpErrorInfo(UNAUTHORIZED.value(), errorMessage));
//
//        final MockResponse mockResponse = new MockResponse();
//        mockResponse
//                .setHeader("Content-Type", "application/json")
//                .setBody(asString)
//                .status("HTTP/1.1 401 Unauthorized");
//
//        server.enqueue(mockResponse);
//
//        final GenericHttpException ex = assertThrows(GenericHttpException.class, () -> authServiceClient.login(new Login()).block());
//
//        assertEquals(UNAUTHORIZED, ex.getHttpStatus());
//        assertEquals(errorMessage, ex.getMessage());
//    }


    @Test
    @DisplayName("Should validate a token")
    void ShouldValidateToken_ShouldReturnOk(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        final Mono<Void> validatedTokenResponse = authServiceClient.validateToken("token").then();

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(0)
                .verifyComplete();




    }


    @Test
    @DisplayName("Should try to validate a token and fail")
    void ShouldValidateToken_ShouldReturnUnauthorized(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401);

        server.enqueue(mockResponse);

        final Mono<Void> validatedTokenResponse = authServiceClient.validateToken("inavlidToken").then();

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(0)
                .verifyError();
    }



    @Test
    @DisplayName("Should login a user")
    void ShouldLoginUser_ShouldReturnOk() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        Login login = Login.builder()
                .email("email")
                .password("password")
                .build();

        final Mono<HttpEntity<UserPasswordLessDTO>> validatedTokenResponse = authServiceClient.login(login);

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(1)
                .verifyComplete();
        }
}
