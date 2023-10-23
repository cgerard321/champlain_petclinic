package com.petclinic.billing.domainclientlayer;

import com.petclinic.billing.datalayer.VetResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class VetClient {

    private final WebClient webClient;

    private final String vetClientServiceBaseURL;

    VetClient(@Value("${app.vet-service.host}") String vetServiceHost,
              @Value("${app.vet-service.port}") String vetServicePort) {
        vetClientServiceBaseURL = "http://" + vetServiceHost + ":" + vetServicePort + "/vets";

        this.webClient = WebClient.builder()
                .baseUrl(vetClientServiceBaseURL)
                .build();
    }

    public Mono<VetResponseDTO> getVetByVetId(final String vetId) {
        return this.webClient
                .get()
                .uri("/{vetId}", vetId)
                .retrieve()
                .bodyToMono(VetResponseDTO.class);
    }
}
