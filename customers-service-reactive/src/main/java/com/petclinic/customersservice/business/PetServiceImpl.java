package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    public Mono<Pet> getPetById(String Id) {
        return petRepo.findPetById(Id);
    }

    @Override
    public Flux<Pet> getPetsByOwnerId(String ownerId) {
        return petRepo.findAllPetByOwnerId(ownerId);
    }

    @Override
    public Mono<Pet> updatePetByPetId(String petId, Mono<Pet> petMono) {
        return petRepo.findById(petId)
                .flatMap(p -> petMono
                        .doOnNext(e -> e.setName(p.getName()))
                        .doOnNext(e -> e.setBirthDate(p.getBirthDate()))
                        .doOnNext(e -> e.setPetTypeId(p.getPetTypeId()))
                        .doOnNext(e -> e.setOwnerId(p.getOwnerId()))
                )
                .flatMap(petRepo::save);
    }

    @Override
    public Mono<Void> deletePetByPetId(String petId) {
        return petRepo.deleteById(petId);
    }
}
