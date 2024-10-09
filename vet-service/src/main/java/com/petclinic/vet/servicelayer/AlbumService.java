package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Album;
import reactor.core.publisher.Flux;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface AlbumService {
    Flux<Album> getAllAlbumsByVetId(String vetId);
    Mono<Void> deleteAlbumPhotoById(String vetId, Integer Id);
}
