package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface PetRepo extends ReactiveMongoRepository<Pet, Integer> {

    Flux<Pet> findAllPetByOwnerId(int ownerId);

    Mono<Pet> findPetByOwnerId(int ownerId, int petId);
    Mono<Void> deletePetById(int petId);
}