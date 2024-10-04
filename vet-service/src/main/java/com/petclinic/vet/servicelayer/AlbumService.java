package com.petclinic.vet.servicelayer;

import reactor.core.publisher.Flux;

import org.springframework.core.io.Resource;

public interface AlbumService {
    Flux<Resource> getAllPhotosByVetId(String vetId);
}
