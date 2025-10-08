package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
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

import java.util.Optional;


@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/owners/petTypes")
public class PetTypeControllerV1 {

    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return customersServiceClient.getAllPetTypes();
    }

    @GetMapping(value = "", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetType> getPetTypes(){
        return customersServiceClient.getPetTypes();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
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


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET, Roles.OWNER})
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/pet-types-pagination")
    public Flux<PetTypeResponseDTO> getPetTypesByPagination(@RequestParam Optional<Integer> page,
                                                            @RequestParam Optional<Integer> size,
                                                            @RequestParam(required = false) String petTypeId,
                                                            @RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String description) {

        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(5);
        }

        return customersServiceClient.getPetTypesByPagination(page, size, petTypeId, name, description);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/pet-types-count")
    public Mono<Long> getTotalNumberOfPetTypes(){
        return customersServiceClient.getTotalNumberOfPetTypes();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/pet-types-filtered-count")
    public Mono<Long> getTotalNumberOfPetTypesWithFilters (
            @RequestParam(required = false) String petTypeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description)
    {
        return customersServiceClient.getTotalNumberOfPetTypesWithFilters(petTypeId, name, description);
    }

}

