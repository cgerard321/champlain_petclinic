package com.petclinic.bffapigateway.presentationlayer.v2.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.Workday;
import com.petclinic.bffapigateway.utils.Security.Variables.SecurityConst;
import com.petclinic.bffapigateway.utils.Utility;
import lombok.RequiredArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class AuthServiceClientControllerIntegrationTest {

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
        cartServiceClient = Mockito.mock(CartServiceClient.class);
        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                customersServiceClient, vetsServiceClient, server.getHostName(),
                String.valueOf(server.getPort()), cartServiceClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }
    @Test
    @DisplayName("Should fetch all users successfully")
    void getAllUsers_ShouldReturnUserList() throws Exception {

        String responseBody = "[{\"userId\":\"1\", \"username\":\"user1\", \"email\":\"user1@example.com\"}," +
                "{\"userId\":\"2\", \"username\":\"user2\", \"email\":\"user2@example.com\"}]";
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody);

        server.enqueue(mockResponse);

        Flux<UserDetails> usersFlux = authServiceClient.getAllUsers("valid-jwt-token");

        StepVerifier.create(usersFlux)
                .expectNextMatches(user -> user.getUserId().equals("1") && user.getUsername().equals("user1"))
                .expectNextMatches(user -> user.getUserId().equals("2") && user.getUsername().equals("user2"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty list when no users are found")
    void getAllUsers_ShouldReturnEmptyList() {

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[]");

        server.enqueue(mockResponse);

        Flux<UserDetails> usersFlux = authServiceClient.getAllUsers("valid-jwt-token");

        StepVerifier.create(usersFlux)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle error when fetching users fails")
    void getAllUsers_ShouldReturnError() {

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json");

        server.enqueue(mockResponse);

        Flux<UserDetails> usersFlux = authServiceClient.getAllUsers("valid-jwt-token");

        StepVerifier.create(usersFlux)
                .expectError()
                .verify();
    }


    @Test
    @DisplayName("Should try to validate a token and fail")
    void ShouldVerifyUserToken_ShouldReturnInvalidForV2(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401);

        server.enqueue(mockResponse);

        final Mono<Void> validatedTokenResponse = authServiceClient.verifyUserUsingV2Endpoint("invalidToken").then();

        // check status response in step verifier
        StepVerifier.create(validatedTokenResponse)
                .expectNextCount(0)
                .verifyError();
    }

    @Test
    @DisplayName("Should verifyUser a token")
    void ShouldVerifyUserToken_ShouldReturnOkForV2(){
        final MockResponse mockResponse = new MockResponse();
        mockResponse
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200);

        server.enqueue(mockResponse);

        final Mono<Void> verifyUser = authServiceClient.verifyUserUsingV2Endpoint("token").then();

        // check status response in step verifier
        StepVerifier.create(verifyUser)
                .expectNextCount(0)
                .verifyComplete();
    }

}