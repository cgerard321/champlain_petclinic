package com.petclinic.customersservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import org.reactivestreams.Publisher;
import org.junit.jupiter.api.Test;

@DataMongoTest
class PhotoRepoTest {

    @Autowired
    PhotoRepo repo;

    @Test
    void insertPhoto() {
        Photo photo = buildPhoto();

        Publisher<Photo> setup = repo.deleteAll().thenMany(repo.save(photo));

        StepVerifier
                .create(setup)
                .consumeNextWith(foundPhoto ->{
                    assertEquals(photo.getId(), foundPhoto.getId());
                    assertEquals(photo.getName(), foundPhoto.getName());
                    assertEquals(photo.getType(), foundPhoto.getType());
                    assertEquals(photo.getPhoto(), foundPhoto.getPhoto());
                })
                .verifyComplete();
    }

    @Test
    public void findPhotoByPhotoId() {

        Photo photo = buildPhoto();

        Publisher<Photo> setup = repo.deleteAll().thenMany(repo.save(photo));
        Publisher<Photo> find = repo.findPhotoById(photo.getId());

        StepVerifier
                .create(setup)
                .expectNext(photo)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();
    }

    private Photo buildPhoto() {
        return Photo.builder()
                .id(5)
                .name("Test")
                .type("test2")
                .photo("photo")
                .build();
    }

}
