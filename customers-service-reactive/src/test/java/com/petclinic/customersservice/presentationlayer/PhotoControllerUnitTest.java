package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.Photo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PhotoController.class)
class PhotoControllerUnitTest {

    private Photo photo =buildPhoto();

    private final int PHOTO_ID = photo.getId();

    private final int PHOTO_ID_NOT_FOUND = 00;

    @Autowired
    private WebTestClient client;

    @MockBean
    PhotoService photoService;



//    @Test
//    public void getPhotoByPhotoIdNotFound() {
//
//        when(photoService.getPhotoByPhotoId(anyString())).thenReturn(Mono.just(photo));
//
//        client.get()
//                .uri("/photos/" + PHOTO_ID_NOT_FOUND)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNotFound();
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(photo.getName())
//                .jsonPath("$.type").isEqualTo(photo.getType())
//                .jsonPath("$.photo").isEqualTo(photo.getPhoto());
//
//        Mockito.verify(photoService, times(1)).getPhotoByPhotoId(PHOTO_ID);
//    }




    private Photo buildPhoto() {
        final String test = "Test photo";
        return Photo.builder()
                .id(3)
                .name("test photo")
                .type("jpeg")
                .photo(test)
                .build();
    }

}