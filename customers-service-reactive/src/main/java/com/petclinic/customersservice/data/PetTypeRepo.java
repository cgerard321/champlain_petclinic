package com.petclinic.customersservice.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PetTypeRepo extends ReactiveMongoRepository<PetType, String> {
    //Mono<PetType> findPetTypeById(String Id);

    Mono<PetType> findByPetTypeId(String petTypeId);

    Mono<Void> deleteByPetTypeId(String petTypeId);


}

