package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.util.EntityDTOUtil;
import com.petclinic.customersservice.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetDTOByPetId(@PathVariable String petId) {
        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetId(petId))
                .flatMap(id -> petService.getPetById(id))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petNotFound(petId));
    }

    @GetMapping("/owner/{ownerId}/pets")
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId) {
        return petService.getPetsByOwnerId(ownerId);
    }

    @DeleteMapping("/{petId}")
    public Mono<ResponseEntity<Void>> deletePetByPetId(@PathVariable String petId) {
        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetId(petId))
                .flatMap(id -> petService.deletePetByPetId(id).thenReturn(ResponseEntity.noContent().<Void>build()))
                .switchIfEmpty(ApplicationExceptions.petNotFound(petId));
    }

    @PostMapping
    public Mono<ResponseEntity<PetResponseDTO>> addPet(@RequestBody Mono<PetRequestDTO> petMono) {
        return petMono
                .transform(Validator.validatePet())
                .as(petService::addPet)
                .map(petResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(petResponseDTO));
    }


    //this endpoint could probably be removed
    @DeleteMapping("/{petId}/v2")
    public Mono<ResponseEntity<PetResponseDTO>> deletePetByPetIdV2(@PathVariable String petId) {
        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetId(petId))
                .flatMap(id -> petService.deletePetByPetIdV2(id))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petNotFound(petId));
    }

    @PutMapping("/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePetByPetId(@PathVariable String petId, @RequestBody Mono<PetRequestDTO> petMono) {
        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetId(petId))
                .thenReturn(petMono.transform(Validator.validatePet()))
                .flatMap(request -> petService.updatePetByPetId(petId, request))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petNotFound(petId));
    }

    @PatchMapping("/{petId}/active")
    public Mono<ResponseEntity<PetResponseDTO>> updatePetIsActive(@PathVariable String petId, @RequestParam String isActive) {
        log.info("received request to update pet isActive: {}", isActive);


        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetId(petId))
                .flatMap(id ->  petService.updatePetIsActive(id, isActive))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petNotFound(petId));
    }

    @GetMapping()
    public Flux<PetResponseDTO> getAllPets() {
        return petService.getAllPets();
    }

    //This endpoint call also probably be removed, the petRequestDTO already takes an ownerId
    @PostMapping("/owners/{ownerId}/pets")
    public Mono<ResponseEntity<PetResponseDTO>> createPetForOwner(@PathVariable String ownerId, @RequestBody PetRequestDTO petRequest) {
        return petService.createPetForOwner(ownerId, Mono.just(petRequest))
                .map(pet -> ResponseEntity.status(HttpStatus.CREATED).body(pet))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

}
