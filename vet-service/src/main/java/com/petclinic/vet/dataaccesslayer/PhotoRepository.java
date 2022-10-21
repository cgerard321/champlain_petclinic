package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
    Mono<Photo> findPhotoById(int photoId);
   //Mono<Photo> findPhotoByName(String name);
    Mono<Boolean> existsById(int id);

    //Mono<Photo> deletePhoto(int photoId);
}
