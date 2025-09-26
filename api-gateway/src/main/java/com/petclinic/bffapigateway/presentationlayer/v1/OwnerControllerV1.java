package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
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
                /*.flatMap(n ->
                        visitsServiceClient.getVisitsForPets(n.getPetIds())
                                .map(addVisitsToOwner(n))
                );*/
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

                /*.flatMap(owner ->
                        visitsServiceClient.getVisitsForPets(owner.getPetIds())
                                .map(addVisitsToOwner(owner))
                );*/
    }


//    @PostMapping(value = "owners",
//            consumes = "application/json",
//            produces = "application/json")
//    public Mono<OwnerResponseDTO> createOwner(@RequestBody OwnerResponseDTO model){
//        return customersServiceClient.createOwner(model);
//    }

    @IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
    @PostMapping(value = "/photo/{ownerId}")
    public Mono<ResponseEntity<String>> setOwnerPhoto(@RequestBody PhotoDetails photoDetails, @PathVariable int ownerId) {
        return customersServiceClient.setOwnerPhoto(photoDetails, ownerId).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    /*@GetMapping(value = "owners/photo/{ownerId}")
    public Mono<PhotoDetails> getOwnerPhoto(@PathVariable int ownerId) {
        return customersServiceClient.getOwnerPhoto(ownerId);
    }*/

//    @PostMapping(value = "owners/{ownerId}/pet/photo/{petId}")
//    public Mono<String> setPetPhoto(@PathVariable String ownerId, @RequestBody PhotoDetails photoDetails, @PathVariable String petId) {
//        return customersServiceClient.setPetPhoto(ownerId, photoDetails, petId);
//    }
//
//    @GetMapping(value = "owners/{ownerId}/pet/photo/{petId}")
//    public Mono<PhotoDetails> getPetPhoto(@PathVariable String ownerId, @PathVariable String petId) {
//        return customersServiceClient.getPetPhoto(ownerId, petId);
//    }
//
//    @DeleteMapping(value = "owners/photo/{photoId}")
//    public Mono<Void> deleteOwnerPhoto(@PathVariable int photoId){
//        return customersServiceClient.deleteOwnerPhoto(photoId);
//    }
//
//    @DeleteMapping(value = "owners/{ownerId}/pet/photo/{photoId}")
//    public Mono<Void> deletePetPhoto(@PathVariable int ownerId, @PathVariable int photoId){
//        return customersServiceClient.deletePetPhoto(ownerId, photoId);
//    }





    /*

    // Endpoint to update an owner
    @PutMapping("owners/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @PathVariable String ownerId,
            @RequestBody OwnerRequestDTO ownerRequestDTO) {
        return customersServiceClient.updateOwner(ownerId, ownerRequestDTO)
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


     */

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
}
