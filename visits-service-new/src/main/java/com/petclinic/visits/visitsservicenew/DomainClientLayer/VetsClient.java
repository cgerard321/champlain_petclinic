package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class VetsClient {

    private final WebClient webClient;
    private final String vetClientServiceBaseURL;

    public VetsClient(@Value("${app.vet-service.host}") String vetServiceHost,
                        @Value("${app.vet-service.port}") String vetServicePort){

        vetClientServiceBaseURL = "http://" + vetServiceHost + ":" + vetServicePort + "/vets";

        this.webClient = WebClient.builder()
                .baseUrl(vetClientServiceBaseURL)
                .build();

    }

    public Mono<VetDTO> getVetByVetId(String vetId) {
        Mono<VetDTO> vetDTOMono =
                webClient
                        .get()
                        .uri(vetClientServiceBaseURL + "/{vetId}", vetId)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error -> {
                            HttpStatusCode statusCode = error.statusCode();
                            if(statusCode.equals(HttpStatus.NOT_FOUND))
                                return Mono.error(new NotFoundException("No veterinarian was found with vetId: " + vetId));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, error ->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(VetDTO.class);

        return vetDTOMono;
    }
}
