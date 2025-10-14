package com.petclinic.visits.visitsservicenew.DomainClientLayer;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Access point to the Vets
 */
@Service
public class VetsClient {

    private final WebClient webClient;
    private final String vetClientServiceBaseURL;

    public VetsClient(@Value("${app.vet-service.host}") String vetServiceHost,
                      @Value("${app.vet-service.port}") String vetServicePort) {

        vetClientServiceBaseURL = "http://" + vetServiceHost + ":" + vetServicePort + "/vets";

        this.webClient = WebClient.builder()
                .baseUrl(vetClientServiceBaseURL)
                .build();

    }

    public Flux<VetDTO> getAllVets() {
        return webClient
                .get()
                .uri(vetClientServiceBaseURL)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error ->
                        Mono.error(new IllegalArgumentException("Error fetching vets"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Server error fetching vets"))
                )
                .bodyToFlux(VetDTO.class);
    }


    /**
     * We are accessing the vet-service/src/main/java/com/petclinic/vet/servicelayer/VetServiceImpl.java --  getVetByVetId()
     * @param vetId Vet ID to search for
     * @return The pet response DTO of the string we searched
     */
    public Mono<VetDTO> getVetByVetId(String vetId) {
                return webClient
                        .get()
                        .uri(vetClientServiceBaseURL + "/{vetId}", vetId)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error -> {
                            HttpStatusCode statusCode = error.statusCode();
                            if (Objects.equals(statusCode, HttpStatus.NOT_FOUND))
                                return Mono.error(new NotFoundException("No veterinarian was found with vetId: " + vetId));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, error ->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(VetDTO.class);
    }
}
