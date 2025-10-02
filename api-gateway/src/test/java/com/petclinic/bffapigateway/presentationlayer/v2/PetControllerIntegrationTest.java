package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PetControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigCustomersService mockServerConfigCustomersService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        mockServerConfigCustomersService = new MockServerConfigCustomersService();
        mockServerConfigCustomersService.registerDeletePetEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigCustomersService.registerGetPetByIdEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigCustomersService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenDeletePet_asAdmin_withValidPetId_thenReturnPetResponseDTO() throws ParseException {
        PetResponseDTO pet = PetResponseDTO.builder()
                .petId("53163352-8398-4513-bdff-b7715c056d1d")
                .name("Buddy")
                .birthDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("1999-11-01T00:00:00.000+00:00"))
                .petTypeId("1")
                .isActive("true")
                .weight("1.3")
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .build();

        Mono<PetResponseDTO> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", pet.getPetId())
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
                .expectNextMatches(petResponseDTO -> {
                    assertNotNull(petResponseDTO);
                    assertThat(petResponseDTO.getPetId()).isEqualTo(pet.getPetId());
                    assertThat(petResponseDTO.getName()).isEqualTo(pet.getName());
                    assertThat(petResponseDTO.getBirthDate()).isEqualTo(pet.getBirthDate());
                    assertThat(petResponseDTO.getPetTypeId()).isEqualTo(pet.getPetTypeId());
                    assertThat(petResponseDTO.getIsActive()).isEqualTo(pet.getIsActive());
                    assertThat(petResponseDTO.getWeight()).isEqualTo(pet.getWeight());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePet_asAdmin_withInvalidPetId_thenReturnInvalidInputException() {
        String invalidPetId = "invalid-pet-id";

        Mono<InvalidInputException> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", invalidPetId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(InvalidInputException.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(errorResponse -> {
                    assertNotNull(errorResponse);
                    assertThat(errorResponse.getMessage()).isEqualTo("Provided pet id is invalid: " + invalidPetId);
                    return true;
                })
                .verifyComplete();
    }


    @Test
    void whenDeletePet_asVet_withValidPetId_thenReturnPetResponseDTO() throws ParseException {
        PetResponseDTO pet = PetResponseDTO.builder()
                .petId("53163352-8398-4513-bdff-b7715c056d1d")
                .name("Buddy")
                .birthDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("1999-11-01T00:00:00.000+00:00"))
                .petTypeId("1")
                .isActive("true")
                .weight("1.3")
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .build();

        Mono<PetResponseDTO> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", pet.getPetId())
                .cookie("Bearer", jwtTokenForValidVet)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(petResponseDTO -> {
                    assertNotNull(petResponseDTO);
                    assertThat(petResponseDTO.getPetId()).isEqualTo(pet.getPetId());
                    assertThat(petResponseDTO.getName()).isEqualTo(pet.getName());
                    assertThat(petResponseDTO.getBirthDate()).isEqualTo(pet.getBirthDate());
                    assertThat(petResponseDTO.getPetTypeId()).isEqualTo(pet.getPetTypeId());
                    assertThat(petResponseDTO.getIsActive()).isEqualTo(pet.getIsActive());
                    assertThat(petResponseDTO.getWeight()).isEqualTo(pet.getWeight());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePet_asVet_withInvalidPetId_thenReturnInvalidInputException() {
        String invalidPetId = "invalid-pet-id";

        Mono<InvalidInputException> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", invalidPetId)
                .cookie("Bearer", jwtTokenForValidVet)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(InvalidInputException.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(errorResponse -> {
                    assertNotNull(errorResponse);
                    assertThat(errorResponse.getMessage()).isEqualTo("Provided pet id is invalid: " + invalidPetId);
                    return true;
                })
                .verifyComplete();
    }


    @Test
    void whenDeletePet_asOwner_withValidPetId_thenReturnPetResponseDTO() throws ParseException {
        PetResponseDTO pet = PetResponseDTO.builder()
                .petId("53163352-8398-4513-bdff-b7715c056d1d")
                .name("Buddy")
                .birthDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("1999-11-01T00:00:00.000+00:00"))
                .petTypeId("1")
                .isActive("true")
                .weight("1.3")
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .build();

        Mono<PetResponseDTO> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", pet.getPetId())
                .cookie("Bearer", jwtTokenForValidOwnerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(PetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(petResponseDTO -> {
                    assertNotNull(petResponseDTO);
                    assertThat(petResponseDTO.getPetId()).isEqualTo(pet.getPetId());
                    assertThat(petResponseDTO.getName()).isEqualTo(pet.getName());
                    assertThat(petResponseDTO.getBirthDate()).isEqualTo(pet.getBirthDate());
                    assertThat(petResponseDTO.getPetTypeId()).isEqualTo(pet.getPetTypeId());
                    assertThat(petResponseDTO.getIsActive()).isEqualTo(pet.getIsActive());
                    assertThat(petResponseDTO.getWeight()).isEqualTo(pet.getWeight());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePet_asOwner_withInvalidPetId_thenReturnInvalidInputException() {
        String invalidPetId = "invalid-pet-id";

        Mono<InvalidInputException> result = webTestClient.delete()
                .uri("/api/v2/gateway/pets/{petId}", invalidPetId)
                .cookie("Bearer", jwtTokenForValidOwnerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .returnResult(InvalidInputException.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(errorResponse -> {
                    assertNotNull(errorResponse);
                    assertThat(errorResponse.getMessage()).isEqualTo("Provided pet id is invalid: " + invalidPetId);
                    return true;
                })
                .verifyComplete();
    }
}