package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
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

import java.util.Optional;
@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/owners")
public class OwnerControllerV1 {
    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OwnerResponseDTO> getAllOwners() {
        return customersServiceClient.getAllOwners();

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners-pagination")
    public Flux<OwnerResponseDTO> getOwnersByPagination(@RequestParam Optional<Integer> page,
                                                        @RequestParam Optional<Integer> size,
                                                        @RequestParam(required = false) String ownerId,
                                                        @RequestParam(required = false) String firstName,
                                                        @RequestParam(required = false) String lastName,
                                                        @RequestParam(required = false) String phoneNumber,
                                                        @RequestParam(required = false) String city) {

        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(5);
        }

        return customersServiceClient.getOwnersByPagination(page,size,ownerId,firstName,lastName,phoneNumber,city);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners-count")
    public Mono<Long> getTotalNumberOfOwners(){
        return customersServiceClient.getTotalNumberOfOwners();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/owners-filtered-count")
    public Mono<Long> getTotalNumberOfOwnersWithFilters (
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city)
    {
        return customersServiceClient.getTotalNumberOfOwnersWithFilters(ownerId,firstName,lastName,phoneNumber,city);
    }



    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerDetails(final @PathVariable String ownerId) {
        return customersServiceClient.getOwner(ownerId)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.RECEPTIONIST})
    @PostMapping(value = "/{ownerId}/photos")
    public Mono<ResponseEntity<String>> setOwnerPhoto(@RequestBody PhotoDetails photoDetails, @PathVariable String ownerId) {
        return customersServiceClient.setOwnerPhoto(photoDetails, ownerId).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @IsUserSpecific(idToMatch = {"ownerId"})
    @PutMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @PathVariable String ownerId,
            @RequestBody Mono<OwnerRequestDTO> ownerRequestMono) {
        return ownerRequestMono.flatMap(ownerRequestDTO ->
                customersServiceClient.updateOwner(ownerId, Mono.just(ownerRequestDTO))
                        .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                        .defaultIfEmpty(ResponseEntity.notFound().build())
        );
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwner(@PathVariable String ownerId){
        return customersServiceClient.deleteOwner(ownerId).then(Mono.just(ResponseEntity.noContent().<OwnerResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @PostMapping(value = "/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> createPetForOwner(@PathVariable String ownerId, @RequestBody PetRequestDTO petRequest){
        return customersServiceClient.createPetForOwner(ownerId, petRequest)
                .map(pet -> ResponseEntity.status(HttpStatus.CREATED).body(pet))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPet(@PathVariable String ownerId, @PathVariable String petId){
        return customersServiceClient.getPet(ownerId, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/{ownerId}/pets", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId){
        return customersServiceClient.getPetsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @DeleteMapping("/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> deletePet(@PathVariable String ownerId, @PathVariable String petId){
        return customersServiceClient.deletePet(ownerId,petId).then(Mono.just(ResponseEntity.noContent().<PetResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @GetMapping("/{ownerId}/photos")
    public Mono<ResponseEntity<byte[]>> getOwnerPhoto(@PathVariable String ownerId) {
        return customersServiceClient.getOwnerPhoto(ownerId)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(bytes))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}
