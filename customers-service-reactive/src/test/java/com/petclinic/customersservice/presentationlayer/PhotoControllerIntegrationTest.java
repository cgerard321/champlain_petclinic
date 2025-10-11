package com.petclinic.customersservice.presentationlayer;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;



@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port= 27018"})
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

    @Test
    public void getPhotoByPhotoIdNotFound() {

        Photo photo = buildPhoto();

        String PHOTO_ID_NOT_FOUND = "abc123";

        Publisher<Photo> setup = photoRepo.deleteAll().thenMany(photoRepo.save(photo));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/photos/" + PHOTO_ID_NOT_FOUND)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deletePhotoById() {

        Photo photo = buildPhoto();

        photoRepo.save(photo);

        Publisher<Void> setup = photoRepo.deleteById(buildPhoto().getId());

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client
                .delete()
                .uri("/photos/" + photo.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

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