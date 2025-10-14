package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.albums.AlbumService;
import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.dataaccesslayer.albums.AlbumRepository;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class AlbumServiceImplTest {

    @Autowired
    AlbumService albumService;

    @MockBean
    AlbumRepository albumRepository;

    // To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String VET_ID = "ac9adeb8-625b-11ee-8c99-0242ac120002";
    byte[] albumData = {123, 23, 75, 34};
    Album album = Album.builder()
            .vetId(VET_ID)
            .filename("album1.jpg")
            .imgType("image/jpeg")
            .data(albumData)
            .build();

    @Test
    void getAllAlbumsByValidVetId() {
        Album album1 = new Album(1, VET_ID, "album1.jpg", "image/jpeg", albumData);
        Album album2 = new Album(2, VET_ID, "album2.jpg", "image/jpeg", albumData);

        when(albumRepository.findAllByVetId(anyString())).thenReturn(Flux.just(album1, album2));

        Flux<Album> albumFlux = albumService.getAllAlbumsByVetId(VET_ID);

        StepVerifier
                .create(albumFlux)
                .expectNext(album1)
                .expectNext(album2)
                .verifyComplete();
    }

    @Test
    void getAllAlbumsByInvalidVetId_thenThrowNotFoundException() {
        when(albumRepository.findAllByVetId(anyString())).thenReturn(Flux.empty());

        Flux<Album> albumFlux = albumService.getAllAlbumsByVetId(VET_ID);

        StepVerifier
                .create(albumFlux)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No albums found for vet " + VET_ID))
                .verify();
    }

    @Test
    void getAllAlbumsByVetId_withError() {

        when(albumRepository.findAllByVetId(anyString())).thenReturn(Flux.error(new RuntimeException("Some error occurred")));

        Flux<Album> result = albumService.getAllAlbumsByVetId(VET_ID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Some error occurred"))
                .verify();
    }
    @Test
    void deleteAlbumPhotoById_whenAlbumExists_shouldSucceed() {
        Mockito.when(albumRepository.findById(album.getId())).thenReturn(Mono.just(album));
        Mockito.when(albumRepository.delete(album)).thenReturn(Mono.empty());

        Mono<Void> result = albumService.deleteAlbumPhotoById(VET_ID, album.getId());

        StepVerifier.create(result)
                .verifyComplete();

        verify(albumRepository, times(1)).findById(album.getId());
        verify(albumRepository, times(1)).delete(album);
    }

    @Test
    void deleteAlbumPhotoById_whenAlbumDoesNotExist_shouldThrowNotFoundException() {
        Mockito.when(albumRepository.findById(album.getId())).thenReturn(Mono.empty());

        Mono<Void> result = albumService.deleteAlbumPhotoById(VET_ID, album.getId());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Album photo not found: " + album.getId()))
                .verify();

        verify(albumRepository, times(1)).findById(album.getId());
        verify(albumRepository, times(0)).delete(album);
    }

}
