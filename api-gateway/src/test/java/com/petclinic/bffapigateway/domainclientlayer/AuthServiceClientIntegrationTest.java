package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.internal.duplex.DuplexResponseBody;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;

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
            ,"Bearer");

    @MockBean
    CustomersServiceClient customersServiceClient;
    private MockWebServer server;
    private ObjectMapper objectMapper;

    @MockBean
    private Utility utility;

    UserDetails userDetails = UserDetails.builder()
            .username("username")
            .userId("userId")
            .email("email")
            .build();

    private AuthServiceClient authServiceClient;

    private final Register USER_REGISTER = Register.builder()
            .username("username")
            .password("password")
            .email("email")
            .owner(OwnerRequestDTO.builder()
                    .ownerId("UUID")
                    .firstName("firstName")
                    .lastName("lastName")
                    .address("address")
                    .city("city")
                    .telephone("telephone")
                    .build())
            .build();

    @BeforeEach
    void setup() {

        customersServiceClient = Mockito.mock(CustomersServiceClient.class);

        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                customersServiceClient, server.getHostName(),
                String.valueOf(server.getPort()));
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("Given valid register information, register user")
    void valid_register(){
        OwnerResponseDTO ownerResponseDTO = OwnerResponseDTO.builder()
                        .ownerId("UUID")
                        .firstName("firstName")
                        .lastName("lastName")
                        .address("address")
                        .city("city")
                        .telephone("telephone")
                        .pets(List.of())
                        .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        Mockito.when(customersServiceClient.createOwner(any()))
                .thenReturn(Mono.just(ownerResponseDTO));


        Mono<OwnerResponseDTO> block = authServiceClient.createUser(Mono.just(USER_REGISTER));

        StepVerifier
                .create(block)
                .verifyComplete();

    }

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

        final Mono<ResponseEntity<UserPasswordLessDTO>> validatedTokenResponse = authServiceClient.login(Mono.just(login));

        // check status response in step verifier
        StepVerifier.create(Mono.just(validatedTokenResponse))
                .expectNextCount(1)
                .verifyComplete();
        }



    @Test
    @DisplayName("Should send a forgotten email")
    void ShouldSendForgottenEmail_ShouldReturnOk() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        UserEmailRequestDTO emailRequestDTO = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        final Mono<ResponseEntity<Void>> validatedTokenResponse = authServiceClient.sendForgottenEmail(Mono.just(emailRequestDTO));

        // check status response in step verifier
        StepVerifier.create(Mono.just(validatedTokenResponse))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should process reset password")
    void ShouldProcessResetPassword_ShouldReturnOk() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        UserPasswordAndTokenRequestModel pwdChange = UserPasswordAndTokenRequestModel.builder()
                .token("token")
                .password("password")
                .build();

        final Mono<ResponseEntity<Void>> validatedTokenResponse = authServiceClient.changePassword(Mono.just(pwdChange));

        // check status response in step verifier
        StepVerifier.create(Mono.just(validatedTokenResponse))
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    void getAllUser_ShouldReturn2() throws JsonProcessingException {
        UserDetails user1 = UserDetails.builder()
                .username("user1")
                .userId("jkbjbhjbllb")
                .email("email1")
                .build();

        UserDetails user2 = UserDetails.builder()
                .username("user2")
                .email("email2")
                .userId("hhvhvhvhuvul")
                .build();
        String validToken = "jvhgvgvgjkvgjvgj";
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setBody(objectMapper.writeValueAsString(List.of(user1,user2)))
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        final Flux<UserDetails> validatedTokenResponse = authServiceClient.getUsers(validToken);

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verifyUser a token")
    void ShouldVerifyUserToken_ShouldReturnOk(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        final Mono<Void> verifyUser = authServiceClient.verifyUser("token").then();

        // check status response in step verifier
        StepVerifier.create(verifyUser)
                .expectNextCount(0)
                .verifyComplete();


    }

    @Test
    @DisplayName("Should try to validate a token and fail")
    void ShouldVerifyUserToken_ShouldReturnInvalid(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401);

        server.enqueue(mockResponse);

        final Mono<Void> validatedTokenResponse = authServiceClient.verifyUser("invalidToken").then();

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(0)
                .verifyError();
    }
}
