package com.petclinic.customersservice.data;

import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface PetRepo extends ReactiveMongoRepository<Pet, String> {

    Flux<Pet> findAllPetByOwnerId(String ownerId);
    Mono<Pet> findPetByPetId(String Id);
}