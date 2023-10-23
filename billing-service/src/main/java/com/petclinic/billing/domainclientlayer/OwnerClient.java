package com.petclinic.billing.domainclientlayer;

import com.petclinic.billing.datalayer.OwnerResponseDTO;
import com.petclinic.billing.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.rmi.ServerException;

@Service
public class OwnerClient {
    private final WebClient webClient;
    private final String ownerClientServiceBaseURL;

    OwnerClient(@Value("${app.customers-service.host}") String ownerServiceHost,
                @Value("${app.customers-service.port}") String ownerServicePort) {
        ownerClientServiceBaseURL = "http://" + ownerServiceHost + ":" + ownerServicePort + "/owners";
        this.webClient = WebClient.builder()
                .baseUrl(ownerClientServiceBaseURL).build();
    }

public Mono<OwnerResponseDTO> getOwnerByOwnerId(final String ownerId) {
    return this.webClient
            .get()
            .uri("/{ownerId}", ownerId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                    return Mono.error(new NotFoundException("Owner not found with ownerId: " + ownerId));
                } else {
                    return Mono.error(new IllegalArgumentException("Client error for ownerId: " + ownerId));
                }
            })
            .onStatus(HttpStatus::is5xxServerError, serverResponse ->
                    Mono.error(new ServerException("Server error for ownerId: " + ownerId))
            )
            .bodyToMono(OwnerResponseDTO.class);
}

}
