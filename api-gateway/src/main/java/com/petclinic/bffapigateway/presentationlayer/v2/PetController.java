package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.exceptions.ForbiddenAccessException;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;

import java.util.List;

import static reactor.core.publisher.Mono.just;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/pets")
@Validated
public class PetController {
    private final CustomersServiceClient customersServiceClient;
    private final AuthServiceClient authServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PutMapping(value = "/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(@RequestBody Mono<PetRequestDTO> petRequestDTO, @PathVariable String petId) {
        return petRequestDTO
                .flatMap(requestDTO -> customersServiceClient.updatePet(just(requestDTO), petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(value= "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PetResponseDTO>> addPet(@RequestBody Mono<PetRequestDTO> petRequestDTO) {
        return customersServiceClient.addPet(petRequestDTO)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @GetMapping(value = "/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPetByPetId(@PathVariable String petId) {
        return customersServiceClient.getPetByPetId(petId)
                .map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @GetMapping("/owners/{ownerId}/pets")
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId) {
        return customersServiceClient.getPetsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.OWNER, Roles.VET})
    @DeleteMapping(value = "/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PetResponseDTO>> deletePet(
            @PathVariable String petId,
            @CookieValue("Bearer") String jwtToken) {

        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided pet id is invalid: " + petId)))
                .flatMap(validPetId -> {
                    // First get the pet to check ownership
                    return customersServiceClient.getPetByPetId(validPetId)
                            .flatMap(pet -> {
                                // Validate token and get user info
                                return authServiceClient.validateToken(jwtToken)
                                        .flatMap(tokenResponse -> {
                                            String userId = tokenResponse.getBody().getUserId();
                                            List<String> roles = tokenResponse.getBody().getRoles();

                                            // Check if user has ADMIN or VET role (bypass ownership check)
                                            boolean isAdminOrVet = roles.contains("ADMIN") || roles.contains("VET");

                                            if (isAdminOrVet || pet.getOwnerId().equals(userId)) {
                                                // User is authorized to delete this pet
                                                return customersServiceClient.deletePetByPetIdV2(validPetId);
                                            } else {
                                                // User is not authorized to delete this pet
                                                return Mono.error(new ForbiddenAccessException("You are not allowed to delete this pet"));
                                            }
                                        });
                            });
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


}
