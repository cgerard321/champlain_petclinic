package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private PhotoService photoService;

    @GetMapping("/{petId}")
    public Mono<PetResponseDTO> getPetDTOByPetId(@PathVariable String petId) {
        return petService.getPetById(petId)
                .map(EntityDTOUtil::toPetResponseDTO);
    }

    @GetMapping("/owner/{ownerId}/pets")
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId) {
        return petService.getPetsByOwnerId(ownerId);
    }

    @DeleteMapping("/{petId}")
    public Mono<Void> deletePetByPetId(@PathVariable String petId) {
        return petService.deletePetByPetId(petId);
    }

    @DeleteMapping("/{petId}/v2")
    public Mono<PetResponseDTO> deletePetByPetIdV2(@PathVariable String petId) {
        return petService.deletePetByPetIdV2(petId);
    }

    @PostMapping
    public Mono<PetResponseDTO> insertPet(@RequestBody Mono<Pet> petMono) {
        return petService.insertPet(petMono).map(EntityDTOUtil::toPetResponseDTO);
    }

    @PutMapping("/{petId}")
    public Mono<ResponseEntity<Pet>> updatePetByPetId(@PathVariable String petId, @RequestBody Mono<Pet> petMono) {
        return petService.updatePetByPetId(petId, petMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePetIsActive(@PathVariable String petId, @RequestBody PetRequestDTO petRequestDTO) {
        try {
            return petService.updatePetIsActive(petId, petRequestDTO.getIsActive())
                    .map(EntityDTOUtil::toPetResponseDTO)
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error updating pet status", ex);
        }
    }

    @GetMapping()
    public Flux<PetResponseDTO> getAllPets() {
        return petService.getAllPets().map(EntityDTOUtil::toPetResponseDTO);
    }

    @GetMapping("/{petId}/photo")
    public Mono<ResponseEntity<PhotoResponseModel>> getPetPhotoByPetId(@PathVariable String petId) {
        if (!isValidUUID(petId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid pet ID format");
        }
        
        return photoService.getPetPhotoByPetId(petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private boolean isValidUUID(String uuid) {
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
