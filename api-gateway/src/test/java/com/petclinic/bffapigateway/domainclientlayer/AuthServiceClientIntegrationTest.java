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
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Rethrower;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    @MockBean
    CartServiceClient cartServiceClient;

    private MockWebServer server;
    private ObjectMapper objectMapper;

    @MockBean
    private Utility utility;

    @MockBean
    private Rethrower rethrower;

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
    void setup() throws Exception {

        customersServiceClient = Mockito.mock(CustomersServiceClient.class);
        vetsServiceClient = Mockito.mock(VetsServiceClient.class);
        cartServiceClient = Mockito.mock(CartServiceClient.class);
        rethrower = Mockito.mock(Rethrower.class);
        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                customersServiceClient, vetsServiceClient, server.getHostName(),
                String.valueOf(server.getPort()), cartServiceClient);
        objectMapper = new ObjectMapper();

        ReflectionTestUtils.setField(authServiceClient, "rethrower", rethrower);

        Mockito.reset(customersServiceClient, vetsServiceClient, cartServiceClient, rethrower);
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

    @Test
    @DisplayName("Should validate a token")
    void ShouldValidateToken_ShouldReturnOk(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        final Mono<Void> validatedTokenResponse = authServiceClient.validateToken("token").then();

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
                .emailOrUsername("email")
                .password("password")
                .build();

        final Mono<ResponseEntity<UserPasswordLessDTO>> validatedTokenResponse = authServiceClient.login(Mono.just(login));

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
                .emailOrUsername("email")
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

        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return user details when valid userId is provided")
    void shouldReturnUserDetails_WhenValidUserIdIsProvided() throws IOException {
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

        Mono<UserDetails> result = authServiceClient.getUserById(jwtToken, userId);

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

        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(0)
                .verifyError();
    }

    @Test
    @DisplayName("Should create a vet user")
    void shouldCreateVetUser() throws IOException {
        RegisterVet registerVet = RegisterVet.builder()
                .username("username")
                .password("password")
                .email("email")
                .build();
        Mono<RegisterVet> registerVetMono = Mono.just(registerVet);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);
        server.enqueue(mockResponse);

        Mono<VetResponseDTO> result = authServiceClient.createVetUser(registerVetMono);

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

    @Test
    @DisplayName("Should return error when deleting a user with invalid token")
    void deleteUser_ShouldReturnError() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(401);

        server.enqueue(mockResponse);

        String jwtToken = "invalidJwtToken";
        String userId = "userId";

        final Mono<Void> deleteUserResponse = authServiceClient.deleteUser(jwtToken, userId);

        StepVerifier.create(deleteUserResponse)
                .expectErrorMatches(throwable -> throwable instanceof GenericHttpException &&
                        ((GenericHttpException) throwable).getHttpStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    @DisplayName("Should get all roles")
    void shouldGetAllRoles() throws Exception {
        Role role1 = Role.builder().name("OWNER").build();
        Role role2 = Role.builder().name("ADMIN").build();


        String rolesJson = new ObjectMapper().writeValueAsString(List.of(role1, role2));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(rolesJson);

        server.enqueue(mockResponse);

        Flux<Role> rolesFlux = authServiceClient.getAllRoles("jwtToken");

        StepVerifier.create(rolesFlux)
                .expectNext(role1)
                .expectNext(role2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create a role")
    void shouldCreateRole() throws Exception {

        RoleRequestModel roleRequestModel = RoleRequestModel.builder().name("SUPPORT").build();
        Role role = Role.builder().name("SUPPORT").build();

        String roleJson = new ObjectMapper().writeValueAsString(role);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody(roleJson);

        server.enqueue(mockResponse);

        Mono<Role> roleMono = authServiceClient.createRole("jwtToken", roleRequestModel);

        StepVerifier.create(roleMono)
                .expectNext(role)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update a role")
    void shouldUpdateRole() throws Exception {
        Integer roleId = 1;
        RoleRequestModel roleRequestModel = RoleRequestModel.builder().name("ROLE_MANAGER").build();
        Role updatedRole = Role.builder().id(roleId).name("ROLE_MANAGER").build();

        String roleJson = new ObjectMapper().writeValueAsString(updatedRole);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(roleJson);

        server.enqueue(mockResponse);
        Mono<Role> roleMono = authServiceClient.updateRole("jwtToken", Long.valueOf(roleId), roleRequestModel);

        StepVerifier.create(roleMono)
                .expectNextMatches(role -> role.getName().equals("ROLE_MANAGER"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get a role by ID")
    void shouldGetRoleById() throws Exception {
        Integer roleId = 1;
        Role role = Role.builder().id(roleId).name("ROLE_ADMIN").build();

        String roleJson = new ObjectMapper().writeValueAsString(role);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setBody(roleJson);

        server.enqueue(mockResponse);

        Mono<Role> roleMono = authServiceClient.getRoleById("jwtToken", Long.valueOf(roleId));

        StepVerifier.create(roleMono)
                .expectNextMatches(r -> r.getName().equals("ROLE_ADMIN"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        UserPasswordLessDTO updateRequest = UserPasswordLessDTO.builder()
                .userId(userId)
                .email("updated@example.com")
                .username("updateduser")
                .build();

        UserPasswordLessDTO updatedUser = UserPasswordLessDTO.builder()
                .userId(userId)
                .email("updated@example.com")
                .username("updateduser")
                .build();

        String userJson = new ObjectMapper().writeValueAsString(updatedUser);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        Mono<UserPasswordLessDTO> result = authServiceClient.updateUser(jwtToken, updateRequest, userId);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getUserId().equals(userId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update username successfully")
    void shouldUpdateUsername() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        String newUsername = "newusername";

        UserPasswordLessDTO updatedUser = UserPasswordLessDTO.builder()
                .userId(userId)
                .email("user@example.com")
                .username(newUsername)
                .build();

        String userJson = new ObjectMapper().writeValueAsString(updatedUser);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody("Username updated successfully");

        server.enqueue(mockResponse);

        Mono<String> result = authServiceClient.updateUsername(userId, newUsername, jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.contains("Username updated successfully"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check username availability - available")
    void shouldCheckUsernameAvailability_Available() throws Exception {
        String username = "availableuser";
        String jwtToken = "valid-jwt-token";

        UserDetails existingUser = UserDetails.builder()
                .username("existinguser")
                .userId("user123")
                .email("existing@example.com")
                .build();

        String usersJson = new ObjectMapper().writeValueAsString(List.of(existingUser));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(usersJson);

        server.enqueue(mockResponse);

        Mono<Boolean> result = authServiceClient.checkUsernameAvailability(username, jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(available -> available == true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check username availability - not available")
    void shouldCheckUsernameAvailability_NotAvailable() throws Exception {
        String username = "existinguser";
        String jwtToken = "valid-jwt-token";

        UserDetails existingUser = UserDetails.builder()
                .username("existinguser")
                .userId("user123")
                .email("existing@example.com")
                .build();

        String usersJson = new ObjectMapper().writeValueAsString(List.of(existingUser));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(usersJson);

        server.enqueue(mockResponse);

        Mono<Boolean> result = authServiceClient.checkUsernameAvailability(username, jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(available -> available == false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should add vet user successfully")
    void shouldAddVetUser() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("vet123")
                .email("vet@example.com")
                .username("testvet")
                .build();

        VetResponseDTO vetResponse = VetResponseDTO.builder()
                .vetId("vet123")
                .firstName("John")
                .lastName("Doe")
                .email("vet@example.com")
                .active(true)
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(vetsServiceClient.addVet(any(Mono.class))).thenReturn(Mono.just(vetResponse));
        when(vetsServiceClient.deleteVet(any())).thenReturn(Mono.empty());

        Mono<VetResponseDTO> result = authServiceClient.addVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .expectNextMatches(vet -> vet.getVetId().equals("vet123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create user with successful owner creation")
    void shouldCreateUserWithOwnerCreation() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("user123")
                .email("user@example.com")
                .username("testuser")
                .build();

        OwnerResponseDTO ownerResponse = OwnerResponseDTO.builder()
                .ownerId("user123")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .city("Anytown")
                .telephone("555-1234")
                .pets(List.of())
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(customersServiceClient.createOwner(any(OwnerRequestDTO.class))).thenReturn(Mono.just(ownerResponse));
        when(customersServiceClient.deleteOwner(any())).thenReturn(Mono.empty());

        Mono<OwnerResponseDTO> result = authServiceClient.createUser(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .expectNextMatches(owner -> owner.getOwnerId().equals("user123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create user using V2 endpoint with cart assignment")
    void shouldCreateUserUsingV2EndpointWithCartAssignment() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("user123")
                .email("user@example.com")
                .username("testuser")
                .build();

        OwnerResponseDTO ownerResponse = OwnerResponseDTO.builder()
                .ownerId("user123")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .city("Anytown")
                .telephone("555-1234")
                .pets(List.of())
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(customersServiceClient.addOwner(any(Mono.class))).thenReturn(Mono.just(ownerResponse));
        when(customersServiceClient.deleteOwner(any())).thenReturn(Mono.empty());
        when(cartServiceClient.assignCartToUser(any())).thenReturn(Mono.empty());

        Mono<OwnerResponseDTO> result = authServiceClient.createUserUsingV2Endpoint(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .expectNextMatches(owner -> owner.getOwnerId().equals("user123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create vet user with successful vet creation")
    void shouldCreateVetUserWithVetCreation() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("vet123")
                .email("vet@example.com")
                .username("testvet")
                .build();

        VetResponseDTO vetResponse = VetResponseDTO.builder()
                .vetId("vet123")
                .firstName("John")
                .lastName("Doe")
                .email("vet@example.com")
                .active(true)
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(vetsServiceClient.createVet(any(Mono.class))).thenReturn(Mono.just(vetResponse));
        when(vetsServiceClient.deleteVet(any())).thenReturn(Mono.empty());

        Mono<VetResponseDTO> result = authServiceClient.createVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .expectNextMatches(vet -> vet.getVetId().equals("vet123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle 4xx error in getUserById")
    void shouldHandle4xxErrorInGetUserById() throws Exception {
        String userId = "invalid-user";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\": \"User not found\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("User not found", HttpStatus.NOT_FOUND));

        Mono<UserDetails> result = authServiceClient.getUserById(jwtToken, userId);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createUser")
    void shouldHandle4xxErrorInCreateUser() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid registration data", HttpStatus.BAD_REQUEST));

        Mono<OwnerResponseDTO> result = authServiceClient.createUser(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle error cleanup in createUser")
    void shouldHandleErrorCleanupInCreateUser() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("user123")
                .email("user@example.com")
                .username("testuser")
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(customersServiceClient.createOwner(any(OwnerRequestDTO.class))).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(customersServiceClient.deleteOwner(any())).thenReturn(Mono.empty());

        Mono<OwnerResponseDTO> result = authServiceClient.createUser(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle error cleanup in createVetUser")
    void shouldHandleErrorCleanupInCreateVetUser() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("vet123")
                .email("vet@example.com")
                .username("testvet")
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(vetsServiceClient.createVet(any(Mono.class))).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(vetsServiceClient.deleteVet(any())).thenReturn(Mono.empty());

        Mono<VetResponseDTO> result = authServiceClient.createVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle error cleanup in addVetUser")
    void shouldHandleErrorCleanupInAddVetUser() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("vet123")
                .email("vet@example.com")
                .username("testvet")
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(vetsServiceClient.addVet(any(Mono.class))).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(vetsServiceClient.deleteVet(any())).thenReturn(Mono.empty());

        Mono<VetResponseDTO> result = authServiceClient.addVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle error cleanup in createUserUsingV2Endpoint")
    void shouldHandleErrorCleanupInCreateUserUsingV2Endpoint() throws Exception {
        UserPasswordLessDTO userResponse = UserPasswordLessDTO.builder()
                .userId("user123")
                .email("user@example.com")
                .username("testuser")
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        when(customersServiceClient.addOwner(any(Mono.class))).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(customersServiceClient.deleteOwner(any())).thenReturn(Mono.empty());

        Mono<OwnerResponseDTO> result = authServiceClient.createUserUsingV2Endpoint(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in changePassword")
    void shouldHandle5xxErrorInChangePassword() throws Exception {
        UserPasswordAndTokenRequestModel changePasswordRequest = UserPasswordAndTokenRequestModel.builder()
                .token("token")
                .password("newpass")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<ResponseEntity<Void>> result = authServiceClient.changePassword(Mono.just(changePasswordRequest));

        StepVerifier.create(result)
                .verifyError(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in deleteUser")
    void shouldHandle5xxErrorInDeleteUser() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<Void> result = authServiceClient.deleteUser(jwtToken, userId);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in validateToken")
    void shouldHandle5xxErrorInValidateToken() throws Exception {
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<ResponseEntity<TokenResponseDTO>> result = authServiceClient.validateToken(jwtToken);

        StepVerifier.create(result)
                .verifyError(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in sendForgottenEmail")
    void shouldHandle5xxErrorInSendForgottenEmail() throws Exception {
        UserEmailRequestDTO forgotPasswordRequest = UserEmailRequestDTO.builder()
                .email("user@example.com")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<ResponseEntity<Void>> result = authServiceClient.sendForgottenEmail(Mono.just(forgotPasswordRequest));

        StepVerifier.create(result)
                .verifyError(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in login")
    void shouldHandle5xxErrorInLogin() throws Exception {
        Login loginRequest = Login.builder()
                .emailOrUsername("testuser@example.com")
                .password("password")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<ResponseEntity<UserPasswordLessDTO>> result = authServiceClient.login(Mono.just(loginRequest));

        StepVerifier.create(result)
                .verifyError(InvalidInputException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in enableUser")
    void shouldHandle5xxErrorInEnableUser() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<Void> result = authServiceClient.enableUser(userId, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 5xx error in disableUser")
    void shouldHandle5xxErrorInDisableUser() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}");

        server.enqueue(mockResponse);


        Mono<Void> result = authServiceClient.disableUser(userId, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should update user roles successfully")
    void shouldUpdateUsersRoles() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        RolesChangeRequestDTO rolesRequest = RolesChangeRequestDTO.builder()
                .roles(List.of("ADMIN", "USER"))
                .build();

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(123L)
                .email("user@example.com")
                .username("testuser")
                .roles(Set.of(new Role(1, "ADMIN"), new Role(2, "USER")))
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userResponse);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        Mono<UserResponseDTO> result = authServiceClient.updateUsersRoles(userId, rolesRequest, jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId() == 123L && user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUsersRoles")
    void shouldHandle4xxErrorInUpdateUsersRoles() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        RolesChangeRequestDTO rolesRequest = RolesChangeRequestDTO.builder()
                .roles(List.of("ADMIN", "USER"))
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid roles\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid roles", HttpStatus.BAD_REQUEST));

        Mono<UserResponseDTO> result = authServiceClient.updateUsersRoles(userId, rolesRequest, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should verify user using V2 endpoint successfully")
    void shouldVerifyUserUsingV2Endpoint() throws Exception {
        String jwtToken = "valid-jwt-token";

        UserDetails userDetails = UserDetails.builder()
                .userId("user123")
                .email("user@example.com")
                .username("testuser")
                .roles(Set.of(new Role(1, "USER")))
                .build();

        String userJson = new ObjectMapper().writeValueAsString(userDetails);

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(userJson);

        server.enqueue(mockResponse);

        Mono<ResponseEntity<UserDetails>> result = authServiceClient.verifyUserUsingV2Endpoint(jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getBody() != null && response.getBody().getEmail().equals("user@example.com"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle 4xx error in verifyUserUsingV2Endpoint")
    void shouldHandle4xxErrorInVerifyUserUsingV2Endpoint() throws Exception {
        String jwtToken = "invalid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\": \"Invalid token\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid token", HttpStatus.UNAUTHORIZED));

        Mono<ResponseEntity<UserDetails>> result = authServiceClient.verifyUserUsingV2Endpoint(jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsers() throws Exception {
        String jwtToken = "valid-jwt-token";

        UserDetails user1 = UserDetails.builder()
                .userId("user1")
                .email("user1@example.com")
                .username("user1")
                .roles(Set.of(new Role(1, "USER")))
                .build();

        UserDetails user2 = UserDetails.builder()
                .userId("user2")
                .email("user2@example.com")
                .username("user2")
                .roles(Set.of(new Role(1, "ADMIN")))
                .build();

        String usersJson = new ObjectMapper().writeValueAsString(List.of(user1, user2));

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(usersJson);

        server.enqueue(mockResponse);

        Flux<UserDetails> result = authServiceClient.getAllUsers(jwtToken);

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getEmail().equals("user1@example.com"))
                .expectNextMatches(user -> user.getEmail().equals("user2@example.com"))
                .verifyComplete();
    }


    @Test
    @DisplayName("Should handle 4xx error in createUserUsingV2Endpoint")
    void shouldHandle4xxErrorInCreateUserUsingV2Endpoint() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid registration data", HttpStatus.BAD_REQUEST));

        Mono<OwnerResponseDTO> result = authServiceClient.createUserUsingV2Endpoint(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createVetUser")
    void shouldHandle4xxErrorInCreateVetUser() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid vet registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid vet registration data", HttpStatus.BAD_REQUEST));

        Mono<VetResponseDTO> result = authServiceClient.createVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in addVetUser")
    void shouldHandle4xxErrorInAddVetUser() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid vet registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid vet registration data", HttpStatus.BAD_REQUEST));

        Mono<VetResponseDTO> result = authServiceClient.addVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createInventoryManagerUser")
    void shouldHandle4xxErrorInCreateInventoryManagerUser() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid inventory manager registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid inventory manager registration data", HttpStatus.BAD_REQUEST));

        Mono<UserPasswordLessDTO> result = authServiceClient.createInventoryMangerUser(Mono.just(REGISTER_INVENTORY_MANAGER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUser")
    void shouldHandle4xxErrorInUpdateUser() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        UserPasswordLessDTO updateRequest = UserPasswordLessDTO.builder()
                .userId(userId)
                .email("updated@example.com")
                .username("updateduser")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid update data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid update data", HttpStatus.BAD_REQUEST));

        Mono<UserPasswordLessDTO> result = authServiceClient.updateUser(jwtToken, updateRequest, userId);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUsername")
    void shouldHandle4xxErrorInUpdateUsername() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        String newUsername = "newusername";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Username already exists\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Username already exists", HttpStatus.BAD_REQUEST));

        Mono<String> result = authServiceClient.updateUsername(userId, newUsername, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in checkUsernameAvailability")
    void shouldHandle4xxErrorInCheckUsernameAvailability() throws Exception {
        String username = "testuser";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(403)
                .setBody("{\"message\": \"Access denied\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Access denied", HttpStatus.FORBIDDEN));

        Mono<Boolean> result = authServiceClient.checkUsernameAvailability(username, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in changePassword")
    void shouldHandle4xxErrorInChangePassword() throws Exception {
        UserPasswordAndTokenRequestModel changePasswordRequest = UserPasswordAndTokenRequestModel.builder()
                .token("token")
                .password("newpass")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid password\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid password", HttpStatus.BAD_REQUEST));

        Mono<ResponseEntity<Void>> result = authServiceClient.changePassword(Mono.just(changePasswordRequest));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in verifyUser")
    void shouldHandle4xxErrorInVerifyUser() throws Exception {
        String jwtToken = "invalid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\": \"Invalid token\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenThrow(new GenericHttpException("Invalid token", HttpStatus.UNAUTHORIZED));

        Mono<ResponseEntity<UserDetails>> result = authServiceClient.verifyUser(jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in getUserById with proper lambda execution")
    void shouldHandle4xxErrorInGetUserByIdWithLambdaExecution() throws Exception {
        String userId = "invalid-user";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\": \"User not found\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "User not found");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<UserDetails> result = authServiceClient.getUserById(jwtToken, userId);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createUser with proper lambda execution")
    void shouldHandle4xxErrorInCreateUserWithLambdaExecution() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid registration data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<OwnerResponseDTO> result = authServiceClient.createUser(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createUserUsingV2Endpoint with proper lambda execution")
    void shouldHandle4xxErrorInCreateUserUsingV2EndpointWithLambdaExecution() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid registration data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<OwnerResponseDTO> result = authServiceClient.createUserUsingV2Endpoint(Mono.just(USER_REGISTER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createVetUser with proper lambda execution")
    void shouldHandle4xxErrorInCreateVetUserWithLambdaExecution() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid vet registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid vet registration data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<VetResponseDTO> result = authServiceClient.createVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in addVetUser with proper lambda execution")
    void shouldHandle4xxErrorInAddVetUserWithLambdaExecution() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid vet registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid vet registration data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<VetResponseDTO> result = authServiceClient.addVetUser(Mono.just(REGISTER_VETERINARIAN));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in createInventoryManagerUser with proper lambda execution")
    void shouldHandle4xxErrorInCreateInventoryManagerUserWithLambdaExecution() throws Exception {
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid inventory manager registration data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid inventory manager registration data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<UserPasswordLessDTO> result = authServiceClient.createInventoryMangerUser(Mono.just(REGISTER_INVENTORY_MANAGER));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUser with proper lambda execution")
    void shouldHandle4xxErrorInUpdateUserWithLambdaExecution() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        UserPasswordLessDTO updateRequest = UserPasswordLessDTO.builder()
                .userId(userId)
                .email("updated@example.com")
                .username("updateduser")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid update data\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid update data");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<UserPasswordLessDTO> result = authServiceClient.updateUser(jwtToken, updateRequest, userId);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUsername with proper lambda execution")
    void shouldHandle4xxErrorInUpdateUsernameWithLambdaExecution() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        String newUsername = "newusername";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Username already exists\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Username already exists");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<String> result = authServiceClient.updateUsername(userId, newUsername, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in checkUsernameAvailability with proper lambda execution")
    void shouldHandle4xxErrorInCheckUsernameAvailabilityWithLambdaExecution() throws Exception {
        String username = "testuser";
        String jwtToken = "valid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(403)
                .setBody("{\"message\": \"Access denied\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Access denied");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<Boolean> result = authServiceClient.checkUsernameAvailability(username, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in changePassword with proper lambda execution")
    void shouldHandle4xxErrorInChangePasswordWithLambdaExecution() throws Exception {
        UserPasswordAndTokenRequestModel changePasswordRequest = UserPasswordAndTokenRequestModel.builder()
                .token("token")
                .password("newpass")
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid password\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid password");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<ResponseEntity<Void>> result = authServiceClient.changePassword(Mono.just(changePasswordRequest));

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in verifyUser with proper lambda execution")
    void shouldHandle4xxErrorInVerifyUserWithLambdaExecution() throws Exception {
        String jwtToken = "invalid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\": \"Invalid token\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid token");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<ResponseEntity<UserDetails>> result = authServiceClient.verifyUser(jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in verifyUserUsingV2Endpoint with proper lambda execution")
    void shouldHandle4xxErrorInVerifyUserUsingV2EndpointWithLambdaExecution() throws Exception {
        String jwtToken = "invalid-jwt-token";

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\": \"Invalid token\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid token");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<ResponseEntity<UserDetails>> result = authServiceClient.verifyUserUsingV2Endpoint(jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }

    @Test
    @DisplayName("Should handle 4xx error in updateUsersRoles with proper lambda execution")
    void shouldHandle4xxErrorInUpdateUsersRolesWithLambdaExecution() throws Exception {
        String userId = "user123";
        String jwtToken = "valid-jwt-token";
        RolesChangeRequestDTO rolesRequest = RolesChangeRequestDTO.builder()
                .roles(List.of("ADMIN", "USER"))
                .build();

        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid roles\"}");

        server.enqueue(mockResponse);

        when(rethrower.rethrow(any(ClientResponse.class), any())).thenAnswer(invocation -> {
            ClientResponse response = invocation.getArgument(0);
            Function<Map, ? extends Throwable> lambda = invocation.getArgument(1);
            Map<String, Object> errorMap = Map.of("message", "Invalid roles");
            return Mono.error(lambda.apply(errorMap));
        });

        Mono<UserResponseDTO> result = authServiceClient.updateUsersRoles(userId, rolesRequest, jwtToken);

        StepVerifier.create(result)
                .verifyError(GenericHttpException.class);
    }
}