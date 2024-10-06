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