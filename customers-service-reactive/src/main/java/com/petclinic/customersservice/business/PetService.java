package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import reactor.core.publisher.Mono;

public interface PetService {

    Mono<Pet> insertPet(Mono<Pet> petMono);
    Mono<Pet> getPetByPetId(int petId);

}
