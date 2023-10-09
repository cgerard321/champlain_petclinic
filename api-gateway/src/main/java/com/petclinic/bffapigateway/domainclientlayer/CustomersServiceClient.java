package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.Objects;

import java.util.Optional;

import static reactor.core.publisher.Mono.just;

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

    public Flux<OwnerResponseDTO> getAllOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners")
                .retrieve()
                .bodyToFlux(OwnerResponseDTO.class);
    }

    public Flux<OwnerResponseDTO> getOwnersByPagination(Optional<Integer> page, Optional<Integer> size) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/owners-pagination?page="+page.orElse(0)+"&size="+size.orElse(5))
                .retrieve()
                .bodyToFlux(OwnerResponseDTO.class);
    }

    public Mono<Long> getTotalNumberOfOwners(){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/owners-count")
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

    public Mono<OwnerResponseDTO> createOwner(OwnerRequestDTO model) {
        log.info("createOwner");

        if (Objects.isNull(model)) {
            log.info("model is null");
        } else {
            log.info("model is not null");
        }

                return webClientBuilder.build()
                        .post()
                        .uri(customersServiceUrl + "/owners")
                        .bodyValue(model)
                        .retrieve()
                        .bodyToMono(OwnerResponseDTO.class);
//        return webClientBuilder.build()
//                .post()
//                .uri(customersServiceUrl + "/owners")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(model, OwnerRequestDTO.class)
//                .retrieve()
//                .bodyToMono(OwnerResponseDTO.class);
    }

    public Flux<PetType> getPetTypes() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners/petTypes")
                .retrieve()
                .bodyToFlux(PetType.class);
    }

    public Flux<PetResponseDTO> getAllPets() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pet")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPetByPetId(String petId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pet/" + petId)
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
                .uri(customersServiceUrl + "/pet/owner/" + ownerId + "/pets")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> createPet(PetResponseDTO model, final String ownerId) {
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + "{ownerId}/pets", ownerId)
                .body(just(model), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> updatePet(PetResponseDTO model, final String petId) {
        return webClientBuilder.build().put()
                .uri(customersServiceUrl + "/pet/{petId}", petId)
                .body(just(model), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> patchPet(PetRequestDTO model, String petId) {
        return webClientBuilder.build().patch()
                .uri(customersServiceUrl + "/pet/{petId}", petId)
                .body(just(model), PetRequestDTO.class)
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
                .uri(customersServiceUrl + "/pet/{petId}", petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<OwnerResponseDTO> deleteOwner(final String ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl +"/owners/"+ ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Mono<String> setOwnerPhoto(PhotoDetails file, int id) {
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + "/photo/" + id)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
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
}
