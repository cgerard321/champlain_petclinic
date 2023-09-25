package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetService {

    Mono<Pet> insertPet(Mono<Pet> petMono);
    Mono<Pet> getPetById(String Id);
    Flux<Pet> getPetsByOwnerId(String ownerId);
    Mono<Pet> updatePetByPetId(String petId, Mono<Pet> petMono);
    Mono<Void> deletePetByPetId(String petId);
    Flux<Pet> getAllPets();
}
