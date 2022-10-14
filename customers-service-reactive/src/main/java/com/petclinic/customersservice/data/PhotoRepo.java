package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PhotoRepo extends ReactiveMongoRepository<Photo, String> {

    Mono<Photo> findPhotoById(int photoId);
    Mono<Photo> findPhotoByName(String name);
    Mono<Boolean> existsById(String id);

    Flux<Photo> findAll();

}
