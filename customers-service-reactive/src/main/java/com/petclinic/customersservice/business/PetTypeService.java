package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetTypeService {

    Mono<PetType> insertPetType(Mono<PetType> petTypeMono);
    Mono<PetType> getPetTypeById(String Id);
    Flux<PetType> getAllPetTypes();
}
