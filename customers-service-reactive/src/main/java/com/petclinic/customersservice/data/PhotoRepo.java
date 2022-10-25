package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PhotoRepo extends ReactiveMongoRepository<Photo, String> {

    Mono<Photo> findPhotoByPhotoId(String photoId);
    Mono<Photo> findPhotoByName(String name);
    Mono<Boolean> existsByPhotoId(String photoId);

}
