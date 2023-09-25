package com.petclinic.billing.domainclientlayer;

import com.petclinic.billing.datalayer.OwnerResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OwnerClient {
    private final WebClient webClient;
    private final String ownerClientServiceBaseURL;

    private OwnerClient(@Value("${app.customers-service.host}") String ownerServiceHost,
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
                .bodyToMono(OwnerResponseDTO.class);
    }
}
