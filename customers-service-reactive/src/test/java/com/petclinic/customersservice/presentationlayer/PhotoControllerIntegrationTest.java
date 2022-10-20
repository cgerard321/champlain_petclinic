package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static reactor.core.publisher.Mono.just;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;



@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27018"})
@AutoConfigureWebTestClient
class PhotoControllerIntegrationTest {

    @Autowired
    WebTestClient client;

    @Autowired
    PhotoRepo photoRepo;

//    @Test
//    void insertPhoto() {
//
//        Photo photo = buildPhoto();
//
//        Publisher<Photo> setup = photoRepo.deleteAll().thenMany(photoRepo.save(photo));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        client.post()
//                .uri("/photos")
//                .body(Mono.just(photo), Photo.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(photo.getName())
//                .jsonPath("$.type").isEqualTo(photo.getType())
//                .jsonPath("$.photo").isEqualTo(photo.getPhoto());
//
//                client.get()
//                .uri("/photos/" + photo.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//
//    }

    @Test
    public void getPhotoByPhotoId() {

        Photo photo = buildPhoto();

        Publisher<Photo> setup = photoRepo.deleteAll().thenMany(photoRepo.save(photo));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/photos/" + photo.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(photo.getName())
                .jsonPath("$.type").isEqualTo(photo.getType())
                .jsonPath("$.photo").isEqualTo(photo.getPhoto());

    }

//    @Test
//    public void getPhotoByPhotoIdNotFound() {
//
//        Photo photo = buildPhoto();
//
//        int PHOTO_ID_NOT_FOUND = 00;
//
//        Publisher<Photo> setup = photoRepo.deleteAll().thenMany(photoRepo.save(photo));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        client
//                .get()
//                .uri("/photos/" + PHOTO_ID_NOT_FOUND)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNotFound();
//    }

    private Photo buildPhoto() {
        final String test = "Test photo";
        return Photo.builder()
                .id("2")
                .name("test photo")
                .type("jpeg")
                .photo(test)
                .build();
    }
}