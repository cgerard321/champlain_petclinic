package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/pets")
public class PetControllerV1 {

    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER,Roles.RECEPTIONIST})
    @GetMapping("/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetByPetId(
            @PathVariable String petId,
            @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {

        return customersServiceClient.getPetByPetId(petId, includePhoto)
                .map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // NOTE: Owner was added as one of the allowed roles to fix pet owners being unable to create visits.
    // Really, there should be a "getAllPetsForOwner" instead and get rid of Owners being able to get all pets.
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER, Roles.RECEPTIONIST})
    @GetMapping(value = "", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetResponseDTO> getAllPets(){
        return customersServiceClient.getAllPets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PetResponseDTO>> addPet(@RequestBody Mono<PetRequestDTO> petRequestDTO) {
        return customersServiceClient.addPet(petRequestDTO)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.VET})
    @GetMapping("/owners/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetForOwner(
            @PathVariable String ownerId,
            @PathVariable String petId,
            @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return customersServiceClient.getPetByPetId(petId, includePhoto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER})
    @PutMapping("/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(
            @PathVariable String petId,
            @RequestBody Mono<PetRequestDTO> petRequestDTO) {
        return customersServiceClient.updatePet(petRequestDTO, petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.VET,Roles.RECEPTIONIST})
    @PutMapping("/owners/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePetForOwner(
            @PathVariable String ownerId,
            @PathVariable String petId,
            @RequestBody Mono<PetRequestDTO> petRequestDTO) {
        return customersServiceClient.updatePet(petRequestDTO, petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER})
    @DeleteMapping("/{petId}")
    public Mono<ResponseEntity<Void>> deletePet(@PathVariable String petId) {
        return customersServiceClient.deletePetByPetId(petId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER,Roles.ADMIN,Roles.VET})
    @PatchMapping(value = "/{petId}/active", produces = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> patchPet(@RequestParam String isActive, @PathVariable String petId) {
        return customersServiceClient.patchPet(isActive, petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER})
    @PatchMapping("/{petId}/photo")
    public Mono<ResponseEntity<PetResponseDTO>> deletePetPhoto(@PathVariable String petId) {
        return customersServiceClient.deletePetPhoto(petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
