package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27019"})
@AutoConfigureWebTestClient
class PhotoServiceImplTest {

    @MockBean
    private PhotoRepo repo;

    @Autowired
    private PhotoService photoService;

    @Test
    void insertPhoto() {
        Photo photoEntity = buildPhoto();
        Mono<Photo> photoMono = Mono.just(photoEntity);
        when(repo.insert(any(Photo.class))).thenReturn(photoMono);
        Mono<Photo> returnedPhoto = photoService.insertPhoto(Mono.just(photoEntity));
        StepVerifier.create(returnedPhoto).consumeNextWith(foundPhoto -> {
                    assertEquals(photoEntity.getId(), foundPhoto.getId());
                    assertEquals(photoEntity.getName(), foundPhoto.getName());
                    assertEquals(photoEntity.getType(), foundPhoto.getType());
                    assertEquals(photoEntity.getPhoto(), foundPhoto.getPhoto());
                })
                .verifyComplete();
    }

    private Photo buildPhoto() {
        return Photo.builder()
                .id(5)
                .name("Test")
                .type("test2")
                .photo("photoString")
                .build();
    }

}
