package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/pet")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class PetController {
    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @IsUserSpecific(idToMatch = {"petId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/{petId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(
            @PathVariable String petId,
            @RequestBody Mono<PetRequestDTO> petRequestDTO) {

        return Mono.just(petId)
                .filter(id -> id.length() == 36) // Validate the petId length
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided pet id is invalid: " + petId)))
                .flatMap(id -> customersServiceClient.updatePet(id, petRequestDTO))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetByPetId(@PathVariable String petId){
        return customersServiceClient.getPetByPetId(petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}

