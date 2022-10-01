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

//    @BeforeEach
//    void setup(){
//
//        when(photoRepository.save(Photo.builder().build()));
//
//    }

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
    void setPhotoPet() {
    }

    @Test
    void getPhotoOwner() {

        Owner owner = buildOwner();
        Photo photo = buildPhoto();
        when(photoRepository.findPhotoById(ownerRepository.findOwnerById(1).getImageId())).thenReturn(photo);

        photoRepository.findPhotoById(2);

        verify(photoRepository.findPhotoById(2).equals(photo.getId()));

    }


    @Test
    void getPhotoPet() {
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