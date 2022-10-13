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

    private static final byte[] photoByte = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
            0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
            0x30, 0x30, (byte)0x9d };

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

    private Photo buildPhoto() {
        return Photo.builder()
                .id(5)
                .name("Test")
                .type("test2")
                .photo(photoByte)
                .build();
    }

}
