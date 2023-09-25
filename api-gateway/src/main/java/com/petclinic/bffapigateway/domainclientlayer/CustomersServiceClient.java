package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import com.petclinic.bffapigateway.dtos.Vets.VetDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.just;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@Component
public class CustomersServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String customersServiceUrl;

    public CustomersServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort;
    }


    public Mono<OwnerResponseDTO> getOwner(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl+ "/owners/" + ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Flux<OwnerResponseDTO> getAllOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/owners")
                .retrieve()
                .bodyToFlux(OwnerResponseDTO.class);
    }

    /*
    public Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTOMono , String ownerId){

            return webClientBuilder.build().put()
                    .uri(customersServiceUrl + ownerId)
                    .body(Mono.just(ownerRequestDTOMono), OwnerRequestDTO.class)
                    .retrieve().bodyToMono(OwnerResponseDTO.class);
    }

     */
/*
    public Mono<OwnerResponseDTO> updateOwner(String ownerId, OwnerRequestDTO ownerRequest) {
        return webClientBuilder.build()
                .put()
                .uri(customersServiceUrl + ownerId)
                .body(BodyInserters.fromValue(ownerRequest))
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

 */

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





    public Flux<OwnerResponseDTO> createOwners (){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
               .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(OwnerResponseDTO.class);

    }

    public Mono<OwnerResponseDTO> createOwner (OwnerResponseDTO model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + "/owners")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), OwnerResponseDTO.class)
                .retrieve().bodyToMono(OwnerResponseDTO.class);

    }

    public Flux<PetType> getPetTypes (){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/petTypes")
                .retrieve()
                .bodyToFlux(PetType.class);
    }
    public Flux<PetResponseDTO> getAllPets(){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pet")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPetByPetId(String petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/pet/" + petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> getPet(final int ownerId, final int petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/" + petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Flux<PetResponseDTO> getPetsByOwnerId(final String ownerId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets")
                .retrieve()
                .bodyToFlux(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> createPet(PetResponseDTO model, final String ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"{ownerId}/pets", ownerId)
                .body(just(model), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> deletePet(final String ownerId, final int petId){
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "{ownerId}/pets/{petId}", ownerId ,petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }


    public Mono<OwnerResponseDTO> deleteOwner (final long ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Mono<String> setOwnerPhoto(PhotoDetails file, int id){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/photo/" + id)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
    }

    /*public Mono<PhotoDetails> getOwnerPhoto(int id){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl +"/photo/" + id)
                .retrieve()
                .bodyToMono(PhotoDetails.class);
    }*/

    public Mono<Void> deleteOwnerPhoto(int photoId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "/photo/" + photoId)
                .retrieve()
                .bodyToMono(Void.class);
    }



    public Mono<String> setPetPhoto(String ownerId, PhotoDetails file, String petId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + petId)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<PhotoDetails> getPetPhoto(String ownerId, String petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + petId)
                .retrieve()
                .bodyToMono(PhotoDetails.class);
    }

    public Mono<Void> deletePetPhoto (int ownerId, int photoId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + photoId)
                .retrieve()
                .bodyToMono(Void.class);
    }


}
