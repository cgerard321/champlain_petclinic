package com.petclinic.vet.dataaccesslayer.photos;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

import java.util.Optional;

public interface PhotoRepository extends ReactiveCrudRepository<Photo, Integer> {
    @Query("SELECT * FROM images WHERE vet_id = $1 ORDER BY id DESC LIMIT 1")
    Mono<Photo> findByVetId(String vetId);

    Mono<Integer> deleteByVetId(String vetId);
}
