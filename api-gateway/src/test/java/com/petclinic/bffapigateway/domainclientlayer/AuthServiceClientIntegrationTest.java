package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.Workday;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    @MockBean
    VetsServiceClient vetsServiceClient;
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

    private final RegisterInventoryManager REGISTER_INVENTORY_MANAGER = RegisterInventoryManager.builder()
            .username("username")
            .password("password")
            .email("email")
            .build();
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


    private final RegisterVet REGISTER_VETERINARIAN = RegisterVet.builder()
            .username("username")
            .password("password")
            .email("email")
            .vet(VetRequestDTO.builder()
                    .vetId("UUID")
                    .firstName("firstName")
                    .lastName("lastName")
                    .active(true)
                    .vetBillId("UUID")
                    .phoneNumber("phoneNumber")
                    .workday(Set.of(Workday.Friday, Workday.Monday, Workday.Thursday, Workday.Tuesday, Workday.Wednesday))
                    .resume("resume")
                    .build())
            .build();

    @BeforeEach
    void setup() {

        customersServiceClient = Mockito.mock(CustomersServiceClient.class);
        vetsServiceClient = Mockito.mock(VetsServiceClient.class);
        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                customersServiceClient, vetsServiceClient, server.getHostName(),
                String.valueOf(server.getPort()));
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("Given valid register information, register inventory manager")
    void valid_register_inventory_manager(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        Mono<UserPasswordLessDTO> block = authServiceClient.createInventoryMangerUser(Mono.just(REGISTER_INVENTORY_MANAGER));

        StepVerifier
                .create(block)
                .verifyComplete();

    }


    @Test
    @DisplayName("Given invalid register information, register inventory manager")
    void invalid_register_inventory_manager(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400);

        server.enqueue(mockResponse);

        Mono<UserPasswordLessDTO> block = authServiceClient.createInventoryMangerUser(Mono.just(REGISTER_INVENTORY_MANAGER));

        StepVerifier
                .create(block)
                .verifyError();
    }


    @Test
    @DisplayName("Given Valid Vet Register, register vet")
    void valid_register_vet(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        Mono<VetResponseDTO> block = authServiceClient.createVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier
                .create(block)
                .verifyComplete();

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
    @DisplayName("Should logout a user")
    void shouldLogoutUser_shouldReturnNoContent() throws Exception {
        final MockResponse loginMockResponse = new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);
        server.enqueue(loginMockResponse);
        ServerHttpRequest loginRequest = MockServerHttpRequest.post("/users/login").build();
        Login login = Login.builder()
                .email("email")
                .password("password")
                .build();
        final Mono<ResponseEntity<UserPasswordLessDTO>> validatedTokenResponse = authServiceClient.login(Mono.just(login));
        ServerHttpRequest logoutRequest = MockServerHttpRequest.post("/users/logout")
                .cookie(new HttpCookie("Bearer", "some_valid_token"))
                .build();
        MockServerHttpResponse logoutMockResponse = new MockServerHttpResponse();

        final Mono<ResponseEntity<Void>> logoutResponse = authServiceClient.logout(logoutRequest, logoutMockResponse);

        StepVerifier.create(logoutResponse)
                .consumeNextWith(responseEntity -> {
                    // Verify the HTTP status code directly
                    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
                })
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
    @DisplayName("Given valid username, get user details")
    void valid_getUsersByUsername() throws Exception {
        UserDetails user1 = UserDetails.builder()
                .username("user1")
                .userId("userId1")
                .email("email1")
                .build();

        UserDetails user2 = UserDetails.builder()
                .username("user2")
                .userId("userId2")
                .email("email2")
                .build();

        String userDetailsJson = new ObjectMapper().writeValueAsString(List.of(user1, user2));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userDetailsJson);

        server.enqueue(mockResponse);

        Flux<UserDetails> userDetailsFlux = authServiceClient.getUsersByUsername("jwtToken", "usernames");

        StepVerifier.create(userDetailsFlux)
                .expectNext(user1)
                .expectNext(user2)
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

    @Test
    @DisplayName("Should create a vet user")
    void shouldCreateVetUser() throws IOException {
        // Arrange
        RegisterVet registerVet = RegisterVet.builder()
                .username("username")
                .password("password")
                .email("email")
                .build();
        Mono<RegisterVet> registerVetMono = Mono.just(registerVet);

        // Set up the MockWebServer to return a specific response
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);
        server.enqueue(mockResponse);

        // Act
        Mono<VetResponseDTO> result = authServiceClient.createVetUser(registerVetMono);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void deleteUser_ShouldReturnOk() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setResponseCode(200);

        server.enqueue(mockResponse);

        String jwtToken = "jwtToken";
        String userId = "userId";

        final Mono<Void> validatedTokenResponse = authServiceClient.deleteUser(jwtToken, userId);

        // check status response in step verifier
        StepVerifier.create(Mono.just(validatedTokenResponse))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void disableUser_ShouldReturnOk() throws IOException {

        final String userId = "valid-user-id";
        final String token = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(200);
        server.enqueue(mockResponse);

        final Mono<Void> response = authServiceClient.disableUser(token, userId);

        StepVerifier.create(response)
                .verifyComplete();
    }

    @Test
    void disableNonExistentUser_ShouldReturnError() throws IOException {

        final String userId = "non-existent-user-id";
        final String token = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(404);
        server.enqueue(mockResponse);

        final Mono<Void> response = authServiceClient.disableUser(userId, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof GenericHttpException &&
                        ((GenericHttpException) throwable).getHttpStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void enableUser_ShouldReturnOk() throws IOException {

        final String userId = "valid-user-id";
        final String token = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(200);
        server.enqueue(mockResponse);

        final Mono<Void> response = authServiceClient.enableUser(token, userId);

        StepVerifier.create(response)
                .verifyComplete();
    }
    @Test
    void enableNonExistentUser_ShouldReturnError() throws IOException {

        final String userId = "non-existent-user-id";
        final String token = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(404);
        server.enqueue(mockResponse);

        final Mono<Void> response = authServiceClient.enableUser(userId, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof GenericHttpException &&
                        ((GenericHttpException) throwable).getHttpStatus() == HttpStatus.BAD_REQUEST) // match against your custom exception
                .verify();
    }

}
