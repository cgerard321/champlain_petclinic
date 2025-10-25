package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=27019"})
@AutoConfigureWebTestClient
class PetServiceImplTest {

    @MockBean
    private PetRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Autowired
    private PetService petService;

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
        when(repo.save(argThat(pet -> pet.getPhotoId() == null))).thenAnswer(invocation -> Mono.just(savedPetWithoutPhoto));
        when(filesServiceClient.deleteFile(PHOTO_ID)).thenAnswer(invocation -> Mono.empty());
        Mono<PetResponseDTO> result = petService.deletePetPhoto(PET_ID);
        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(PET_ID, response.getPetId());
                    assertNull(response.getPhoto());
                })
                .verifyComplete();
        verify(repo).findPetByPetId(PET_ID);
        verify(repo).save(argThat(pet -> pet.getPhotoId() == null));
        verify(filesServiceClient).deleteFile(PHOTO_ID);
    }

    @Test
    void deletePetPhoto_WithNonExistentPet_ShouldThrowNotFoundException() {
        String NON_EXISTENT_ID = "non-existent-id";
        when(repo.findPetByPetId(NON_EXISTENT_ID)).thenAnswer(invocation -> Mono.empty());
        Mono<PetResponseDTO> result = petService.deletePetPhoto(NON_EXISTENT_ID);
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof NotFoundException &&
                        e.getMessage().contains("Pet not found"))
                .verify();
        verify(repo).findPetByPetId(NON_EXISTENT_ID);
        verify(repo, never()).save(any(Pet.class));
        verify(filesServiceClient, never()).deleteFile(anyString());
    }

    @Test
    void addPetPhoto_WithValidPet_ShouldSucceed() {
        String PET_ID = "pet-123";
        Pet existingPet = buildPet();
        existingPet.setPetId(PET_ID);
        existingPet.setPhotoId(null);
        byte[] data = "fakeimagedata".getBytes();
        FileRequestDTO fileRequest = new FileRequestDTO("dog.png", "image/png", data);
        FileResponseDTO fileResponse = new FileResponseDTO("photo-xyz", "dog.png", "image/png", data);
        when(repo.findPetByPetId(PET_ID)).thenAnswer(invocation -> Mono.just(existingPet));
        when(filesServiceClient.addFile(fileRequest)).thenAnswer(invocation -> Mono.just(fileResponse));
        when(repo.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet p = invocation.getArgument(0);
            p.setPhotoId("photo-xyz");
            return Mono.just(p);
        });
        Mono<PetResponseDTO> result = petService.addPetPhoto(PET_ID, fileRequest);
        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals(PET_ID, response.getPetId());
                    assertNotNull(response.getPhoto());
                    assertEquals("photo-xyz", response.getPhoto().getFileId());
                })
                .verifyComplete();
        verify(repo).findPetByPetId(PET_ID);
        verify(filesServiceClient).addFile(fileRequest);
        verify(repo).save(any(Pet.class));
    }

    @Test
    void addPetPhoto_WithNonExistentPet_ShouldThrowNotFoundException() {
        String PET_ID = "unknown-id";
        byte[] data = "abc".getBytes();
        FileRequestDTO fileRequest = new FileRequestDTO("photo.png", "image/png", data);
        when(repo.findPetByPetId(PET_ID)).thenAnswer(invocation -> Mono.empty());
        Mono<PetResponseDTO> result = petService.addPetPhoto(PET_ID, fileRequest);
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof NotFoundException &&
                        e.getMessage().contains("Pet not found"))
                .verify();
        verify(repo).findPetByPetId(PET_ID);
        verify(filesServiceClient, never()).addFile(any());
        verify(repo, never()).save(any());
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
                .build();
    }
}
