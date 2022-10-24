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
    private PetTypeRepo petTypeRepo;

    @Override
    public Mono<PetType> insertPetType(Mono<PetType> petTypeMono) {
        return petTypeMono
                .flatMap(petTypeRepo::insert);
    }

    @Override
    public Flux<PetType> getAll() {
        return petTypeRepo.findAll();
    }

    @Override
    public Mono<Void> deletePetTypeByID(int id){
        return petTypeRepo.deleteById(id);
    }

    @Override
    public Mono<Void> deletePetType(int id){
        return petTypeRepo.deletePetTypeById(id);
    }

    @Override
    public Mono<PetType> updatePetType(int id, Mono<PetType> petTypeMono){

        return petTypeRepo.findPetTypesById(id)
            .flatMap(p -> petTypeMono
                    .doOnNext(e -> e.setId(p.getId()))
            )
            .flatMap(petTypeRepo::save);
    }

}
