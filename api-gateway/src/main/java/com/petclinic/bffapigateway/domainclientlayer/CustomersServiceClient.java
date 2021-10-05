package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Login;
import com.petclinic.bffapigateway.dtos.OwnerDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/owners";
    }


    public Mono<OwnerDetails> getOwner(final int ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/{ownerId}", ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

    public Flux<OwnerDetails> getOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl)
                .retrieve()
                .bodyToFlux(OwnerDetails.class);
    }

    public Mono<OwnerDetails> updateOwner(OwnerDetails od,final int ownerId){

            return webClientBuilder.build().put()
                    .uri(customersServiceUrl + "/{ownerId}", ownerId)
                    .retrieve().bodyToMono(OwnerDetails.class);
    }




    public Flux<OwnerDetails> createOwners (){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(OwnerDetails.class);

    }

    public Mono<OwnerDetails> createOwner (final OwnerDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl+ model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(OwnerDetails.class);

    }





}
