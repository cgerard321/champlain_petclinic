package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/owners/";
    }


    public Mono<OwnerResponseDTO> getOwner(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerResponseDTO.class);
    }

    public Flux<OwnerResponseDTO> getOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl)
                .retrieve()
                .bodyToFlux(OwnerResponseDTO.class);
    }

    public Mono<OwnerResponseDTO> updateOwner(int ownerId, OwnerResponseDTO od){

            return webClientBuilder.build().put()
                    .uri(customersServiceUrl + ownerId)
                    .body(Mono.just(od), OwnerResponseDTO.class)
                    .retrieve().bodyToMono(OwnerResponseDTO.class);
    }




    public Flux<OwnerResponseDTO> createOwners (){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(OwnerResponseDTO.class);

    }

    public Mono<OwnerResponseDTO> createOwner (OwnerResponseDTO model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
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

    public Mono<PetResponseDTO> getPet(final int ownerId, final int petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/" + petId)
                .retrieve()
                .bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> createPet(PetResponseDTO model, final int ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"{ownerId}/pets", ownerId)
                .body(just(model), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetResponseDTO.class);
    }

    public Mono<PetResponseDTO> deletePet(final int ownerId, final int petId){
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

    public Mono<PhotoDetails> getOwnerPhoto(int id){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl +"/photo/" + id)
                .retrieve()
                .bodyToMono(PhotoDetails.class);
    }

    public Mono<Void> deleteOwnerPhoto(int photoId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "/photo/" + photoId)
                .retrieve()
                .bodyToMono(Void.class);
    }



    public Mono<String> setPetPhoto(int ownerId, PhotoDetails file, int id){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + id)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<PhotoDetails> getPetPhoto(int ownerId, int id){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + id)
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
