package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PetTypeRepo extends ReactiveMongoRepository<PetType, Integer> {

    Mono<PetType> findPetTypeById(String Id);

}
