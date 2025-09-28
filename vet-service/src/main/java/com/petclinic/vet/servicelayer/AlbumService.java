package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Album;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface AlbumService {
    Flux<Album> getAllAlbumsByVetId(String vetId);
    Mono<Void> deleteAlbumPhotoById(String vetId, Integer Id);


    Mono<Album> insertAlbumPhoto(String vetId, String photoName, MultipartFile file);
    Mono<Album> insertAlbumPhoto(String vetId, String photoName, byte[] fileData);
}
