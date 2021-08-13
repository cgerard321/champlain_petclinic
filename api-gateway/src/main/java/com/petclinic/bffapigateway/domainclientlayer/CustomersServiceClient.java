package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@Component
@RequiredArgsConstructor
public class CustomersServiceClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<OwnerDetails> getOwner(final int ownerId) {
        return webClientBuilder.build().get()
                .uri("http://customers-service/owners/{ownerId}", ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

    public Mono<OwnerDetails[]> getOwners() {
        return webClientBuilder.build().get()
                .uri("http://customers-service/owners")
                .retrieve()
                .bodyToMono(OwnerDetails[].class);
    }
}
