package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Album;
import reactor.core.publisher.Flux;

import org.springframework.core.io.Resource;

public interface AlbumService {
    Flux<Album> getAllAlbumsByVetId(String vetId);
}
