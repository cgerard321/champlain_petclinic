package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerRequestDTO;
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
@RequestMapping("/api/gateway/customers")
public class CustomerControllerV1 {
    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CustomerResponseDTO> getAllCustomers() {
        return customersServiceClient.getAllCustomers();

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CustomerResponseDTO>> addCustomer(@RequestBody Mono<CustomerRequestDTO> customerRequestDTOMono) {
        return customersServiceClient.createCustomer(customerRequestDTOMono)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/customers-pagination", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CustomerResponseDTO> getCustomersByPagination(@RequestParam Optional<Integer> page,
                                                              @RequestParam Optional<Integer> size,
                                                              @RequestParam(required = false) String customerId,
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

        return customersServiceClient.getCustomersByPagination(page,size,customerId,firstName,lastName,phoneNumber,city);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/customers-count")
    public Mono<Long> getTotalNumberOfCustomers(){
        return customersServiceClient.getTotalNumberOfCustomers();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET,Roles.RECEPTIONIST})
    @GetMapping(value = "/customers-filtered-count")
    public Mono<Long> getTotalNumberOfCustomersWithFilters (
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city)
    {
        return customersServiceClient.getTotalNumberOfCustomersWithFilters(customerId,firstName,lastName,phoneNumber,city);
    }



    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN,Roles.RECEPTIONIST})
    @GetMapping(value = "/detail/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerDetails(final @PathVariable String customerId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return customersServiceClient.getCustomer(customerId, includePhoto)
                .map(customerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(customerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN,Roles.RECEPTIONIST})
    @PutMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomer(
            @PathVariable String customerId,
            @RequestBody Mono<CustomerRequestDTO> customerRequestDTOMono) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided customer id is invalid: " + customerId)))
                .flatMap(id -> customerRequestDTOMono.flatMap(customerRequestDTO ->
                        customersServiceClient.updateCustomer(id, Mono.just(customerRequestDTO))
                                .map(updatedCustomer -> ResponseEntity.ok().body(updatedCustomer))
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                ));
    }

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @PatchMapping("/{customerId}/photo")
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomerPhoto(
            @PathVariable String customerId,
            @RequestBody Mono<FileDetails> photoMono) {
        return customersServiceClient.updateCustomerPhoto(customerId, photoMono)
                .map(updatedCustomer -> ResponseEntity.ok().body(updatedCustomer))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> deleteCustomer(@PathVariable String customerId){
        return customersServiceClient.deleteCustomer(customerId).then(Mono.just(ResponseEntity.noContent().<CustomerResponseDTO>build()))
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

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @DeleteMapping("/{customerId}/photo")
    public Mono<ResponseEntity<CustomerResponseDTO>> deleteCustomerPhoto(@PathVariable String customerId) {
        return customersServiceClient.deleteCustomerPhoto(customerId)
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
