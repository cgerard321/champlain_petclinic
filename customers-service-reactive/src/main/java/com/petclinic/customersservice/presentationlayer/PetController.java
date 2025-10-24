package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
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

    @GetMapping("/{petId}")
    public Mono<PetResponseDTO> getPet(
            @PathVariable String petId,
            @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return petService.getPetById(petId, includePhoto);
    }

    @GetMapping("/pet/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetByPetId(
            @PathVariable String petId,
            @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return petService.getPetById(petId, includePhoto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
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

    @PostMapping("/owners/{ownerId}/pets")
    public Mono<ResponseEntity<PetResponseDTO>> createPetForOwner(
            @PathVariable String ownerId,
            @RequestBody PetRequestDTO petRequest) {
        return petService.createPetForOwner(ownerId, Mono.just(petRequest))
                .map(pet -> ResponseEntity.status(HttpStatus.CREATED).body(pet))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

}
