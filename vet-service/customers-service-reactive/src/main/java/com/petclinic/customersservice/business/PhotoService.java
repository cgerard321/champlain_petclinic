package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import reactor.core.publisher.Mono;

public interface PhotoService {

    Mono<Photo> setOwnerPhoto(Mono<Photo> PhotoMono, int ownerId);
    Mono<Photo> setPetPhoto(Mono<Photo> PhotoMono, int petId);
    Mono<Photo> getOwnerPhoto(int ownerId);
    Mono<Photo> getPetPhoto(int petId);
    Mono<Void> deletePhoto(int photoId);

}
