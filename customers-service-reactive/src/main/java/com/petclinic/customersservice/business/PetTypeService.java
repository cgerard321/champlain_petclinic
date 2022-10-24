package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PetTypeService {

   public Mono<PetType> insertPetType(Mono<PetType> petTypeMono);

   public Flux<PetType> getAll();

    public Mono<Void> deletePetTypeByID(int id);

    public Mono<Void> deletePetType(int id);

    public Mono<PetType> updatePetType(int id, Mono<PetType> petTypeMono);


}
