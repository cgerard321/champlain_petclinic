package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/gateway/owners")
public class OwnerControllerV1 {
    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OwnerResponseDTO> getAllOwners() {
        return customersServiceClient.getAllOwners();

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OwnerResponseDTO>> addOwner(@RequestBody Mono<OwnerRequestDTO> ownerRequestDTO) {
        return customersServiceClient.createOwner(ownerRequestDTO)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/owners-pagination", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/owners-count")
    public Mono<Long> getTotalNumberOfOwners(){
        return customersServiceClient.getTotalNumberOfOwners();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
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



    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.RECEPTIONIST})
    @GetMapping(value = "/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerDetails(final @PathVariable String ownerId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return customersServiceClient.getOwner(ownerId, includePhoto)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.RECEPTIONIST})
    @PutMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @PathVariable String ownerId,
            @RequestBody Mono<OwnerRequestDTO> ownerRequestMono) {
        return Mono.just(ownerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided owner id is invalid: " + ownerId)))
                .flatMap(id -> ownerRequestMono.flatMap(ownerRequestDTO ->
                        customersServiceClient.updateOwner(id, Mono.just(ownerRequestDTO))
                                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                ));
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @PatchMapping("/{ownerId}/photo")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwnerPhoto(
            @PathVariable String ownerId,
            @RequestBody Mono<FileDetails> photoMono) {
        return customersServiceClient.updateOwnerPhoto(ownerId, photoMono)
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwner(@PathVariable String ownerId){
        return customersServiceClient.deleteOwner(ownerId).then(Mono.just(ResponseEntity.noContent().<OwnerResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @PostMapping(value = "/{ownerId}/pets" , produces = "application/json", consumes = "application/json")
    public Mono<ResponseEntity<PetResponseDTO>> createPetForOwner(@PathVariable String ownerId, @RequestBody PetRequestDTO petRequest){
        return customersServiceClient.createPetForOwner(ownerId, petRequest)
                .map(pet -> ResponseEntity.status(HttpStatus.CREATED).body(pet))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> getPet(@PathVariable String ownerId, @PathVariable String petId){
        return customersServiceClient.getPet(ownerId, petId).map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/{ownerId}/pets", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PetResponseDTO> getPetsByOwnerId(@PathVariable String ownerId){
        return customersServiceClient.getPetsByOwnerId(ownerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @DeleteMapping("/{ownerId}/pets/{petId}")
    public Mono<ResponseEntity<PetResponseDTO>> deletePet(@PathVariable String ownerId, @PathVariable String petId){
         return customersServiceClient.deletePet(ownerId,petId)
                .map(pet -> ResponseEntity.noContent().<PetResponseDTO>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @DeleteMapping("/{ownerId}/photo")
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwnerPhoto(@PathVariable String ownerId) {
        return customersServiceClient.deleteOwnerPhoto(ownerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN, Roles.VET})
    @PatchMapping("/{ownerId}/pets/{petId}/photo")
    public Mono<ResponseEntity<PetResponseDTO>> deletePetPhotoForOwner(
            @PathVariable String ownerId,
            @PathVariable String petId) {
        return customersServiceClient.deletePetPhoto(petId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
