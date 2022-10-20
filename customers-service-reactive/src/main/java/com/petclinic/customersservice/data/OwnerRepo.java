package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OwnerRepo extends ReactiveMongoRepository<Owner, String> {

    Mono<Owner> findById(String ownerId);

    Mono<Void> deleteById(String ownerId);
}
