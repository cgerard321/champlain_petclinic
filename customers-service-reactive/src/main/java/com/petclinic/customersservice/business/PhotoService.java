package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhotoService {

    Flux<Photo> getAll();
    Mono<Photo> setOwnerPhoto(Mono<Photo> PhotoMono, int ownerId);
    Mono<Photo> insertPhoto(Mono<Photo> PhotoMono);

    Mono<Photo> getPhotoByPhotoId(String photoId);
    Mono<Photo> setPetPhoto(Mono<Photo> PhotoMono, int petId);
    Mono<Photo> getOwnerPhoto(int ownerId);
    Mono<Photo> getPetPhoto(int petId);
    Mono<Void> deletePhoto(int photoId);

}
