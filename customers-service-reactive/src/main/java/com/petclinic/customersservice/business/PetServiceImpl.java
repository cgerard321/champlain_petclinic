package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    PetRepo petRepo;

    @Override
    public Mono<Pet> insertPet(Mono<Pet> petMono) {
        return petMono
                .flatMap(petRepo::insert);
    }

    @Override
    public Mono<Pet> getPetByPetId(int petId) {
        return petRepo.findPetByPetId(petId);
    }
}
