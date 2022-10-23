package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhotoService {

    Mono<Photo> insertPhoto(Mono<Photo> PhotoMono);

    Mono<Photo> getPhotoByPhotoId(String photoId);

    Mono<Void> deletePhotoByPhotoId(String photoId);

}
