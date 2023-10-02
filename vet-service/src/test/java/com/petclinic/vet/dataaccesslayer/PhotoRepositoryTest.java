/*
package com.petclinic.vet.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
class PhotoRepositoryTest {
    @Autowired
    private PhotoRepository photoRepository;

    //To counter missing bean error
    */
/*@MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;*//*

    */
/*@MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;*//*


    byte[] byteArray = {12, 32, 23};
    Photo photo1 = Photo.builder()
            .id(1)
            .vetId("123")
            .filename("vet_default.jpg")
            .imgType("jpeg")
            .data(byteArray)
            .build();

    @BeforeEach
    public void setupDB(){
        Publisher<Photo> setup = photoRepository.deleteAll()
                .thenMany(photoRepository.save(photo1));

        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findPhotoByVetId_ValidId_ShouldSucceed() {
        //arrange
        Mono<Photo> addedPhoto = photoRepository.findByVetId(photo1.getVetId());
        StepVerifier
                .create(addedPhoto)
                .consumeNextWith(foundPhoto -> {
                    assertNotNull(foundPhoto);
                    assertEquals(photo1.getId(), foundPhoto.getId());
                    assertEquals(photo1.getVetId(), foundPhoto.getVetId());
                    assertEquals(photo1.getFilename(), foundPhoto.getFilename());
                    assertEquals(photo1.getImgType(), foundPhoto.getImgType());
                    assertEquals(photo1.getData(), foundPhoto.getData());
                })
                .verifyComplete();
    }
    @Test
    void findPhotoByVetId_InvalidId_ShouldBeEmpty() {
        //arrange
        String invalidId = "ifiqugf";
        StepVerifier
                .create(photoRepository.findByVetId(invalidId))
                .expectNextCount(0)
                .verifyComplete();
    }
}
*/
