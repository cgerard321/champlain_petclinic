package com.petclinic.customersservice.business;

import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetService {

    Mono<PetResponseDTO> addPet(Mono<PetRequestDTO> petMono);
    Mono<PetResponseDTO> getPetById(String Id, boolean includePhoto);
    Flux<PetResponseDTO> getPetsByOwnerId(String ownerId);
    Mono<PetResponseDTO> updatePetByPetId(String petId, Mono<PetRequestDTO> petMono);
    Mono<Void> deletePetByPetId(String petId);
    Mono<PetResponseDTO> deletePetByPetIdV2(String petID);
    Mono<PetResponseDTO> updatePetIsActive(String petId, String isActive);
    Flux<PetResponseDTO> getAllPets();
    Mono<PetResponseDTO> createPetForOwner(String ownerId, Mono<PetRequestDTO> petRequestDTO);
    Mono<PetResponseDTO> deletePetPhoto(String petId);
}
