package com.petclinic.bffapigateway.presentationlayer.v1.Users;

import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.presentationlayer.v1.UserControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {
                UserControllerV1.class,
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class}
        )
)
@AutoConfigureWebTestClient
class UserControllerV1UnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private AuthServiceClient authServiceClient;

    private final String TOKEN = "test-token";

    private final String EXISTING_USER_ID_1 = "be08dfa1-25d4-4352-92df-5dc37c2464c7";
    private final String EXISTING_USER_ID_2 = "be08dfa1-25d4-4352-92df-5dc37c2464c7";
    private final String NON_EXISTING_USER_ID = "be08dfa1-25d4-4352-92df-5dc37c246md8";

    private final String UPDATED_USERNAME = "updatedTestUser";

    private final UserDetails USER_DETAILS_1 = UserDetails.builder()
            .userId(EXISTING_USER_ID_1)
            .username("testUser")
            .email("test@example.com")
            .build();

    private final UserDetails USER_DETAILS_2 = UserDetails.builder()
            .userId(EXISTING_USER_ID_2)
            .username("secondTestUser")
            .email("test2@example.com")
            .build();

    private final OwnerRequestDTO OWNER_REQUEST_MODEL = OwnerRequestDTO.builder()
            .firstName("Ric")
            .lastName("Danon")
            .build();

    private final OwnerResponseDTO OWNER_RESPONSE_MODEL = OwnerResponseDTO.builder()
            .ownerId(EXISTING_USER_ID_1)
            .firstName("Ric")
            .lastName("Danon")
            .build();

    private final String EXISTING_VET_ID = "181faeb5-c024-425c-9f08-663600008f06";

    private final VetRequestDTO VET_REQUEST_MODEL = VetRequestDTO.builder()
            .vetId(EXISTING_VET_ID) //this should not be a thing, Service client should not be setting vet's id
            .firstName("Pauline")
            .lastName("LeBlanc")
            .email("skjfhf@gmail.com")
            .phoneNumber("947-238-2847")
            .resume("Just became a vet")
            .workday(new HashSet<>())
            .specialties(new HashSet<>())
            .active(false)
            .build();

    private final VetResponseDTO VET_RESPONSE_MODEL = VetResponseDTO.builder()
            .vetId(EXISTING_VET_ID)
            .firstName("Pauline")
            .lastName("LeBlanc")
            .email("skjfhf@gmail.com")
            .phoneNumber("947-238-2847")
            .resume("Just became a vet")
            .workday(new HashSet<>())
            .specialties(new HashSet<>())
            .active(false)
            .build();

    private final Register OWNER_REGISTER = Register.builder()
            .username("Johnny123")
            .password("Password22##")
            .email("email@email.com")
            .owner(OWNER_REQUEST_MODEL)
            .build();

    private final RegisterVet VET_REGISTER = RegisterVet.builder()
            .userId(EXISTING_VET_ID)
            .username("vet")
            .email("vet@email.com")
            .password("pwd")
            .vet(VET_REQUEST_MODEL).build();;

    private final String EXISTING_MANAGER_ID = "67828401-a3bf-4a92-9bb9-1a3f9034fa18";

    private final RegisterInventoryManager MANAGER_REGISTER = RegisterInventoryManager.builder()
            .userId(EXISTING_MANAGER_ID)
            .username("Johnny123")
            .password("Password22##")
            .email("email@email.com")
            .build();

    private final UserPasswordLessDTO USER_RESPONSE = UserPasswordLessDTO
            .builder()
            .userId(EXISTING_MANAGER_ID)
            .email("email@email.com")
            .roles(Set.of(Role.builder().name(Roles.INVENTORY_MANAGER.name()).build()))
            .build();

    @Test
    void whenGetUserById_thenReturnUserDetails() {
        when(authServiceClient.getUserById(TOKEN, EXISTING_USER_ID_1))
                .thenReturn(Mono.just(USER_DETAILS_1));

        client.get()
                .uri("/api/gateway/users/{userId}", EXISTING_USER_ID_1)
                .cookie("Bearer", TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDetails.class)
                .value(user -> {
                    assertEquals(EXISTING_USER_ID_1, user.getUserId());
                    assertEquals(USER_DETAILS_1.getUsername(), user.getUsername());
                    assertEquals(USER_DETAILS_1.getEmail(), user.getEmail());
                });

        verify(authServiceClient).getUserById(TOKEN, EXISTING_USER_ID_1);
    }

    @Test
    void whenGetUserById_thenReturnNotFound() {
        when(authServiceClient.getUserById(TOKEN, NON_EXISTING_USER_ID))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/users/{userId}", NON_EXISTING_USER_ID)
                .cookie("Bearer", TOKEN)
                .exchange()
                .expectStatus().isNotFound();

        verify(authServiceClient).getUserById(TOKEN, NON_EXISTING_USER_ID);
    }

    @Test
    void whenUpdateUsername_thenReturnUpdatedUsername() {
        when(authServiceClient.updateUsername(EXISTING_USER_ID_1, UPDATED_USERNAME, TOKEN))
                .thenReturn(Mono.just(UPDATED_USERNAME));

        client.patch()
                .uri("/api/gateway/users/{userId}/username", EXISTING_USER_ID_1)
                .cookie("Bearer", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(UPDATED_USERNAME))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(username -> assertEquals(UPDATED_USERNAME, username));

        verify(authServiceClient).updateUsername(EXISTING_USER_ID_1, UPDATED_USERNAME, TOKEN);
    }

    @Test
    void whenUpdateUsername_thenReturnNotFound() {
        when(authServiceClient.updateUsername(EXISTING_USER_ID_1, UPDATED_USERNAME, TOKEN))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/users/{userId}/username", EXISTING_USER_ID_1)
                .cookie("Bearer", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(UPDATED_USERNAME))
                .exchange()
                .expectStatus().isNotFound();

        verify(authServiceClient).updateUsername(EXISTING_USER_ID_1, UPDATED_USERNAME, TOKEN);
    }

    @Test
    void whenCheckUsernameAvailability_thenReturnTrue() {
        when(authServiceClient.checkUsernameAvailability(USER_DETAILS_1.getUsername(), TOKEN))
                .thenReturn(Mono.just(true));

        client.get()
                .uri("/api/gateway/users/username/check?username={username}", USER_DETAILS_1.getUsername())
                .cookie("Bearer", TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(available -> assertEquals(true, available));

        verify(authServiceClient).checkUsernameAvailability(USER_DETAILS_1.getUsername(), TOKEN);
    }

    @Test
    void whenCheckUsernameAvailability_thenReturnFalse() {
        when(authServiceClient.checkUsernameAvailability(USER_DETAILS_1.getUsername(), TOKEN))
                .thenReturn(Mono.just(false));

        client.get()
                .uri("/api/gateway/users/username/check?username={username}", USER_DETAILS_1.getUsername())
                .cookie("Bearer", TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .value(available -> assertEquals(false, available));

        verify(authServiceClient).checkUsernameAvailability(USER_DETAILS_1.getUsername(), TOKEN);
    }

    @Test
    @DisplayName("Given valid JWT, verify user with redirection")
    void verify_user_with_redirection_shouldSucceed(){
        ResponseEntity<UserDetails> responseEntity = ResponseEntity.ok(USER_DETAILS_1);

        when(authServiceClient.verifyUser(TOKEN))
                .thenReturn(Mono.just(responseEntity));

        client.get()
                .uri("/api/gateway/users/verification/{token}", TOKEN)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "http://localhost:8080/#!/login");
    }

    @Test
    @DisplayName("Given valid Login, return JWT and user details")
    void login_valid() throws Exception {
        UserPasswordLessDTO userPasswordLessDTO = UserPasswordLessDTO.builder()
                .email(USER_DETAILS_1.getEmail())
                .username(USER_DETAILS_1.getUsername())
                .roles(USER_DETAILS_1.getRoles())
                .build();

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();

        headers.put(HttpHeaders.COOKIE, Collections.singletonList("Bearer=" + TOKEN + "; Path=/; HttpOnly; SameSite=Lax"));

        Mono<ResponseEntity<UserPasswordLessDTO>> httpResponse = Mono.just(ResponseEntity.ok().headers(HttpHeaders.readOnlyHttpHeaders(headers)).body(userPasswordLessDTO));

        when(authServiceClient.login(any()))
                .thenReturn(httpResponse);

        when(authServiceClient.login(any()))
                .thenReturn(httpResponse);


        final Login login = Login.builder()
                .password("valid")
                .email(USER_DETAILS_1.getEmail())
                .build();

        when(authServiceClient.login(any()))
                .thenReturn(httpResponse);

        client.post()
                .uri("/api/gateway/users/login")
                .accept(APPLICATION_JSON)
                .body(Mono.just(login), Login.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPasswordLessDTO.class)
                .value((res ->
                {
                    Assert.assertEquals(res.getEmail(),userPasswordLessDTO.getEmail());

                }));
    }

    @Test
    @DisplayName("Given invalid Login, throw 401")
    void login_invalid() throws Exception {
        final Login login = Login.builder()
                .password("valid")
                .email(USER_DETAILS_1.getEmail())
                .build();
        final String message = "I live in unending agony. I spent 6 hours and ended up with nothing";
        when(authServiceClient.login(any()))
                .thenThrow(new GenericHttpException(message, UNAUTHORIZED));

        client.post()
                .uri("/api/gateway/users/login")
                .accept(APPLICATION_JSON)
                .body(Mono.just(login), Login.class)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(UNAUTHORIZED.value())
                .jsonPath("$.message").isEqualTo(message)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Should Logout with a Valid Session, Clearing Bearer Cookie, and Returning 204")
    void logout_shouldClearBearerCookie() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.COOKIE, "Bearer=some.token.value; Path=/; HttpOnly; SameSite=Lax");
        when(authServiceClient.logout(any(ServerHttpRequest.class), any(ServerHttpResponse.class)))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));
        client.post()
                .uri("/api/gateway/users/logout")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE);
    }

    @Test
    @DisplayName("Given Expired Session, Logout Should Return 401")
    void logout_shouldReturnUnauthorizedForExpiredSession() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        when(authServiceClient.logout(any(ServerHttpRequest.class), any(ServerHttpResponse.class)))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
        client.post()
                .uri("/api/gateway/users/logout")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE);
    }

    @Test
    void sendForgottenEmail_ShouldSucceed(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();

        when(authServiceClient.sendForgottenEmail(Mono.just(dto)))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any());
    }

    @Test
    void sendForgottenEmail_ShouldFail(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();

        when(authServiceClient.sendForgottenEmail(any()))
                .thenThrow(new GenericHttpException("error",BAD_REQUEST));



        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any());
    }

    @Test
    void processResetPassword_ShouldSucceed(){
        final UserPasswordAndTokenRequestModel dto = UserPasswordAndTokenRequestModel.builder()
                .password("password")
                .token("Valid token")
                .build();

        when(authServiceClient.changePassword(any()))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        client.post()
                .uri("/api/gateway/users/reset_password")
                .body(Mono.just(dto), UserPasswordAndTokenRequestModel.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(authServiceClient, times(1)).changePassword(any());
    }

    @Test
    void createUser(){
        when(authServiceClient.createUser(any()))
                .thenReturn(Mono.just(OWNER_RESPONSE_MODEL));

        client.post()
                .uri("/api/gateway/users")
                .body(Mono.just(OWNER_REGISTER), Register.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(dto->{
                    assertNotNull(dto.getOwnerId());
                    Assert.assertEquals(dto.getFirstName(), OWNER_RESPONSE_MODEL.getFirstName());
                    Assert.assertEquals(dto.getLastName(), OWNER_RESPONSE_MODEL.getLastName());
                    Assert.assertEquals(dto.getAddress(), OWNER_RESPONSE_MODEL.getAddress());
                    Assert.assertEquals(dto.getCity(), OWNER_RESPONSE_MODEL.getCity());
                    Assert.assertEquals(dto.getProvince(), OWNER_RESPONSE_MODEL.getProvince());
                    Assert.assertEquals(dto.getTelephone(), OWNER_RESPONSE_MODEL.getTelephone());
                });
    }

    @Test
    void getAllUsers_ShouldReturn2(){
        when(authServiceClient.getUsers(TOKEN))
                .thenReturn(Flux.just(USER_DETAILS_1, USER_DETAILS_2));

        client.get()
                .uri("/api/gateway/users")
                .cookie("Bearer", TOKEN)
                .exchange()
                .expectBodyList(UserDetails.class)
                .hasSize(2);
    }

    @Test
    public void getAllUsers_NoUsername_ShouldReturnAllUsers() {
        when(authServiceClient.getUsers(anyString()))
                .thenReturn(Flux.just(USER_DETAILS_1, USER_DETAILS_2));

        client.get()
                .uri("/api/gateway/users")
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDetails.class)
                .hasSize(2);
    }

    @Test
    public void getAllUsers_WithUsername_ShouldReturnUsersWithSpecificUsername() {
        when(authServiceClient.getUsersByUsername(anyString(), anyString()))
                .thenReturn(Flux.just(USER_DETAILS_1));

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/users")
                        .queryParam("username", "specificUsername")
                        .build())
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDetails.class)
                .hasSize(1);
    }

    @Test
    void deleteUserById_ValidUserId_ShouldDeleteUser() {
        when(authServiceClient.deleteUser(anyString(), anyString()))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/users/validUserId")
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createUserInventoryManager_ShouldSucceed(){
        when(authServiceClient.createInventoryMangerUser(any()))
                .thenReturn(Mono.just(USER_RESPONSE));

        client.post()
                .uri("/api/gateway/users/inventoryManager")
                .body(Mono.just(MANAGER_REGISTER), RegisterInventoryManager.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserPasswordLessDTO.class)
                .value(dto->{
                    Assert.assertEquals(dto.getUserId(), USER_RESPONSE.getUserId());
                    Assert.assertEquals(dto.getEmail(), USER_RESPONSE.getEmail());
                    Assert.assertEquals(dto.getRoles(),  USER_RESPONSE.getRoles());
                });
    }

    @Test
    void createVet() {
        when(authServiceClient.createVetUser(any(Mono.class)))
                .thenReturn((Mono.just(VET_RESPONSE_MODEL)));

        client.post()
                .uri("/api/gateway/users/vets")
                .body(Mono.just(VET_REGISTER), RegisterVet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(authServiceClient, times(1))
                .createVetUser(any(Mono.class));
    }

    //    @Test
//    @DisplayName("Given invalid JWT, expect 400")
//    void verify_user_bad_token() {
//
//        final String errorMessage = "some error message";
//        final String invalidToken = "some.invalid.token";
//
//        when(authServiceClient.verifyUser(invalidToken))
//                .thenThrow(new GenericHttpException(errorMessage, BAD_REQUEST));
//
//        client.get()
//                .uri("/api/gateway/verification/{token}", invalidToken)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
//                .jsonPath("$.timestamp").exists()
//                .jsonPath("$.message").isEqualTo(errorMessage);
//    }

    //    @Test
//    void getUserDetails() {
//        UserDetails user = new UserDetails();
//        user.setId(1);
//        user.setUsername("roger675");
//        user.setPassword("secretnooneknows");
//        user.setEmail("RogerBrown@gmail.com");
//
//        when(authServiceClient.getUser(1))
//                .thenReturn(Mono.just(user));
//
//        client.get()
//
//                .uri("/api/gateway/users/1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.username").isEqualTo("roger675")
//                .jsonPath("$.password").isEqualTo("secretnooneknows")
//                .jsonPath("$.email").isEqualTo("RogerBrown@gmail.com");
//
//        assertEquals(user.getId(), 1);
//    }
}