package com.petclinic.bffapigateway.presentationlayer.V1.Owners;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigCustomersService;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;
import static com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OwnerControllerV1IntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    private final String OWNER_BASE_PATH = "/api/gateway/owners";
    private final String OWNER_ID = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
    private final String PET_ID = "pet-id-456";

    // DTOs matching the mock server expectations
    OwnerRequestDTO ownerUpdateRequest = OwnerRequestDTO.builder()
            .firstName("Betty")
            .lastName("Davis")
            .address("638 Cardinal Ave.")
            .city("Sun Prairie")
            .province("Quebec")
            .telephone("6085551749")
            .build();

    OwnerResponseDTO petOwnerResponse = OwnerResponseDTO.builder()
            .ownerId(OWNER_ID)
            .firstName("Betty")
            .lastName("Davis")
            .address("638 Cardinal Ave.")
            .city("Sun Prairie")
            .province("Quebec")
            .telephone("6085551749")
            .build();

    PetResponseDTO petResponse = PetResponseDTO.builder()
            .petId(PET_ID)
            .name("Buster")
            .ownerId(OWNER_ID)
            .petTypeId("pt-1")
            .build();


    @BeforeEach
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerGetAllOwnersEndpoint();
        mockServerConfigCustomersService.registerGetOwnerByIdEndpoint();
        mockServerConfigCustomersService.registerUpdateOwnerEndpoint();
        mockServerConfigCustomersService.registerDeleteOwnerEndpoint();
        mockServerConfigCustomersService.registerGetPetForOwnerEndpoint(OWNER_ID, PET_ID, petResponse);

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
    }

    @AfterEach
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenGetAllOwners_WithValidClient_thenReturnResult() {

        Mono<List<OwnerResponseDTO>> result = webTestClient.get()
                .uri(OWNER_BASE_PATH)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody()
                .collectList()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(ownerResponseDTOS -> {
                    assertNotNull(ownerResponseDTOS);
                    assertThat(ownerResponseDTOS.size()).isEqualTo(3);
                    assertThat(ownerResponseDTOS.get(0).getFirstName()).isEqualTo("John");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetOwnerDetails_withValidId_thenReturnOwner() {
        Mono<OwnerResponseDTO> result = webTestClient.get()
                .uri(OWNER_BASE_PATH + "/{ownerId}", OWNER_ID)
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
                .expectNextMatches(owner -> {
                    assertNotNull(owner);
                    assertThat(owner.getOwnerId()).isEqualTo(OWNER_ID);
                    assertThat(owner.getFirstName()).isEqualTo("Betty");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeleteOwner_withValidId_thenReturnNoContent() {

        webTestClient.delete()
                .uri(OWNER_BASE_PATH + "/{ownerId}", OWNER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetPet_withValidOwnerAndPetId_thenReturnPet() {
        Mono<PetResponseDTO> result = webTestClient.get()
                .uri(OWNER_BASE_PATH + "/{ownerId}/pets/{petId}", OWNER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(pet -> {
                    assertNotNull(pet);
                    assertThat(pet.getPetId()).isEqualTo(PET_ID);
                    assertThat(pet.getName()).isEqualTo("Buster");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePet_withValidOwnerAndPetId_thenReturnNotFound() {
        webTestClient.delete()
                .uri(OWNER_BASE_PATH + "/{ownerId}/pets/{petId}", OWNER_ID, PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetOwnerWithPhotoIntegration_thenReturnOwnerWithPhoto() {
        mockServerConfigCustomersService.clearExpectationsForOwner(OWNER_ID);
        
        String mockOwnerJson = """
            {
                "ownerId": "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a",
                "firstName": "John",
                "lastName": "Doe",
                "photo": {
                    "fileId": "photo-123",
                    "fileName": "profile.png",
                    "fileType": "image/png",
                    "fileData": "aW50ZWdyYXRpb25QaG90b0RhdGE="
                }
            }
            """;
        mockServerConfigCustomersService.registerGetOwnerWithPhotoEndpoint(OWNER_ID, mockOwnerJson);

        Mono<OwnerResponseDTO> result = webTestClient.get()
                .uri(OWNER_BASE_PATH + "/{ownerId}?includePhoto=true", OWNER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(OwnerResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier.create(result)
                .expectNextMatches(owner -> 
                    owner.getOwnerId().equals("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a") &&
                    owner.getFirstName().equals("John") &&
                    owner.getLastName().equals("Doe") &&
                    owner.getPhoto() != null &&
                    owner.getPhoto().getFileType().equals("image/png"))
                .verifyComplete();
    }

    @Test
    void whenGetOwnerPhotoIntegrationNotFound_thenReturn404() {
        mockServerConfigCustomersService.registerGetOwnerPhotoEndpoint(OWNER_ID, null);

        webTestClient.get()
                .uri(OWNER_BASE_PATH + "/{ownerId}/photos", OWNER_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.IMAGE_PNG)
                .exchange()
                .expectStatus().isNotFound();
    }

}
