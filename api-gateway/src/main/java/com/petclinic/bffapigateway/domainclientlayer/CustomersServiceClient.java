package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.net.URI;

import java.util.Optional;

@Slf4j
@Component
public class CustomersServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String customersServiceUrl;

    public CustomersServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort) {
        this.webClientBuilder = webClientBuilder;
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort;
    }

    public Mono<CustomerResponseDTO> getCustomer(final String customerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/customers/" + customerId)
                .retrieve()
                .bodyToMono(CustomerResponseDTO.class);
    }

    public Mono<CustomerResponseDTO> getCustomer(final String customerId, boolean includePhoto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/customers/" + customerId);
        builder.queryParam("includePhoto", includePhoto);
        
        return webClientBuilder.build().get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(CustomerResponseDTO.class);
    }

    public Flux<CustomerResponseDTO> getAllCustomers() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/customers")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(CustomerResponseDTO.class);
    }

    public Flux<CustomerResponseDTO> getCustomersByPagination(Optional<Integer> page, Optional<Integer> size, String customerId, String firstName, String lastName, String phoneNumber, String city) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/customers/customers-pagination");

        builder.queryParam("page", page);
        builder.queryParam("size",size);

        // Add query parameters conditionally if they are not null or empty
        if (customerId != null && !customerId.isEmpty()) {
            builder.queryParam("customerId", customerId);
        }
        if (firstName != null && !firstName.isEmpty()) {
            builder.queryParam("firstName", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            builder.queryParam("lastName", lastName);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            builder.queryParam("phoneNumber", phoneNumber);
        }
        if (city != null && !city.isEmpty()) {
            builder.queryParam("city", city);
        }

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(CustomerResponseDTO.class);
    }

    public Mono<Long> getTotalNumberOfCustomers(){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/customers/customers-count")
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<Long> getTotalNumberOfCustomersWithFilters(String customerId, String firstName, String lastName, String phoneNumber, String city){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/customers/customers-filtered-count");

        // Add query parameters conditionally if they are not null or empty
        if (customerId != null && !customerId.isEmpty()) {
            builder.queryParam("customerId", customerId);
        }
        if (firstName != null && !firstName.isEmpty()) {
            builder.queryParam("firstName", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            builder.queryParam("lastName", lastName);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            builder.queryParam("phoneNumber", phoneNumber);
        }
        if (city != null && !city.isEmpty()) {
            builder.queryParam("city", city);
        }

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(Long.class);
    }


    public Mono<CustomerResponseDTO> updateCustomer(String customerId, Mono<CustomerRequestDTO> customerRequestDTOMono) {
        return customerRequestDTOMono.flatMap(requestDTO ->
                webClientBuilder.build()
                        .put()
                        .uri(customersServiceUrl + "/customers/" + customerId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(CustomerResponseDTO.class)
        );
    }

    public Flux<CustomerResponseDTO> createCustomers() {
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(CustomerResponseDTO.class);
    }

    public Mono<CustomerResponseDTO> createCustomer(Mono<CustomerRequestDTO> model) {
        return model.flatMap(requestDTO ->
                webClientBuilder.build()
                        .post()
                        .uri(customersServiceUrl + "/customers")
                        .bodyValue(requestDTO)
                        .retrieve()
                        .bodyToMono(CustomerResponseDTO.class)
        );
    }

    public Flux<PetTypeResponseDTO> getPetTypes() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(PetTypeResponseDTO.class);
    }

    public Flux<PetResponseDTO> getAllPets() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pets")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPetByPetId(String petId, boolean includePhoto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/pets/" + petId);
        builder.queryParam("includePhoto", includePhoto);

        return webClientBuilder.build().get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPet(final String ownerId, final String petId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/" + ownerId + "/pets/" + petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Flux<PetResponseDTO> getPetsByOwnerId(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pets/owner/" + ownerId +"/pets")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> addPet(Mono<PetRequestDTO> model) {
        return model.flatMap(requestDTO ->
                webClientBuilder.build()
                        .post()
                        .uri(customersServiceUrl + "/pets")
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(PetResponseDTO.class));
    }

    public Mono<PetResponseDTO> updatePet(Mono<PetRequestDTO> petRequestDTO, String petId) {
        return petRequestDTO.flatMap(requestDTO ->
                webClientBuilder.build().put()
                        .uri(customersServiceUrl + "/pets/" + petId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(PetResponseDTO.class));
    }

    public Mono<PetResponseDTO> patchPet(String isActive, String petId) {
        URI uri = UriComponentsBuilder
                .fromUriString(customersServiceUrl + "/pets/{petId}/active")
                .queryParam("isActive", isActive)
                .buildAndExpand(petId)
                .toUri();

        return webClientBuilder.build()
                .patch()
                .uri(uri)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> deletePet(final String ownerId, final String petId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "{ownerId}/pets/{petId}", ownerId, petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> deletePetByPetId(final String petId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "/pets/{petId}", petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }


    public Mono<CustomerResponseDTO> deleteCustomer(final String customerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl +"/customers/"+ customerId)
                .retrieve()
                .bodyToMono(CustomerResponseDTO.class);
    }


    public Mono<Void> deletePetPhoto(int ownerId, int photoId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + photoId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(PetTypeResponseDTO.class);
    }
    public Mono<PetTypeResponseDTO> getPetTypeByPetTypeId(String petTypeId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes/" + petTypeId)
                .retrieve()
                .bodyToMono(PetTypeResponseDTO.class);
    }

    public Mono<PetTypeResponseDTO> deletePetType(final String petTypeId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl +"/owners/petTypes/"+ petTypeId)
                .retrieve()
                .bodyToMono(PetTypeResponseDTO.class);
    }

    public Mono<Void> deletePetTypeV2(final String petTypeId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl +"/owners/petTypes/"+ petTypeId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<PetTypeResponseDTO> addPetType(Mono<PetTypeRequestDTO> petTypeRequestDTOMono) {
        return petTypeRequestDTOMono.flatMap(requestDTO ->
                webClientBuilder.build()
                        .post()
                        .uri(customersServiceUrl + "/owners/petTypes")
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(PetTypeResponseDTO.class)
        );
    }


    public Mono<PetTypeResponseDTO> updatePetType(String petTypeId, Mono<PetTypeRequestDTO> petTypeRequestDTO) {
        return petTypeRequestDTO.flatMap(requestDTO ->
                webClientBuilder.build()
                        .put()
                        .uri(customersServiceUrl + "/owners/petTypes/" + petTypeId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(PetTypeResponseDTO.class)
        );
    }

    public Mono<PetResponseDTO> createPetForOwner(String ownerId, PetRequestDTO petRequest) {
        return webClientBuilder.build()
                .post()
                .uri(customersServiceUrl + "/pets/owners/" + ownerId + "/pets")
                .body(BodyInserters.fromValue(petRequest))
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Flux<PetTypeResponseDTO> getPetTypesByPagination(Optional<Integer> page, Optional<Integer> size, String petTypeId, String name, String description) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/petTypes/pet-types-pagination");

        builder.queryParam("page", page);
        builder.queryParam("size", size);

        // Add query parameters conditionally if they are not null or empty
        if (petTypeId != null && !petTypeId.isEmpty()) {
            builder.queryParam("petTypeId", petTypeId);
        }
        if (name != null && !name.isEmpty()) {
            builder.queryParam("name", name);
        }
        if (description != null && !description.isEmpty()) {
            builder.queryParam("description", description);
        }

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(PetTypeResponseDTO.class);
    }

    public Mono<Long> getTotalNumberOfPetTypes(){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes/pet-types-count")
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<Long> getTotalNumberOfPetTypesWithFilters(String petTypeId, String name, String description){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/petTypes/pet-types-filtered-count");

        // Add query parameters conditionally if they are not null or empty
        if (petTypeId != null && !petTypeId.isEmpty()) {
            builder.queryParam("petTypeId", petTypeId);
        }
        if (name != null && !name.isEmpty()) {
            builder.queryParam("name", name);
        }
        if (description != null && !description.isEmpty()) {
            builder.queryParam("description", description);
        }

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<CustomerResponseDTO> updateCustomerPhoto(String customerId, Mono<FileDetails> photoMono) {
        return webClientBuilder.build().patch()
                .uri(customersServiceUrl + "/customers/" + customerId + "/photo")
                .body(photoMono, FileDetails.class)
                .retrieve()
                .bodyToMono(CustomerResponseDTO.class);
    }
    public Mono<CustomerResponseDTO> deleteCustomerPhoto(String customerId) {
        return webClientBuilder.build()
                .delete()
                .uri(customersServiceUrl + "/customers/" + customerId + "/photo")
                .retrieve()
                .bodyToMono(CustomerResponseDTO.class);
    }

    public Mono<PetResponseDTO> addPetPhoto(String petId, Mono<FileDetails> photoMono) {
        return photoMono.flatMap(photo ->
                webClientBuilder.build()
                        .patch()
                        .uri(customersServiceUrl + "/pets/" + petId + "/photos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(photo))
                        .retrieve()
                        .bodyToMono(PetResponseDTO.class)
        );
    }

    public Mono<PetResponseDTO> deletePetPhoto(String petId) {
        return webClientBuilder.build()
                .patch()
                .uri(customersServiceUrl + "/pets/" + petId + "/photo")
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPet(final String ownerId, final String petId, boolean includePhoto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/" + ownerId + "/pets/" + petId);
        builder.queryParam("includePhoto", includePhoto);

        return webClientBuilder.build().get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

}
