package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PetController.class)
public class PetControllerUnitTest {

    @MockBean
    private PetService petService;

    @MockBean
    private PhotoService photoService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenDeletePetByPetIdV2_withExistingPetId_thenReturnPetResponseDTO() {
        Pet pet = buildPet();
        PetResponseDTO petResponseDTO = EntityDTOUtil.toPetResponseDTO(pet);

        when(petService.deletePetByPetIdV2(pet.getPetId())).thenReturn(Mono.just(petResponseDTO));

        webTestClient.delete()
                .uri("/pet/{petId}/v2", pet.getPetId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .isEqualTo(petResponseDTO);

        verify(petService).deletePetByPetIdV2(pet.getPetId());
    }

    @Test
    void whenGetPetPhotoByPetId_withValidPetId_thenReturnPhotoResponseModel() {
        String petId = "12345678-1234-1234-1234-123456789012";
        PhotoResponseModel photoResponseModel = PhotoResponseModel.builder()
                .id("1")
                .name("Cat default photo")
                .type("image/jpeg")
                .photo("base64ImageData")
                .build();

        when(photoService.getPetPhotoByPetId(petId)).thenReturn(Mono.just(photoResponseModel));

        webTestClient.get()
                .uri("/pet/{petId}/photo", petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoResponseModel.class)
                .value(response -> {
                    assert response.getId().equals("1");
                    assert response.getName().equals("Cat default photo");
                    assert response.getType().equals("image/jpeg");
                    assert response.getPhoto().equals("base64ImageData");
                });

        verify(photoService).getPetPhotoByPetId(petId);
    }

    @Test
    void whenGetPetPhotoByPetId_withInvalidUUID_thenReturnUnprocessableEntity() {
        String invalidPetId = "invalid-uuid";

        webTestClient.get()
                .uri("/pet/{petId}/photo", invalidPetId)
                .exchange()
                .expectStatus().isEqualTo(422);

        verify(photoService, never()).getPetPhotoByPetId(invalidPetId);
    }

    @Test
    void whenGetPetPhotoByPetId_withNonExistentPetId_thenReturnNotFound() {
        String petId = "12345678-1234-1234-1234-123456789999";

        when(photoService.getPetPhotoByPetId(petId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/pet/{petId}/photo", petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(photoService).getPetPhotoByPetId(petId);
    }

    private Pet buildPet() {
        return Pet.builder()
                .petId("a-very-valid-pet-id")
                .name("Cookie")
                .ownerId("a-very-valid-owner-id")
                .petTypeId("1")
                .birthDate(new Date())
                .isActive("true")
                .build();
    }
}