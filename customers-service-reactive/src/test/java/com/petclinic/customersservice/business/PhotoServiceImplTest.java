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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest
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

    @Test
    void getPhotoByPhotoId() {

        Photo photoEntity = buildPhoto();
        int PHOTO_ID = photoEntity.getId();

        when(repo.findPhotoById(any(Integer.class))).thenReturn(Mono.just(photoEntity));

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

        int PHOTO_ID_NOT_FOUND = 00;

        when(repo.findPhotoById(any(Integer.class))).thenReturn(Mono.just(photo));

        Mono<Photo> photoMono = photoService.getPhotoByPhotoId(PHOTO_ID_NOT_FOUND);

        StepVerifier
                .create(photoMono)
                .expectNextCount(1)
                .expectError();
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
