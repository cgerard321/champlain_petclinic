package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
class OwnerResourceIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PhotoRepository photoRepository;

    @Autowired
    OwnerRepository ownerRepository;

    private final String test = "Test byte";
    private final byte[] testBytes = test.getBytes();

    @Test
    void createPhoto(){
        Photo photo = new Photo();
        photo.setId(2);
        photo.setName("Test photo");
        photo.setType("jpeg");
        photo.setPhoto(testBytes);

        Owner owner = new Owner();
        owner.setId(1);
        owner.setTelephone("1234567890");
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setCity("MTL");
        owner.setAddress("9 rue des oiseaux");
        owner.setImageId(1);

        when(photoRepository.save(photo))
                .thenReturn(photo);


        webTestClient.post()
                .uri("/owners/photo/{ownerId}")
                .body(Mono.just(photo), Photo.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        owner.setImageId(2);

//        assertEquals(photo.getId(),2);
//        assertEquals(photo.getName(),"Test photo");
//        assertEquals(photo.getType(),"jpeg");
//        assertEquals(photo.getPhoto(),testBytes);
        ///need to assert body received is confirmation string

        //ownerRepository.getOne(1);

    }

    @Test
    public void getPhotoById(){

        Photo photo = new Photo();

        photo.setId(2);
        photo.setName("Test photo");
        photo.setType("jpeg");
        photo.setPhoto(testBytes);

        when(photoRepository.save(photo))
                .thenReturn(photo);

        webTestClient.get()
                .uri("/api/gateway/owners/{photoId}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.photoId").isEqualTo(2)
                .jsonPath("$.name").isEqualTo(photo.getName())
                .jsonPath("$.type").isEqualTo(photo.getType())
                .jsonPath("$.photo").isEqualTo(photo.getPhoto());

//        assertEquals(photo.getId(), 2); not necessary


    }

}