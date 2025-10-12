package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Register;
import com.petclinic.bffapigateway.dtos.Auth.UserDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ContextConfiguration(classes = {
        UserController.class,
        AuthServiceClient.class,
        CustomersServiceClient.class,
})
@WebFluxTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
        classes = {JwtTokenFilter.class,RoleFilter.class}),useDefaultFilters = false)
@AutoConfigureWebTestClient
class AuthControllerUnitTest {
    @Autowired private WebTestClient client;
    @MockBean private CustomersServiceClient customersServiceClient;
    @MockBean private AuthServiceClient authServiceClient;
    private MockWebServer server;

    @InjectMocks
    private UserController userController;

    @Mock
    private CustomersServiceClient customersServiceClientMock;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToController(new UserController(authServiceClient)).build();
    }


    @Test
    @DisplayName("Given valid JWT, verify user with redirection")
    void verify_user_with_redirection_shouldSucceedForV2(){
        final String validToken = "some.valid.token";

        // Mocking the behavior of authServiceClient.verifyUserUsingV2Endpoint to return a successful response
        UserDetails user = UserDetails.builder()
                .userId("22222")
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        ResponseEntity<UserDetails> responseEntity = ResponseEntity.ok(user);

        when(authServiceClient.verifyUserUsingV2Endpoint(validToken))
                .thenReturn(Mono.just(responseEntity));

        client.get()
                .uri("/api/v2/gateway/verification/{token}", validToken)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "${frontend.url}/users/login");
    }
    @Test
    @DisplayName("Should fetch all users successfully")
    void getAllUsers_ShouldReturnUserList() {
        String jwtToken = "validJwtToken";

        UserDetails user1 = UserDetails.builder()
                .userId("userId1")
                .email("user1@mail.com")
                .username("user1")
                .roles(Collections.emptySet())
                .build();

        UserDetails user2 = UserDetails.builder()
                .userId("userId2")
                .email("user2@mail.com")
                .username("user2")
                .roles(Collections.emptySet())
                .build();

        when(authServiceClient.getAllUsers(jwtToken))
                .thenReturn(Flux.just(user1, user2));

        client.get()
                .uri("/api/v2/gateway/users")
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].userId").isEqualTo("userId1")
                .jsonPath("$[0].email").isEqualTo("user1@mail.com")
                .jsonPath("$[0].username").isEqualTo("user1")
                .jsonPath("$[1].userId").isEqualTo("userId2")
                .jsonPath("$[1].email").isEqualTo("user2@mail.com")
                .jsonPath("$[1].username").isEqualTo("user2");
    }

    @Test
    @DisplayName("Should return an empty list when no users are found")
    void getAllUsers_ShouldReturnEmptyList() {
        String jwtToken = "validJwtToken";

        when(authServiceClient.getAllUsers(jwtToken))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/v2/gateway/users")
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 500 error when service fails")
    void getAllUsers_ShouldReturnInternalServerError() {
        String jwtToken = "validJwtToken";

        when(authServiceClient.getAllUsers(jwtToken))
                .thenReturn(Flux.error(new RuntimeException("Service error")));

        client.get()
                .uri("/api/v2/gateway/users")
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    @DisplayName("Given valid Register object, create user successfully")
    void createUserUsingV2Endpoint_shouldSucceed() {

        OwnerResponseDTO responseDTO = OwnerResponseDTO.builder()
                .ownerId("8f49e2d4-21da-4316-a5ce-56fee11becb6")
                .firstName("firstName")
                .lastName("lastName")
                .address("Address")
                .city("City")
                .province("Province")
                .telephone("1234567891")
                .build();

        Register registerRequest = new Register();
        registerRequest.setUserId("12345");
        registerRequest.setPassword("password");
        registerRequest.setOwner(OwnerRequestDTO.builder()
                .ownerId("8f49e2d4-21da-4316-a5ce-56fee11becb6")
                .firstName("firstName")
                .lastName("lastName")
                .address("Address")
                .city("City")
                .province("Province")
                .telephone("1234567891")
                .build());
        registerRequest.setUsername("newUser");
        registerRequest.setEmail("newuser@mail.com");

        when(authServiceClient.createUserUsingV2Endpoint(any(Mono.class)))
                .thenReturn(Mono.just(responseDTO));

        client.post()
                .uri("/api/v2/gateway/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OwnerResponseDTO.class)
                .value(response -> {
                    Assertions.assertEquals("8f49e2d4-21da-4316-a5ce-56fee11becb6", response.getOwnerId());
                    Assertions.assertEquals("firstName", response.getFirstName());
                });
    }

    @Test
    @DisplayName("Given data with no id, return not found")
    void createUserUsingV2Endpoint_shouldReturnBadRequest() {
        Register invalidRegisterRequest = new Register();

        when(authServiceClient.createUserUsingV2Endpoint(Mono.just(invalidRegisterRequest)))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRegisterRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should delete a user successfully")
    void deleteUser_ShouldReturnNoContent() {
        String jwtToken = "validJwtToken";
        String userId = "userId";

        when(authServiceClient.deleteUser(jwtToken, userId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/users/{userId}", userId)
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isNoContent();
    }


    @Test
    @DisplayName("Should return error when deleting a user fails")
    void deleteUser_ShouldReturnInternalServerError() {
        String jwtToken = "validJwtToken";
        String userId = "userId";

        when(authServiceClient.deleteUser(jwtToken, userId))
                .thenReturn(Mono.error(new RuntimeException("Error deleting user")));

        client.delete()
                .uri("/api/v2/gateway/users/{userId}", userId)
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().is5xxServerError();
    }

}
