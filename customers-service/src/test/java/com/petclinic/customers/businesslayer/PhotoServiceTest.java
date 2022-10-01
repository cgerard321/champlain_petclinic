package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
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

    @Autowired
    PhotoService photoService;

    @Autowired
    OwnerService ownerService;

//    @BeforeEach
//    void setup(){
//
//        when(photoRepository.save(Photo.builder().build()));
//
//    }

    @Test
    void setPhotoOwner() {
        Owner owner = buildOwner();
        Photo photo = buildPhoto();

        when(ownerRepository.findOwnerById(1)).thenReturn(owner);

        Owner owner1 = ownerService.findByOwnerId(1).get();


        verify(photoRepository.findPhotoById(2).equals(photo.getId()));
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