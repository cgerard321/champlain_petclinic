package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.*;
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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

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

        when(photoRepository.save(photo)).thenReturn(photo);

        when(ownerRepository.findOwnerById(1)).thenReturn(owner);
        int deleteId = owner.getImageId();

        when(photoRepository.findPhotoByName(photo.getName()).getId()).thenReturn(photo.getId());

        owner.setImageId(photo.getId());

        when(ownerRepository.save(owner)).thenReturn(owner);

//        String response =;
//
//
////        if (deleteId !=1) {
////            this.deletePhoto();
////        }
//
//        assertThat(response).isEqualTo("Image uploaded successfully: " + photo.getName());



    }

    @Test
    void setPetPhoto() {


    }

    @Test
    void getOwnerPhoto() {

        Owner owner = buildOwner();
        Photo photo = buildPhoto();

        when(ownerRepository.findOwnerById(owner.getId())).thenReturn(owner);
        when(photoRepository.findPhotoById(owner.getImageId())).thenReturn(photo);

        Photo returnedPhoto = photoService.getOwnerPhoto(1);

        assertThat(returnedPhoto.getId()).isEqualTo(photo.getId());
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

    }

    private Owner buildOwner() {
        final String test = "Test photo";
        final byte[] testBytes = test.getBytes();
        return Owner.builder()
                .id(1)
                .firstName("John")
                .lastName("Smith")
                .address("534 Oak Road")
                .city("Brossard")
                .telephone("5554443333")
                .imageId(2)
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