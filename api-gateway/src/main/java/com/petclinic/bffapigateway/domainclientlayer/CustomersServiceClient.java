package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.*;

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


    public Mono<OwnerDetails> getOwner(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

    public Flux<OwnerDetails> getOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl)
                .retrieve()
                .bodyToFlux(OwnerDetails.class);
    }

    public Mono<OwnerDetails> updateOwner(String ownerId, OwnerDetails od){

            return webClientBuilder.build().put()
                    .uri(customersServiceUrl + ownerId)
                    .body(Mono.just(od), OwnerDetails.class)
                    .retrieve().bodyToMono(OwnerDetails.class);
    }




    public Flux<OwnerDetails> createOwners (){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(OwnerDetails.class);

    }

    public Mono<OwnerDetails> createOwner (OwnerDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), OwnerDetails.class)
                .retrieve().bodyToMono(OwnerDetails.class);

    }

    public Flux<PetType> getPetTypes (){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/petTypes")
                .retrieve()
                .bodyToFlux(PetType.class);
    }

    public Mono<PetDetails> getPet(final String ownerId, final String petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/" + petId)
                .retrieve()
                .bodyToMono(PetDetails.class);
    }

    public Mono<PetDetails> createPet(PetDetails model, final String ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"{ownerId}/pets", ownerId)
                .body(just(model), PetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetDetails.class);
    }

    public Mono<PetDetails> deletePet(final String ownerId, final String petId){
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "{ownerId}/pets/{petId}", ownerId ,petId)
                .retrieve()
                .bodyToMono(PetDetails.class);
    }


    public Mono<OwnerDetails> deleteOwner (final String ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

    public Mono<String> setOwnerPhoto(PhotoDetails file, String id){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/photo/" + id)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<PhotoDetails> getOwnerPhoto(String id){
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



    public Mono<String> setPetPhoto(String ownerId, PhotoDetails file, String id){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + id)
                .body(just(file), PhotoDetails.class)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<PhotoDetails> getPetPhoto(String ownerId, String id){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + id)
                .retrieve()
                .bodyToMono(PhotoDetails.class);
    }

    public Mono<Void> deletePetPhoto (String ownerId, int photoId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId + "/pets/photo/" + photoId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
