package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.presentationlayer.PhotoResponseModel;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest
class PhotoServiceImplTest {

    @MockBean
    private PhotoRepo repo;

    @MockBean
    private PetRepo petRepo;

    @MockBean
    private PetTypeRepo petTypeRepo;

    @Autowired
    private PhotoService photoService;

    @Test
    void insertPhoto() {
        Photo photoEntity = buildPhoto();
        Mono<Photo> photoMono = Mono.just(photoEntity);
        when(repo.save(any(Photo.class))).thenReturn(photoMono);
        Mono<Photo> returnedPhoto = photoService.insertPhoto(Mono.just(photoEntity));
        StepVerifier.create(returnedPhoto).consumeNextWith(foundPhoto -> {
                    assertEquals(photoEntity.getId(), foundPhoto.getId());
                    assertEquals(photoEntity.getName(), foundPhoto.getName());
                    assertEquals(photoEntity.getType(), foundPhoto.getType());
                    assertEquals(photoEntity.getPhoto(), foundPhoto.getPhoto());
                })
                .verifyComplete();
    }

    @Test
    void getPhotoByPhotoId() {

        Photo photoEntity = buildPhoto();
        String PHOTO_ID = photoEntity.getId();

        when(repo.findById(any(String.class))).thenReturn(Mono.just(photoEntity));

        Mono<Photo> returnedPhoto = photoService.getPhotoByPhotoId(PHOTO_ID);

        StepVerifier.create(returnedPhoto)
                .consumeNextWith(foundPhoto -> {
                    assertEquals(photoEntity.getName(), foundPhoto.getName());
                    assertEquals(photoEntity.getType(), foundPhoto.getType());
                    assertEquals(photoEntity.getPhoto(), foundPhoto.getPhoto());
                })
                .verifyComplete();
    }

    @Test
    public void getPhotoByPhotoIdNotFound() {

        Photo photo = buildPhoto();

        String PHOTO_ID_NOT_FOUND = "00";

        when(repo.findById(any(String.class))).thenReturn(Mono.just(photo));

        Mono<Photo> photoMono = photoService.getPhotoByPhotoId(PHOTO_ID_NOT_FOUND);

        StepVerifier
                .create(photoMono)
                .expectNextCount(1)
                .expectError();
    }


    @Test
    void getPetPhotoByPetId_WithDefaultPhoto() {
        String petId = "pet-123";
        String petTypeId = "1";
        Pet pet = buildPet(petId, null);
        pet.setPetTypeId(petTypeId);
        PetType petType = buildPetType(petTypeId, "Cat");
        
        when(petRepo.findPetByPetId(petId)).thenReturn(Mono.just(pet));
        when(petTypeRepo.findPetTypeById(petTypeId)).thenReturn(Mono.just(petType));
        
        Mono<PhotoResponseModel> result = photoService.getPetPhotoByPetId(petId);
        
        StepVerifier.create(result)
                .consumeNextWith(photoResponse -> {
                    assertEquals(petTypeId, photoResponse.getId());
                    assertEquals("Cat default photo", photoResponse.getName());
                    assertEquals("image/jpeg", photoResponse.getType());
                    assertNotNull(photoResponse.getPhoto());
                })
                .verifyComplete();
    }

    @Test
    void getPetPhotoByPetId_PetNotFound() {
        String petId = "non-existent-pet";
        
        when(petRepo.findPetByPetId(petId)).thenReturn(Mono.empty());
        
        Mono<PhotoResponseModel> result = photoService.getPetPhotoByPetId(petId);
        
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getPetPhotoByPetId_CustomPhotoNotFound_FallbackToDefault() {
        String petId = "pet-123";
        String photoId = "non-existent-photo";
        String petTypeId = "2";
        Pet pet = buildPet(petId, photoId);
        pet.setPetTypeId(petTypeId);
        PetType petType = buildPetType(petTypeId, "Dog");
        
        when(petRepo.findPetByPetId(petId)).thenReturn(Mono.just(pet));
        when(repo.findById(photoId)).thenReturn(Mono.empty());
        when(petTypeRepo.findPetTypeById(petTypeId)).thenReturn(Mono.just(petType));
        
        Mono<PhotoResponseModel> result = photoService.getPetPhotoByPetId(petId);
        
        StepVerifier.create(result)
                .consumeNextWith(photoResponse -> {
                    assertEquals(petTypeId, photoResponse.getId());
                    assertEquals("Dog default photo", photoResponse.getName());
                    assertEquals("image/jpeg", photoResponse.getType());
                    assertNotNull(photoResponse.getPhoto());
                })
                .verifyComplete();
    }

    @Test
    void deletePhotoByPhotoId() {
        String photoId = "photo-123";
        
        when(repo.deleteById(photoId)).thenReturn(Mono.empty());
        
        Mono<Void> result = photoService.deletePhotoByPhotoId(photoId);
        
        StepVerifier.create(result)
                .verifyComplete();
    }

    private Photo buildPhoto() {
        return Photo.builder()
                .id("5")
                .name("Test")
                .type("test2")
                .photo("photoString")
                .build();
    }

    private Pet buildPet(String petId, String photoId) {
        return Pet.builder()
                .petId(petId)
                .name("Test Pet")
                .ownerId("owner-123")
                .petTypeId("1")
                .photoId(photoId)
                .isActive("true")
                .build();
    }

    private PetType buildPetType(String petTypeId, String name) {
        return PetType.builder()
                .petTypeId(petTypeId)
                .name(name)
                .petTypeDescription(name + " description")
                .build();
    }

}
