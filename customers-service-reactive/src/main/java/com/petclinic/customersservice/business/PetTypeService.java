package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetTypeService {

    Mono<PetType> insertPetType(Mono<PetType> petTypeMono);
    //Mono<PetType> getPetTypeById(Integer Id);

    //Flux<PetType> getAllPetTypes();

    Flux<PetTypeResponseDTO> getAllPetTypes();
    Mono<PetTypeResponseDTO> getPetTypeByPetTypeId(String petTypeId);

    Mono<PetTypeResponseDTO> updatePetType(Mono<PetTypeRequestDTO> petTypeRequestDTO, String petTypeId);

    Mono<Void> deletePetTypeByPetTypeId(String petTypeId);




}
