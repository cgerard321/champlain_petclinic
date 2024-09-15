package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigCustomersService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerUpdateOwnerEndpoint();
        mockServerConfigCustomersService.registerAddOwnerEndpoint();
        mockServerConfigCustomersService.registerGetAllOwnersEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();

    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenUpdateOwner_asCustomer_withValidOwnerId_thenReturnUpdatedOwnerResponseDTO() {
        OwnerRequestDTO updatedRequestDTO = OwnerRequestDTO.builder()
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .province("Quebec")
                .telephone("6085551749")
                .build();

        Mono<OwnerResponseDTO> result = webTestClient.put()
                .uri("/api/v2/gateway/owners/{ownerId}", updatedRequestDTO.getOwnerId())
                .cookie("Bearer", jwtTokenForValidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestDTO), OwnerRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(ownerResponseDTO -> {
                    assertNotNull(ownerResponseDTO);
                    assertEquals(updatedRequestDTO.getOwnerId(), ownerResponseDTO.getOwnerId());
                    assertEquals(updatedRequestDTO.getFirstName(), ownerResponseDTO.getFirstName());
                    assertEquals(updatedRequestDTO.getLastName(), ownerResponseDTO.getLastName());
                    assertEquals(updatedRequestDTO.getAddress(), ownerResponseDTO.getAddress());
                    assertEquals(updatedRequestDTO.getCity(), ownerResponseDTO.getCity());
                    assertEquals(updatedRequestDTO.getProvince(), ownerResponseDTO.getProvince());
                    assertEquals(updatedRequestDTO.getTelephone(), ownerResponseDTO.getTelephone());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenUpdateOwner_asCustomer_withInvalidOwnerId_thenReturnInvalidInputException() {
        OwnerRequestDTO updatedRequestDTO = OwnerRequestDTO.builder()
                .ownerId("invalid-owner-id")
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .province("Quebec")
                .telephone("6085551749")
                .build();

        Mono<InvalidInputException> result = webTestClient.put()
                .uri("/api/v2/gateway/owners/{ownerId}", updatedRequestDTO.getOwnerId())
                .cookie("Bearer", jwtTokenForInvalidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestDTO), OwnerRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(InvalidInputException.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(errorResponse -> {
                    assertNotNull(errorResponse);
                    assertEquals("Provided owner id is invalid: " + updatedRequestDTO.getOwnerId(), errorResponse.getMessage());
                    return true;
                })
                .verifyComplete();
    }


    @Test
    void whenAddOwner_asAdmin_thenReturnCreatedOwnerResponseDTO() {
        OwnerRequestDTO newOwnerRequestDTO = OwnerRequestDTO.builder()
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .province("Quebec")
                .telephone("6085551749")
                .build();

        Mono<OwnerResponseDTO> result = webTestClient.post()
                .uri("/api/v2/gateway/owners")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newOwnerRequestDTO), OwnerRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(ownerResponseDTO -> {
                    assertNotNull(ownerResponseDTO);
                    assertNotNull(ownerResponseDTO.getOwnerId());
                    assertEquals(newOwnerRequestDTO.getFirstName(), ownerResponseDTO.getFirstName());
                    assertEquals(newOwnerRequestDTO.getLastName(), ownerResponseDTO.getLastName());
                    assertEquals(newOwnerRequestDTO.getAddress(), ownerResponseDTO.getAddress());
                    assertEquals(newOwnerRequestDTO.getCity(), ownerResponseDTO.getCity());
                    assertEquals(newOwnerRequestDTO.getProvince(), ownerResponseDTO.getProvince());
                    assertEquals(newOwnerRequestDTO.getTelephone(), ownerResponseDTO.getTelephone());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenAddOwner_asCustomer_thenReturnForbidden() {
        OwnerRequestDTO newOwnerRequestDTO = OwnerRequestDTO.builder()
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .province("Quebec")
                .telephone("6085551749")
                .build();

        webTestClient.post()
                .uri("/api/v2/gateway/owners")
                .cookie("Bearer", jwtTokenForValidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newOwnerRequestDTO), OwnerRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenGetAllOwners_asAdmin_thenReturnAllOwners() {
        Flux<OwnerResponseDTO> result = webTestClient.get()
                .uri("/api/v2/gateway/owners")
                .cookie("Bearer", jwtTokenForValidAdmin)  // Token for an admin user
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody();

    }


}