package com.petclinic.customersservice.business;

import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27017"})
@AutoConfigureWebTestClient
class PhotoServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PhotoService photoService;

    @MockBean
    private PhotoRepo photoRepo;


    @Test
    void setOwnerPhoto() {

        Owner owner = buildOwner();

        Photo photo = buildPhoto();

        when(photoRepo.save(photo)).thenReturn(Mono.just(photo));

        Mono<Photo> photoMono = photoService.setOwnerPhoto(Mono.just(photo), owner.getId());

        StepVerifier
                .create(photoMono)
                .consumeNextWith(foundPhoto -> {
                    assertEquals(photo.getId(), foundPhoto.getId());
                    assertEquals(photo.getName(), foundPhoto.getName());
                    assertEquals(photo.getType(), foundPhoto.getType());
                    //assertEquals(photo.getPhoto(), foundPhoto.getPhoto());
                })
                .verifyComplete();
    }
//
//    @Test
//    void setPetPhoto() {
//
//        Pet pet = buildPet();
//
//        Photo photo = buildPhoto();
//
//        when(photoRepo.save(photo)).thenReturn(photo);
//
//        Mono<Photo> photoMono = photoService.setOwnerPhoto(Mono.just(photo), owner.getId());
//
//        StepVerifier
//                .create(photoMono)
//                .consumeNextWith(foundPhoto -> {
//                    assertEquals(photo.getId(), foundPhoto.getId());
//                    assertEquals(photo.getName(), foundPhoto.getName());
//                    assertEquals(photo.getType(), foundPhoto.getType());
//                    //assertEquals(photo.getPhoto(), foundPhoto.getPhoto());
//                })
//                .verifyComplete();
//    }


    private Owner buildOwner() {
        return Owner.builder()
                .id(1)
                .firstName("James")
                .lastName("Bond")
                .address("789 Parkland View")
                .city("London")
                .telephone("9998887777")
                .photoId(1)
                .build();
    }

    private Photo buildPhoto() {
        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
        return Photo.builder()
                .id("2")
                .name("test photo")
                .type("jpeg")
                .photo(test)
                .build();
    }
}