package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
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
    public Flux<Pet> getAllPets() {
        return petRepo.findAll();
    }

    @Override
    public Mono<Pet> getPetById(String Id) {
        return petRepo.findPetByPetId(Id);
    }

    @Override
    public Flux<PetResponseDTO> getPetsByOwnerId(String ownerId) {
        return petRepo.findAllPetByOwnerId(ownerId)
                .flatMap(petEntity -> Mono.just(EntityDTOUtil.toPetResponseDTO(petEntity)))
                .onErrorResume(e -> Flux.empty()); // Handle errors by returning an empty Flux
    }

    @Override
    public Mono<Pet> updatePetByPetId(String petId, Mono<Pet> petMono) {
        return petRepo.findPetByPetId(petId)
                .flatMap(p -> petMono
                        .doOnNext(e -> e.setId(p.getId()))
                        .doOnNext(e -> e.setOwnerId(p.getOwnerId()))
                        .doOnNext(e -> e.setPhotoId(p.getPhotoId()))
                )
                .flatMap(petRepo::save);
    }
    @Override
    public Mono<Pet> updatePetIsActive(String petId, String isActive) {
        return petRepo.findPetByPetId(petId)
                .flatMap(p -> {
                    p.setIsActive(isActive);
                    return petRepo.save(p);
                });
    }
    @Override
    public Mono<Void> deletePetByPetId(String petId) {
        return petRepo.findPetByPetId(petId)
                .flatMap(petRepo::delete);
    }

}
