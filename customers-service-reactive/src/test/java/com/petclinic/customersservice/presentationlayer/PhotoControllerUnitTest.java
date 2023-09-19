package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.presentationlayer.PhotoController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PhotoController.class)
class PhotoControllerUnitTest {

    private final Photo photo =buildPhoto();

    private final String PHOTO_ID = photo.getId();


    @Autowired
    private WebTestClient client;

    @MockBean
    PhotoService photoService;

    @Test
    void insertPhoto() {

        Photo photo = buildPhoto();

        Mono<Photo> photoMono = Mono.just(photo);

        when(photoService.insertPhoto(photoMono)).thenReturn(photoMono);

        client.post()
                .uri("/photos")
                .body(photoMono, Photo.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(photoService, times(1)).insertPhoto(any(Mono.class));
    }


    @Test
    public void getPhotoByPhotoId() {

        when(photoService.getPhotoByPhotoId(anyString())).thenReturn(Mono.just(photo));

        client.get()
                .uri("/photos/" + PHOTO_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(photo.getName())
                .jsonPath("$.type").isEqualTo(photo.getType())
                .jsonPath("$.photo").isEqualTo(photo.getPhoto());

        Mockito.verify(photoService, times(1)).getPhotoByPhotoId(PHOTO_ID);
    }

    @Test
    void deletePhotoById() {

        Photo photo = buildPhoto();

        String PHOTO_ID = photo.getId();

        when(photoService.getPhotoByPhotoId(anyString())).thenReturn(Mono.just(photo));

        client
                .delete()
                .uri("/photos/" + PHOTO_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        Mockito.verify(photoService, times(1)).deletePhotoByPhotoId(PHOTO_ID);
    }




    private Photo buildPhoto() {
        final String test = "Test photo";
        return Photo.builder()
                .id("3")
                .name("test photo")
                .type("jpeg")
                .photo(test)
                .build();
    }

}