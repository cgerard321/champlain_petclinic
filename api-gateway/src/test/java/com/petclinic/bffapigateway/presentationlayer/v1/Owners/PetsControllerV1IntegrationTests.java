package com.petclinic.bffapigateway.presentationlayer.v1.Owners;

import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigCustomersService;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Date;
import static com.petclinic.bffapigateway.presentationlayer.v1.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetsControllerV1IntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    private final String PET_PATH = "/api/gateway/pets";
    private final String OWNER_PET_PATH = "/api/gateway/pets/owners/{ownerId}/pets/{petId}";
    private final String PET_ID = "petId-400";
    private final String OWNER_ID = "ownerId-100";
    private final Date BIRTH_DATE = new Date(2023, 1, 1);

    @BeforeEach
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
    }

    @AfterEach
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    private PetResponseDTO buildPetResponseDTO() {
        return PetResponseDTO.builder()
                .petId(PET_ID)
                .name("Fluffy")
                .birthDate(BIRTH_DATE)
                .petTypeId("pt-1")
                .weight("10.5")
                .isActive("true")
                .build();
    }

    private PetRequestDTO buildPetRequestDTO(String name) {
        return PetRequestDTO.builder()
                .name(name)
                .birthDate(BIRTH_DATE)
                .petTypeId("pt-1")
                .weight("10.5")
                .isActive("true")
                .build();
    }

    @Test
    void whenGetPetByPetId_WithValidClient_thenReturnPet() {
        final String OWNER_ID = "test-owner-id";
        PetResponseDTO expectedPet = buildPetResponseDTO();

        mockServerConfigCustomersService.registerGetPetByIdEndpoint(PET_ID, expectedPet);

        Mono<PetResponseDTO> result = webTestClient.get()
                .uri(PET_PATH + "/owners/{ownerId}/pets/{petId}", OWNER_ID, PET_ID)
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
                    assertThat(pet.getName()).isEqualTo("Fluffy");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetPetForOwner_WithValidClient_thenReturnPet() {
        PetResponseDTO expectedPet = buildPetResponseDTO();

        mockServerConfigCustomersService.registerGetPetByIdEndpoint(PET_ID, expectedPet);


        Mono<PetResponseDTO> result = webTestClient.get()
                .uri(OWNER_PET_PATH, OWNER_ID, PET_ID)
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
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetPetByPetId_WithServiceFail_thenReturnError() {
        mockServerConfigCustomersService.registerPetEndpoint_500(PET_ID);

        webTestClient.get()
                .uri(PET_PATH + "/{petId}", PET_ID)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void whenUpdatePet_WithNonExistentPetId_thenReturn404NotFound() {
        final String nonExistentId = "non-existent-id-404";
        PetRequestDTO updateRequest = buildPetRequestDTO("Ghost Pet");

        mockServerConfigCustomersService.registerUpdatePetEndpoint_404(nonExistentId);

        webTestClient.put()
                .uri(PET_PATH + "/{petId}", nonExistentId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), PetRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound();
    }
}
