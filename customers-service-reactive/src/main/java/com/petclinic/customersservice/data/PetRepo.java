package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PetRepo extends ReactiveMongoRepository<Pet, Integer> {

    Flux<Pet> findPetsByOwnerId(int ownerId);
    Flux<PetType> findPetTypes();

}
