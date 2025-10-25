package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PetServiceUnitTest {

    @Mock
    private PetRepo repo;

    @Mock
    private OwnerService ownerService;

    @Mock
    private FilesServiceClient filesServiceClient;

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

    @Test
    void whenCreatePetForOwner_withValidOwnerAndPetRequest_thenReturnPetResponseDTO() {
        String ownerId = "valid-owner-id";
        PetRequestDTO petRequest = buildPetRequestDTO();
        Pet savedPet = buildPetFromRequest(petRequest, ownerId);
        OwnerResponseDTO ownerResponse = buildOwnerResponseDTO();

        when(ownerService.getOwnerByOwnerId(ownerId, false)).thenReturn(Mono.just(ownerResponse));
        when(repo.save(any(Pet.class))).thenReturn(Mono.just(savedPet));

        Mono<PetResponseDTO> result = petService.createPetForOwner(ownerId, Mono.just(petRequest));

        StepVerifier
                .create(result)
                .consumeNextWith(createdPet -> {
                    assertEquals(savedPet.getName(), createdPet.getName());
                    assertEquals(savedPet.getPetTypeId(), createdPet.getPetTypeId());
                    assertEquals(savedPet.getOwnerId(), createdPet.getOwnerId());
                    assertEquals(savedPet.getWeight(), createdPet.getWeight());
                    assertEquals("true", createdPet.getIsActive());
                })
                .verifyComplete();
    }

    @Test
    void whenCreatePetForOwner_withNonExistingOwner_thenReturnNotFoundException() {
        String nonExistingOwnerId = "non-existent-owner-id";
        PetRequestDTO petRequest = buildPetRequestDTO();

        when(ownerService.getOwnerByOwnerId(nonExistingOwnerId,false)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.createPetForOwner(nonExistingOwnerId, Mono.just(petRequest));

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Owner not found with id: " + nonExistingOwnerId))
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

    private PetRequestDTO buildPetRequestDTO() {
        return PetRequestDTO.builder()
                .name("Buddy")
                .petTypeId("2")
                .birthDate(new Date())
                .weight("15.5")
                .isActive("true")
                .build();
    }

    private Pet buildPetFromRequest(PetRequestDTO request, String ownerId) {
        return Pet.builder()
                .petId("generated-pet-id")
                .name(request.getName())
                .ownerId(ownerId)
                .petTypeId(request.getPetTypeId())
                .birthDate(request.getBirthDate())
                .weight(request.getWeight())
                .isActive("true")
                .build();
    }

    private OwnerResponseDTO buildOwnerResponseDTO() {
        return OwnerResponseDTO.builder()
                .ownerId("valid-owner-id")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .city("Test City")
                .province("Test Province")
                .telephone("555-1234")
                .build();
    }
}