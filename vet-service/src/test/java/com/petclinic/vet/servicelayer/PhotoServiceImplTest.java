package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
class PhotoServiceImplTest {
    @Autowired
    PhotoService photoService;

    @MockBean
    PhotoRepository photoRepository;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String VET_ID = "6748786";
    byte[] photoData = {123, 23, 75, 34};
    Photo photo = Photo.builder()
            .vetId(VET_ID)
            .filename("vet_default.jpg")
            .imgType("image/jpeg")
            .data(photoData)
            .build();

    @Test
    void getPhotoByValidVetId() {
        when(photoRepository.findByVetId(anyString())).thenReturn(Mono.just(photo));

        Mono<Resource> photoMono = photoService.getPhotoByVetId(VET_ID);

        StepVerifier
                .create(photoMono)
                .consumeNextWith(image -> {
                    assertNotNull(image);

                    Resource photo = photoMono.block();
                    assertEquals(photo, image);
                })
                .verifyComplete();
    }

    @Test
    void insertPhotoOfVet() {
        String photoName = "vet_default.jpg";
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(photoData));
        Photo savedPhoto = new Photo();
        savedPhoto.setVetId(VET_ID);
        savedPhoto.setFilename(photoName);
        savedPhoto.setImgType("image/jpeg");
        savedPhoto.setData(photoData);
        when(photoRepository.save(any(Photo.class))).thenReturn(Mono.just(savedPhoto));

        Mono<Resource> savedPhotoMono = photoService.insertPhotoOfVet(VET_ID, photoName, photoResource);

        StepVerifier
                .create(savedPhotoMono)
                .consumeNextWith(image -> {
                    assertNotNull(image);

                    Resource photo = savedPhotoMono.block();
                    assertEquals(photo, image);
                })
                .verifyComplete();
    }

}