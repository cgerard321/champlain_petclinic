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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        mockServerConfigCustomersService.registerDeleteOwnerEndpoint();
        mockServerConfigCustomersService.registerGetOwnerByIdEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();

    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenUpdateOwner_asCustomer_withValidOwnerId_thenReturnUpdatedOwnerResponseDTO() {
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";

        OwnerRequestDTO updatedRequestDTO = OwnerRequestDTO.builder()
                .firstName("Betty")
                .lastName("Davis")
                .address("638 Cardinal Ave.")
                .city("Sun Prairie")
                .province("Quebec")
                .telephone("6085551749")
                .build();

        Mono<OwnerResponseDTO> result = webTestClient.put()
                .uri("/api/v2/gateway/owners/{ownerId}", ownerId)
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
                    assertEquals(ownerId, ownerResponseDTO.getOwnerId());
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

        StepVerifier
                .create(result)
                .expectNextCount(3)
                .verifyComplete();


    }

    @Test
    void whenGetAllOwners_asVet_thenReturnAllOwners() {
        Flux<OwnerResponseDTO> result = webTestClient.get()
                .uri("/api/v2/gateway/owners")
                .cookie("Bearer", jwtTokenForValidVet)  // Token for a vet user
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody();

        StepVerifier
                .create(result)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void whenDeleteOwner_asAdmin_thenReturnOwnerResponse() {
        // Mock data to simulate the OwnerResponseDTO
        OwnerResponseDTO expectedOwner = new OwnerResponseDTO();
        expectedOwner.setOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        expectedOwner.setFirstName("Betty");
        expectedOwner.setLastName("Davis");
        expectedOwner.setAddress("638 Cardinal Ave.");
        expectedOwner.setCity("Sun Prairie");
        expectedOwner.setProvince("Quebec");
        expectedOwner.setTelephone("6085551749");

        // Perform the DELETE request and expect OwnerResponseDTO in the body
        webTestClient.delete()
                .uri("/api/v2/gateway/owners/{ownerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .cookie("Bearer", "valid-test-token-for-valid-admin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Now we expect a 200 OK response, not 204 NO_CONTENT
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponse -> {
                    assertThat(ownerResponse.getOwnerId()).isEqualTo(expectedOwner.getOwnerId());
                    assertThat(ownerResponse.getFirstName()).isEqualTo(expectedOwner.getFirstName());
                    assertThat(ownerResponse.getLastName()).isEqualTo(expectedOwner.getLastName());
                    assertThat(ownerResponse.getAddress()).isEqualTo(expectedOwner.getAddress());
                    assertThat(ownerResponse.getCity()).isEqualTo(expectedOwner.getCity());
                    assertThat(ownerResponse.getProvince()).isEqualTo(expectedOwner.getProvince());
                    assertThat(ownerResponse.getTelephone()).isEqualTo(expectedOwner.getTelephone());
                });
    }


    @Test
    void whenDeleteOwner_withInvalidId_thenReturnNotFound() {

        String invalidOwnerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f03610";

        webTestClient.delete()
                .uri("/owners/{ownerId}", invalidOwnerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetOwnerById_asAdmin_withValidId_thenReturnOwnerResponseDTO() {
        String validOwnerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        OwnerResponseDTO expectedOwner = new OwnerResponseDTO();
        expectedOwner.setOwnerId(validOwnerId);
        expectedOwner.setFirstName("Betty");
        expectedOwner.setLastName("Davis");
        expectedOwner.setAddress("638 Cardinal Ave.");
        expectedOwner.setCity("Sun Prairie");
        expectedOwner.setProvince("Quebec");
        expectedOwner.setTelephone("6085551749");

        Mono<OwnerResponseDTO> result = webTestClient.get()
                .uri("/api/v2/gateway/owners/{ownerId}", validOwnerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
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
                    assertEquals(expectedOwner.getOwnerId(), ownerResponseDTO.getOwnerId());
                    assertEquals(expectedOwner.getFirstName(), ownerResponseDTO.getFirstName());
                    assertEquals(expectedOwner.getLastName(), ownerResponseDTO.getLastName());
                    assertEquals(expectedOwner.getAddress(), ownerResponseDTO.getAddress());
                    assertEquals(expectedOwner.getCity(), ownerResponseDTO.getCity());
                    assertEquals(expectedOwner.getProvince(), ownerResponseDTO.getProvince());
                    assertEquals(expectedOwner.getTelephone(), ownerResponseDTO.getTelephone());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetOwnerById_withInvalidId_thenReturnNotFound() {
        String invalidOwnerId = "non-existing-owner-id";

        webTestClient.get()
                .uri("/api/v2/gateway/owners/{ownerId}", invalidOwnerId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


}