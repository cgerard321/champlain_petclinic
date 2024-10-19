package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PetServiceUnitTest {

    @Mock
    private PetRepo repo;

    @InjectMocks
    private PetServiceImpl petService;

    @Test
    void whenDeletePetByPetIdV2_withExistingPetId_thenReturnPetResponseDTO() {
        Pet pet = buildPet();

        when(repo.findPetByPetId(pet.getPetId())).thenReturn(Mono.just(pet));
        when(repo.delete(pet)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.deletePetByPetIdV2(pet.getPetId());

        StepVerifier
                .create(result)
                .consumeNextWith(deletedPet -> {
                    assertEquals(pet.getPetId(), deletedPet.getPetId());
                    assertEquals(pet.getName(), deletedPet.getName());
                    assertEquals(pet.getPetTypeId(), deletedPet.getPetTypeId());
                    assertEquals(pet.getOwnerId(), deletedPet.getOwnerId());
                    assertEquals(pet.getBirthDate(), deletedPet.getBirthDate());
                })
                .verifyComplete();
    }

    @Test
    void whenDeletePetByPetIdV2_withNonExistingPetId_thenReturnNotFoundException() {
        String nonExistingPetId = "non-existent-id";

        when(repo.findPetByPetId(nonExistingPetId)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.deletePetByPetIdV2(nonExistingPetId);

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Pet id not found: " + nonExistingPetId))
                .verify();
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