package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface PetRepo extends ReactiveMongoRepository<Pet, String> {

    Flux<Pet> findAllPetByOwnerId(String ownerId);
    Flux<Pet> findPetsByOwnerId(String ownerId);
    Mono<Pet> findPetByPetId(String Id);
}