package com.petclinic.customersservice.datalayer;

import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
@DataMongoTest
class PhotoRepoTest {

    @Autowired
    private PhotoRepo photoRepo;

    @Test
    public void findPhotoByPhotoId() {

        Photo photo = buildPhoto();

        Publisher<Photo> setup = photoRepo.deleteAll().thenMany(photoRepo.save(photo));
        Publisher<Photo> find = photoRepo.findPhotoById(photo.getPhoto());

        StepVerifier
                .create(setup)
                .expectNext(photo)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(0)
                .verifyComplete();
    }

    private Photo buildPhoto() {
        final String test = "Test photo";
        return Photo.builder()
                .id("4")
                .name("test photo")
                .type("jpeg")
                .photo(test)
                .build();
    }

}