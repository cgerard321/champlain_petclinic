package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
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

    @Autowired
    FilesServiceClient filesServiceClient;

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
    public Mono<PetResponseDTO> getPetById(String Id, boolean includePhoto) {
        return petRepo.findPetByPetId(Id)
           .flatMap(pet -> {
             PetResponseDTO dto = EntityDTOUtil.toPetResponseDTO(pet);

             if (includePhoto && pet.getPhotoId() != null && !pet.getPhotoId().isEmpty()) {
        return filesServiceClient.getFile(pet.getPhotoId())
           .map(fileResp -> {
            dto.setPhoto(fileResp);
        return dto;
        })
        .onErrorResume(err -> {
           System.err.printf("Error fetching file %s for petId %s: %s%n",
           pet.getPhotoId(), Id, err.getMessage());
           return Mono.just(dto);
           });
        }
        return Mono.just(dto);
      });
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
                    return ownerService.getOwnerByOwnerId(ownerId, false)
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
