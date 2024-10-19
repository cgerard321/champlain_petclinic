package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetService {

    Mono<Pet> insertPet(Mono<Pet> petMono);
    Mono<Pet> getPetById(String Id);
    Flux<PetResponseDTO> getPetsByOwnerId(String ownerId);
    Mono<Pet> updatePetByPetId(String petId, Mono<Pet> petMono);
    Mono<Void> deletePetByPetId(String petId);
    Mono<PetResponseDTO> deletePetByPetIdV2(String petID);
    Mono<Pet> updatePetIsActive(String petId, String isActive);
    Flux<Pet> getAllPets();
    Mono<String> testQodana();
}
