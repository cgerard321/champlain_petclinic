package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    PetRepo petRepo;

    @Autowired
    OwnerService ownerService;

    @Override
    public Flux<PetResponseDTO> getAllPets() {
        return petRepo.findAll().map(EntityDTOUtil::toPetResponseDTO);
    }

    @Override
    public Mono<PetResponseDTO> addPet(Mono<PetRequestDTO> petMono) {
        return petMono
                .map(EntityDTOUtil::toPet)
                .flatMap(petRepo::save)
                .map(EntityDTOUtil::toPetResponseDTO);
    }

    @Override
    public Mono<PetResponseDTO> getPetById(String Id) {
        return petRepo.findPetByPetId(Id).map(EntityDTOUtil::toPetResponseDTO);
    }

    @Override
    public Flux<PetResponseDTO> getPetsByOwnerId(String ownerId) {
        return petRepo.findAllPetByOwnerId(ownerId)
                .flatMap(petEntity -> Mono.just(EntityDTOUtil.toPetResponseDTO(petEntity)))
                .onErrorResume(e -> Flux.empty()); // Handle errors by returning an empty Flux
    }

    @Override
    public Mono<PetResponseDTO> updatePetByPetId(String petId, Mono<PetRequestDTO> petMono) {
        return petRepo.findPetByPetId(petId)
                .flatMap(pet -> petMono
                        .map(EntityDTOUtil::toPet)
                        .doOnNext(p -> {
                            p.setId(pet.getId());
                            p.setPetId(pet.getPetId());
                        })
                )
                .flatMap(petRepo::save)
                .map(EntityDTOUtil::toPetResponseDTO);
    }
    @Override
    public Mono<PetResponseDTO> updatePetIsActive(String petId, String isActive) {
        return petRepo.findPetByPetId(petId)
                .flatMap(p -> {
                    p.setIsActive(isActive);
                    return petRepo.save(p);
                })
                .map(EntityDTOUtil::toPetResponseDTO);
    }
    @Override
    public Mono<Void> deletePetByPetId(String petId) {
        return petRepo.findPetByPetId(petId)
                .flatMap(petRepo::delete);
    }

    @Override
    public Mono<PetResponseDTO> deletePetByPetIdV2(String petId) {
        return petRepo.findPetByPetId(petId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Pet id not found: " + petId))))
                .flatMap(found -> petRepo.delete(found)
                        .then(Mono.just(found)))
                .map(EntityDTOUtil::toPetResponseDTO);
    }

    @Override
    public Mono<PetResponseDTO> createPetForOwner(String ownerId, Mono<PetRequestDTO> petRequestDTO) {
        return petRequestDTO
                .flatMap(requestDTO -> {
                    return ownerService.getOwnerByOwnerId(ownerId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id: " + ownerId)))
                            .then(Mono.just(requestDTO));
                })
                .map(requestDTO -> {
                    Pet pet = new Pet();
                    pet.setPetId(UUID.randomUUID().toString());
                    pet.setOwnerId(ownerId);
                    pet.setName(requestDTO.getName());
                    pet.setBirthDate(requestDTO.getBirthDate());
                    pet.setPetTypeId(requestDTO.getPetTypeId());
                    pet.setIsActive("true");
                    pet.setWeight(requestDTO.getWeight());
                    return pet;
                })
                .flatMap(petRepo::save)
                .map(EntityDTOUtil::toPetResponseDTO);
    }

}
