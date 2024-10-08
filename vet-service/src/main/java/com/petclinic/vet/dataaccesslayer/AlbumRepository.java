package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AlbumRepository extends ReactiveCrudRepository<Album, Integer> {

    Flux<Album> findAllByVetId(String vetId);
}
