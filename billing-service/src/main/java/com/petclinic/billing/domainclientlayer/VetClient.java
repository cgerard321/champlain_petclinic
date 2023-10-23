package com.petclinic.billing.domainclientlayer;

import com.petclinic.billing.datalayer.VetResponseDTO;
import com.petclinic.billing.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.rmi.ServerException;

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
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new NotFoundException("Vet not found with vetId: " + vetId));
                    } else {
                        return Mono.error(new IllegalArgumentException("Client error for vetId: " + vetId));
                    }
                })
                .onStatus(HttpStatus::is5xxServerError, serverResponse ->
                        Mono.error(new ServerException("Server error for vetId: " + vetId))
                )
                .bodyToMono(VetResponseDTO.class);
    }
}
