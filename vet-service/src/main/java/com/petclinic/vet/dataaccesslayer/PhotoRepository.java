package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PhotoRepository extends ReactiveCrudRepository<Photo, Integer> {
    Mono<Photo> findByVetId(String vetId);

}
