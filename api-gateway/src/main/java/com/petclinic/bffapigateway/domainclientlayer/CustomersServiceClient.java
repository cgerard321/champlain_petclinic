package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.OwnerDetails;

import com.petclinic.bffapigateway.dtos.PetDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


    public Mono<OwnerDetails> getOwner(final int ownerId) {
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

    public Mono<OwnerDetails> updateOwner(int ownerId, OwnerDetails od){

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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    public Mono<PetDetails> createPet(final PetDetails model,final int ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/{ownerId}/pets", ownerId)
=======
    public Mono<PetDetails> createPet(final PetDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + "/pets")
>>>>>>> b6da7faa (Created client method and failing test for the endpoint of adding a pet)
=======
    public Mono<PetDetails> createPet(final int ownerId,final PetDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/{ownerId}" + ownerId + "/pets")
>>>>>>> 7ec8008a (Modified code so test passes)
=======
    public Mono<PetDetails> createPet(final PetDetails model,final int ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/{ownerId}/pets", ownerId)
>>>>>>> 0558a528 (Customer service is fked gg.)
=======
    public Mono<PetDetails> createNewPet(final PetDetails model,final int ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/{ownerId}/pets", ownerId)
                .body(just(model), PetDetails.class)
>>>>>>> cef5b393 (Ok, idk, help plz)
=======
    public Mono<PetDetails> createPet(final PetDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl + "/pets")
>>>>>>> c6febbaa (Created client method and failing test for the endpoint of adding a pet)
=======
    public Mono<PetDetails> createPet(final int ownerId,final PetDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/{ownerId}" + ownerId + "/pets")
>>>>>>> 0d55775b (Modified code so test passes)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetDetails.class);
    }

<<<<<<< HEAD
<<<<<<< HEAD

=======
>>>>>>> b6da7faa (Created client method and failing test for the endpoint of adding a pet)
=======
>>>>>>> c6febbaa (Created client method and failing test for the endpoint of adding a pet)



    public Mono<OwnerDetails> deleteOwner (final long ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

}
