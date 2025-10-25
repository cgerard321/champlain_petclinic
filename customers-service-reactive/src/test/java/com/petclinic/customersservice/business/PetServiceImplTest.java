package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.*;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port= 27019"})
@AutoConfigureWebTestClient
class PetServiceImplTest {

    @MockBean
    private PetRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Autowired
    private PetService petService;

/*
    @Test
    void insertPet() {
        Pet petEntity = buildPet();
        Mono<Pet> petMono = Mono.just(petEntity);
        when(repo.insert(any(Pet.class))).thenReturn(petMono);
        Mono<Pet> returnedPet = petService.insertPet(Mono.just(petEntity));
        StepVerifier.create(returnedPet).consumeNextWith(foundOwner -> {
                    assertEquals(petEntity.getId(), foundOwner.getId());
                    assertEquals(petEntity.getFirstName(), foundOwner.getFirstName());
                    assertEquals(petEntity.getLastName(), foundOwner.getLastName());
                    assertEquals(petEntity.getAddress(), foundOwner.getAddress());
                    assertEquals(petEntity.getCity(), foundOwner.getCity());
                    assertEquals(petEntity.getTelephone(), foundOwner.getTelephone());
                    //assertEquals(petEntity.getPhotoId(), foundOwner.getPhotoId());
                })
                .verifyComplete();
    }


    @Test
    public void deletePet() {

        Pet pet = buildPet();
        String PET_ID = pet.getPetId();

        when(repo.deleteById(anyString())).thenReturn(Mono.empty());

        Mono<Void> petDelete = petService.deletePetByPetId(PET_ID);

        StepVerifier
                .create(petDelete)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findPetByPetId() {

        //Owner owner = buildOwner();
        Pet pet = buildPet();
        String PET_ID = pet.getId();
        when(repo.findPetByPetId(PET_ID)).thenReturn(Mono.just(pet));
        Mono<Pet> petEntity = petService.getPetById(PET_ID);

        StepVerifier
                .create(petEntity)
                .consumeNextWith(foundPet -> {
                    assertEquals(pet.getId(), foundPet.getId());
                    assertEquals(pet.getName(), foundPet.getName());
                    assertEquals(pet.getPetTypeId(), foundPet.getPetTypeId());
                    assertEquals(pet.getPhotoId(), foundPet.getPhotoId());
                    assertEquals(pet.getOwnerId(), foundPet.getOwnerId());
                    assertEquals(pet.getBirthDate(), foundPet.getBirthDate());
                })
                .verifyComplete();
    }

    @Test
    void getAllPets_ShouldSucceed() {
        PetResponseDTO petResponseDTO = PetResponseDTO.builder()
                .petId("pet-123")
                .name("Pet Name")
                .photoId("12314151")
                .birthDate(date)
                .ownerId("ownerId-123")
                .build();

        List<PetResponseDTO> pets = new ArrayList<>();
        pets.add(petResponseDTO);

        Flux<PetResponseDTO> returnAllOwners = Flux.just(petResponseDTO);

        StepVerifier
                .create(returnAllOwners)
                .expectNextMatches(petDto -> petDto.getPetId().equals(petResponseDTO.getPetId()))
                .expectComplete()
                .verify();
    }

 */
/*
    @Test
    void findPetsByOwnerId() {
        Pet pet = buildPet();
        String OWNER_ID = pet.getOwnerId();
        when(repo.findAllPetByOwnerId(anyString())).thenReturn(Flux.just(pet));
        Flux<PetResponseDTO> petFlux = petService.getPetsByOwnerId(OWNER_ID);
        StepVerifier
                .create(petFlux)
                .consumeNextWith(foundPet -> {
                    assertEquals(pet.getPetId(), foundPet.getPetId());
                    assertEquals(pet.getName(), foundPet.getName());
                    assertEquals(pet.getPetTypeId(), foundPet.getPetTypeId());
                    assertEquals(pet.getPhotoId(), foundPet.getPhotoId());
                    assertEquals(pet.getOwnerId(), foundPet.getOwnerId());
                    assertEquals(pet.getBirthDate(), foundPet.getBirthDate());
                    assertEquals(pet.getIsActive(), foundPet.getIsActive());
                })
                .verifyComplete();
    }

    @Test
    void getPetByIdNotFound() {

        Pet petEntity = buildPet();
        String PET_ID = "Not found";
        when(repo.findPetByPetId(PET_ID)).thenReturn(Mono.just(petEntity));
        Mono<Pet> petMono = petService.getPetById(PET_ID);
        StepVerifier
                .create(petMono)
                .expectNextCount(1)
                .expectError();

    }

 */
/*
    @Test
    public void deletePetNotFound() {

        Pet pet = buildPet();
        String PET_ID = "00";

        when(repo.deleteById(anyString())).thenReturn(Mono.empty());

        Mono<Void> petDelete = petService.deletePetByPetId(PET_ID);

        StepVerifier
                .create(petDelete)
                .expectNextCount(1)
                .expectError();
    }

 */

    @Test
    void deletePetPhoto_WithExistingPhoto_ShouldSucceed() {
        String PET_ID = "petId-123";
        String PHOTO_ID = "photo-123";
        Pet existingPetWithPhoto = buildPet();
        existingPetWithPhoto.setPetId(PET_ID);
        existingPetWithPhoto.setPhotoId(PHOTO_ID);
        
        Pet savedPetWithoutPhoto = buildPet();
        savedPetWithoutPhoto.setPetId(PET_ID);
        savedPetWithoutPhoto.setPhotoId(null);

        when(repo.findPetByPetId(PET_ID)).thenReturn(Mono.just(existingPetWithPhoto));
        when(repo.save(argThat(pet -> pet.getPhotoId() == null)))
                .thenReturn(Mono.just(savedPetWithoutPhoto));
        when(filesServiceClient.deleteFile(PHOTO_ID)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.deletePetPhoto(PET_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(PET_ID, response.getPetId());
                    assertNull(response.getPhoto(), "Photo should be null in the response DTO");
                })
                .verifyComplete();

        verify(repo).findPetByPetId(PET_ID);
        verify(repo).save(argThat(pet -> pet.getPhotoId() == null));
        verify(filesServiceClient).deleteFile(PHOTO_ID);
    }

    @Test
    void deletePetPhoto_WithoutPhoto_ShouldReturnPetWithoutError() {
        String PET_ID = "petId-123";
        Pet existingPetWithoutPhoto = buildPet();
        existingPetWithoutPhoto.setPetId(PET_ID);
        existingPetWithoutPhoto.setPhotoId(null);

        when(repo.findPetByPetId(PET_ID)).thenReturn(Mono.just(existingPetWithoutPhoto));

        Mono<PetResponseDTO> result = petService.deletePetPhoto(PET_ID);

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(PET_ID, response.getPetId());
                    assertNull(response.getPhoto());
                })
                .verifyComplete();

        verify(repo).findPetByPetId(PET_ID);
        verify(repo, never()).save(any(Pet.class));
        verify(filesServiceClient, never()).deleteFile(anyString());
    }

    @Test
    void deletePetPhoto_WithNonExistentPet_ShouldThrowNotFoundException() {
        String NON_EXISTENT_ID = "non-existent-id";
        when(repo.findPetByPetId(NON_EXISTENT_ID)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.deletePetPhoto(NON_EXISTENT_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("Pet not found"))
                .verify();

        verify(repo).findPetByPetId(NON_EXISTENT_ID);
        verify(repo, never()).save(any(Pet.class));
        verify(filesServiceClient, never()).deleteFile(anyString());
    }

    Date date = new Date(20221010);

    private Pet buildPet() {
        return Pet.builder()
                .id("55")
                .petId("petId-123")
                .ownerId("ownerId-1234")
                .name("Test Pet")
                .birthDate(date)
                .petTypeId("5")
                .photoId("3")
                .isActive("true")
                .build();
    }

    private Owner buildOwner() {
        return Owner.builder()
                .id("44")
                .ownerId("ownerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }

}
