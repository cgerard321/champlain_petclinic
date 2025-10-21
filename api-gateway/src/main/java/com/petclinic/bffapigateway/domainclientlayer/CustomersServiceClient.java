package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
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

    public Mono<OwnerResponseDTO> getOwner(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/" + ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Mono<OwnerResponseDTO> getOwner(final String ownerId, boolean includePhoto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/" + ownerId);
        builder.queryParam("includePhoto", includePhoto);
        
        return webClientBuilder.build().get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Flux<OwnerResponseDTO> getAllOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners")
                .retrieve()
                .bodyToFlux(OwnerResponseDTO.class);
    }

    public Flux<OwnerResponseDTO> getOwnersByPagination(Optional<Integer> page, Optional<Integer> size, String ownerId, String firstName, String lastName, String phoneNumber, String city) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/owners-pagination");

        builder.queryParam("page", page);
        builder.queryParam("size",size);

        // Add query parameters conditionally if they are not null or empty
        if (ownerId != null && !ownerId.isEmpty()) {
            builder.queryParam("ownerId", ownerId);
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
                .bodyToFlux(OwnerResponseDTO.class);
    }

    public Mono<Long> getTotalNumberOfOwners(){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/owners-count")
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<Long> getTotalNumberOfOwnersWithFilters(String ownerId, String firstName, String lastName, String phoneNumber, String city){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/owners-filtered-count");

        // Add query parameters conditionally if they are not null or empty
        if (ownerId != null && !ownerId.isEmpty()) {
            builder.queryParam("ownerId", ownerId);
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


    public Mono<OwnerResponseDTO> updateOwner(String ownerId, Mono<OwnerRequestDTO> ownerRequestDTO) {
        return ownerRequestDTO.flatMap(requestDTO ->
                webClientBuilder.build()
                        .put()
                        .uri(customersServiceUrl + "/owners/" + ownerId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(OwnerResponseDTO.class)
        );
    }

    public Flux<OwnerResponseDTO> createOwners() {
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToFlux(OwnerResponseDTO.class);
    }

    public Mono<OwnerResponseDTO> createOwner(Mono<OwnerRequestDTO> model) {
        return model.flatMap(requestDTO ->
                webClientBuilder.build()
                        .post()
                        .uri(customersServiceUrl + "/owners")
                        .bodyValue(requestDTO)
                        .retrieve()
                        .bodyToMono(OwnerResponseDTO.class)
        );
    }

    public Flux<PetTypeResponseDTO> getPetTypes() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes")
                .retrieve()
                .bodyToFlux(PetTypeResponseDTO.class);
    }

    public Flux<PetResponseDTO> getAllPets() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pet")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPetByPetId(String petId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pets/" + petId)
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

    public Mono<PetResponseDTO> deletePetByPetIdV2(final String petId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "/pets/{petId}/v2", petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<OwnerResponseDTO> deleteOwner(final String ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl +"/owners/"+ ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
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

    public Mono<OwnerResponseDTO> updateOwnerPhoto(String ownerId, Mono<FileDetails> photoMono) {
        return webClientBuilder.build().patch()
                .uri(customersServiceUrl + "/owners/" + ownerId + "/photo")
                .body(photoMono, FileDetails.class)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

}
