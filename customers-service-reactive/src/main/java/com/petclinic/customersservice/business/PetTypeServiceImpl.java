package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
public class PetTypeServiceImpl implements PetTypeService {

    @Autowired
    PetTypeRepo petTypeRepo;

    @Override
    public Mono<PetType> insertPetType(Mono<PetType> petTypeMono) {
        return petTypeMono
                .flatMap(petTypeRepo::insert);
    }

    @Override
    public Flux<PetType> getAllPetTypes() {
        return petTypeRepo.findAll();
    }

    @Override
    public Mono<PetType> getPetTypeById(String Id) {
        return petTypeRepo.findPetTypeById(Id);
    }

}
