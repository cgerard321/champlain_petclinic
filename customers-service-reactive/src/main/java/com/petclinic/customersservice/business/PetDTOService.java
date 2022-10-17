package com.petclinic.customersservice.business;

import reactor.core.publisher.Mono;

public interface PetDTOService {

    Mono<PetDTO> getPetDTOByPetId(int petId);

}
