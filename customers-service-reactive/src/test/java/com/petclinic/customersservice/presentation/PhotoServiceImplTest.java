package com.petclinic.customersservice.presentation;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27019"})
@AutoConfigureWebTestClient
public class PhotoServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PhotoService photoService;

    @MockBean
    private PhotoRepo repo;

    private static final byte[] photoByte = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
            0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
            0x30, 0x30, (byte)0x9d };

    @Test
    void insertPhoto() {
        Photo photoEntity = buildPhoto();
        Mono<Photo> photoMono = Mono.just(photoEntity);
        when(repo.insert(any(Photo.class))).thenReturn(photoMono);
        StepVerifier
                .create(photoMono)
                .consumeNextWith(foundPhoto -> {
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
                .photo(photoByte)
                .build();
    }

}
