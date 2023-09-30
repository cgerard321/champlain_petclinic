package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface PhotoRepository extends ReactiveCrudRepository<Photo, String> {
    /*
    Mono<Photo> findPhotoById(int photoId);
    Mono<Photo> findPhotoByName(String name);
    Mono<Boolean> existsById(int id);
    */
    Mono<Photo> findByFilename(String filename);
    Mono<Photo> findByVetId(String vetId);

}
