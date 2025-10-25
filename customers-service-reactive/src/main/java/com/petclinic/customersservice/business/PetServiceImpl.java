package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
@Slf4j
public class PetServiceImpl implements PetService {

    @Autowired
    PetRepo petRepo;

    @Autowired
    OwnerService ownerService;

    @Autowired
    FilesServiceClient filesServiceClient;

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
                .flatMap(found -> {
                    Mono<Void> deletePhotoMono = Mono.justOrEmpty(found.getPhotoId())
                            .flatMap(filesServiceClient::deleteFile);
                    Mono<Void> deletePetMono = petRepo.delete(found);
                    
                    return Mono.when(deletePetMono, deletePhotoMono);
                });
    }

    @Override
    public Mono<PetResponseDTO> deletePetByPetIdV2(String petId) {
        return petRepo.findPetByPetId(petId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Pet id not found: " + petId))))
                .flatMap(found -> {
                    Mono<Void> deletePhotoMono = Mono.justOrEmpty(found.getPhotoId())
                            .flatMap(filesServiceClient::deleteFile);
                    Mono<Void> deletePetMono = petRepo.delete(found);
                    
                    return Mono.when(deletePetMono, deletePhotoMono)
                            .thenReturn(found);
                })
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

    @Override
    public Mono<PetResponseDTO> deletePetPhoto(String petId) {
        return petRepo.findPetByPetId(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("Pet not found with id: " + petId)))
                .flatMap(existingPet -> {
                    String photoId = existingPet.getPhotoId();
                    if (photoId != null && !photoId.isEmpty()) {
                        existingPet.setPhotoId(null);
                        return petRepo.save(existingPet)
                                .flatMap(savedPet ->
                                        filesServiceClient.deleteFile(photoId)
                                                .onErrorResume(e -> {
                                                    log.error("Error deleting photo file {}: {}", photoId, e.getMessage());
                                                    return Mono.empty();
                                                })
                                                .thenReturn(savedPet)
                                )
                                .map(EntityDTOUtil::toPetResponseDTO);
                    }
                    return Mono.just(EntityDTOUtil.toPetResponseDTO(existingPet));
                });
    }
}
