package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/owners/petTypes")
public class PetControllerV1 {

    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return customersServiceClient.getAllPetTypes();
                /*.flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );*/
    }

    @IsUserSpecific(idToMatch = {"petTypeId"}, bypassRoles = {Roles.ALL})
    @GetMapping(value = "/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> getPetTypeById(final @PathVariable String petTypeId) {
        return customersServiceClient.getPetTypeByPetTypeId(petTypeId)
                .map(petTypeResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(petTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());

                /*.flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );*/
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{petTypeId}")
    public Mono<ResponseEntity<Void>> deletePetTypeByPetTypeId(final @PathVariable String petTypeId){
        return customersServiceClient.deletePetTypeV2(petTypeId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().<Void>build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping
    public Mono<ResponseEntity<PetTypeResponseDTO>> addPetType(
            @RequestBody Mono<PetTypeRequestDTO> petTypeRequestDTOMono) {

        return customersServiceClient.addPetType(petTypeRequestDTOMono)
                .map(createdPetType ->
                        ResponseEntity.status(HttpStatus.CREATED).body(createdPetType))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @IsUserSpecific(idToMatch = {"petTypeId"})
    @PutMapping("/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> updatePetType(
            @PathVariable String petTypeId,
            @RequestBody Mono<PetTypeRequestDTO> petTypeRequestMono) {
        return petTypeRequestMono.flatMap(petTypeRequestDTO ->
                customersServiceClient.updatePetType(petTypeId, Mono.just(petTypeRequestDTO))
                        .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                        .defaultIfEmpty(ResponseEntity.notFound().build())
        );
    }
}
