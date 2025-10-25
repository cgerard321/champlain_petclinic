package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PetController.class)
public class PetControllerUnitTest {

    @MockBean
    private PetService petService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenDeletePetByPetIdV2_withExistingPetId_thenReturnPetResponseDTO() {
        Pet pet = buildPet();
        PetResponseDTO petResponseDTO = EntityDTOUtil.toPetResponseDTO(pet);

        when(petService.deletePetByPetIdV2(pet.getPetId())).thenReturn(Mono.just(petResponseDTO));

        webTestClient.delete()
                .uri("/pets/{petId}/v2", pet.getPetId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .isEqualTo(petResponseDTO);

        verify(petService).deletePetByPetIdV2(pet.getPetId());
    }

    @Test
    void whenCreatePetForOwner_withValidRequest_thenReturnCreatedPet() {
        String ownerId = "valid-owner-id";
        PetRequestDTO petRequest = buildPetRequestDTO();
        PetResponseDTO expectedResponse = buildPetResponseDTO();

        when(petService.createPetForOwner(anyString(), any(Mono.class))).thenReturn(Mono.just(expectedResponse));

        webTestClient.post()
                .uri("/pets/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(petRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetResponseDTO.class)
                .isEqualTo(expectedResponse);

        verify(petService).createPetForOwner(anyString(), any(Mono.class));
    }

    @Test
    void whenCreatePetForOwner_withInvalidOwner_thenReturnBadRequest() {
        String invalidOwnerId = "invalid-owner-id";
        PetRequestDTO petRequest = buildPetRequestDTO();

        when(petService.createPetForOwner(anyString(), any(Mono.class)))
                .thenReturn(Mono.error(new NotFoundException("Owner not found with id: " + invalidOwnerId)));

        webTestClient.post()
                .uri("/pets/owners/{ownerId}/pets", invalidOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(petRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(petService).createPetForOwner(anyString(), any(Mono.class));
    }

    @Test
    void deletePetPhoto_ShouldReturnOk() {
        PetResponseDTO petResponseDTO = buildPetResponseDTO();
        String petId = "0e4d8481-b611-4e52-baed-af16caa8bf8a";
        
        when(petService.deletePetPhoto(petId)).thenReturn(Mono.just(petResponseDTO));

        webTestClient.patch()
                .uri("/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .isEqualTo(petResponseDTO);

        verify(petService).deletePetPhoto(petId);
    }

    @Test
    void deletePetPhoto_WithNonExistentPet_ShouldReturnNotFound() {
        String petId = "00000000-0000-0000-0000-000000000000";
        
        when(petService.deletePetPhoto(petId)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri("/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(petService).deletePetPhoto(petId);
    }

    private Pet buildPet() {
        return Pet.builder()
                .petId("c947af59-c389-416e-86d8-7f6132476590")
                .name("Cookie")
                .ownerId("a0ebbe09-e555-4256-aa1a-525b32c37a31")
                .petTypeId("1")
                .birthDate(new Date())
                .isActive("true")
                .build();
    }

    private PetRequestDTO buildPetRequestDTO() {
        return PetRequestDTO.builder()
                .name("Buddy")
                .petTypeId("2")
                .birthDate(new Date())
                .weight("15.5")
                .isActive("true")
                .build();
    }

    private PetResponseDTO buildPetResponseDTO() {
        return PetResponseDTO.builder()
                .petId("generated-pet-id")
                .name("Buddy")
                .ownerId("valid-owner-id")
                .petTypeId("2")
                .birthDate(new Date())
                .weight("15.5")
                .isActive("true")
                .build();
    }
}