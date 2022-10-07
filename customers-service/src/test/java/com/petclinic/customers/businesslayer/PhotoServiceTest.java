package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.*;
import com.petclinic.customers.presentationlayer.PetRequest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PhotoServiceTest {

    @MockBean
    PhotoRepository photoRepository;
    @MockBean
    OwnerRepository ownerRepository;
    @MockBean
    PetRepository petRepository;
    @Autowired
    PhotoService photoService;

    @Autowired
    OwnerService ownerService;

    @Autowired
    PetService petService;

    public Pet setupPet() {

        Owner owner = buildOwner();

        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    @Test
    void setOwnerPhoto() {
        Owner owner = buildOwner();
        Photo photo = buildPhoto();

        when(ownerRepository.findOwnerById(1)).thenReturn(owner);
        when(photoRepository.findPhotoByName(photo.getName())).thenReturn(photo);

        ownerService.createOwner(owner);
        photoService.setOwnerPhoto(photo,owner.getId());

        final String photoResult = photoService.setOwnerPhoto(photo,owner.getId());
        assertEquals("Image uploaded successfully: " + photo.getName(), photoResult);

    }

    @Test
    void setPetPhoto() {
        PetRequest petRequest = buildPetRequest();
        Pet pet = buildPet();
        Owner owner = buildOwner();
        Photo photo = buildPhoto();

        when(ownerRepository.findOwnerById(1)).thenReturn(owner);
        when(petRepository.findPetById(1)).thenReturn(pet);
        when(photoRepository.findPhotoByName(photo.getName())).thenReturn(photo);

        ownerService.createOwner(owner);
        //petService.CreatePet(petRequest,owner.getId());
        photoService.setPetPhoto(photo,pet.getId());

        final String photoResult = photoService.setPetPhoto(photo,pet.getId());
        assertEquals("Image uploaded successfully: " + photo.getName(), photoResult);

    }

    @Test
    void getOwnerPhoto() {

        Owner owner = buildOwner();
        Photo photo = buildPhoto();

        when(ownerRepository.findOwnerById(owner.getId())).thenReturn(owner);
        when(photoRepository.findPhotoById(owner.getImageId())).thenReturn(photo);

        Photo returnedPhoto = photoService.getOwnerPhoto(1);

        assertThat(returnedPhoto.getName()).isEqualTo(photo.getName());
    }


    @Test
    void getPetPhoto() {

        Pet pet = setupPet();
        Photo photo = buildPhoto();

        when(petRepository.findPetById(pet.getId())).thenReturn(pet);
        when(photoRepository.findPhotoById(pet.getImageId())).thenReturn(photo);

        Photo returnedPhoto = photoService.getPetPhoto(2);

        assertThat(returnedPhoto.getId()).isEqualTo(photo.getId());

    }

    @Test
    void deletePhoto() {

        Photo photo = buildPhoto();

        when(photoRepository.findPhotoById(photo.getId())).thenReturn(photo);

        photoService.deletePhoto(photo.getId());

        verify(photoRepository,times(1)).delete(photo);

    }

    private Owner buildOwner() {
            return Owner.builder()
                .id(1)
                .firstName("John")
                .lastName("Smith")
                .address("534 Oak Road")
                .city("Brossard")
                .telephone("5554443333")
                .imageId(1)
                .build();
    }

    private PetRequest buildPetRequest() {
        return PetRequest.builder()
                .id(1)
                .name("Joe")
                .birthDate(new Date())
                .build();
    }

    private Pet buildPet() {
        return Pet.builder()
                .id(1)
                .name("Joe")
                .birthDate(new Date())
                .imageId(1)
                .build();
    }


    private Photo buildPhoto(){
        final String test = "Test photo";
        final byte[] testBytes = test.getBytes();
        return Photo.builder()
                .id(2)
                .name("test photo")
                .type("jpeg")
                .photo(testBytes)
                .build();
    }
}