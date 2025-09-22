package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
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

import static reactor.core.publisher.Mono.just;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/pets")
@Validated
public class PetController {
    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PutMapping(value = "/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> updatePet(@RequestBody Mono<PetRequestDTO> petRequestDTO, @PathVariable String petId) {
        return petRequestDTO
                .flatMap(requestDTO -> {
                    requestDTO.setPetId(petId); // Ensure petId is set
                    return customersServiceClient.updatePet(just(requestDTO), petId)
                            .flatMap(petResponse -> customersServiceClient.getOwner(petResponse.getOwnerId())
                                    .flatMap(owner -> {
                                        // Remove the old pet and add the updated pet
                                        owner.getPets().removeIf(pet -> pet.getPetId().equals(petId));
                                        owner.getPets().add(petResponse);

                                        // Create OwnerRequestDTO with existing owner details
                                        OwnerRequestDTO ownerRequestDTO = OwnerRequestDTO.builder()
                                                .ownerId(owner.getOwnerId())
                                                .firstName(owner.getFirstName())
                                                .lastName(owner.getLastName())
                                                .address(owner.getAddress())
                                                .city(owner.getCity())
                                                .province(owner.getProvince())
                                                .telephone(owner.getTelephone())
                                                .build();

                                        return customersServiceClient.updateOwner(ownerRequestDTO.getOwnerId(), just(ownerRequestDTO))
                                                .thenReturn(petResponse);
                                    })
                            );
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
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
    @GetMapping("/owner/{ownerId}/pets")
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId) {
        return customersServiceClient.getPetsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @DeleteMapping(value = "/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PetResponseDTO>> deletePet(@PathVariable String petId) {
        return Mono.just(petId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided pet id is invalid: " + petId)))
                .flatMap(customersServiceClient::deletePetByPetIdV2)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


}
