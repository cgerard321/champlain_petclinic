package com.petclinic.vet.businesslayer.albums;

import reactor.core.publisher.Flux;
import org.springframework.http.codec.multipart.FilePart;

import com.petclinic.vet.dataaccesslayer.albums.Album;

import reactor.core.publisher.Mono;

public interface AlbumService {
  Flux<Album> getAllAlbumsByVetId(String vetId);
  Mono<Void> deleteAlbumPhotoById(String vetId, Integer Id);
  Mono<Album> insertAlbumPhoto(String vetId, String photoName, Mono<byte[]> fileData);
  Mono<Album> insertAlbumPhoto(String vetId, String photoName, FilePart file);
}
