package com.petclinic.visits.visitsservicenew.DomainClientLayer;


import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Access point to the Pets
 */
@Service
public class PetsClient {

    private final WebClient webClient;
    private final String petClientServiceBaseURL;

    public PetsClient(@Value("${app.customers-service-reactive.host}") String petsServiceHost,
                      @Value("${app.customers-service-reactive.port}") String petsServicePort){

        petClientServiceBaseURL = "http://" + petsServiceHost + ":" + petsServicePort + "/pets";

        this.webClient = WebClient.builder()
                .baseUrl(petClientServiceBaseURL)
                .build();

        }

    /**
     * We are accessing customers-service-reactive/src/main/java/com.petclinic.customerservice/business/PetServiceImpl
     * @param petId Pet ID to search for
     * @return The pet response DTO of the string we searched
     */
    public Mono<PetResponseDTO> getPetById( final String petId){
            return webClient
                    .get()
                    .uri(petClientServiceBaseURL + "/{petId}",petId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, error -> {
                        HttpStatusCode statusCode = error.statusCode();
                        if (statusCode.equals(HttpStatus.NOT_FOUND))
                            return Mono.error(new NotFoundException("No pet was found with petId: " + petId));
                        return Mono.error(new IllegalArgumentException("Something went wrong"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, error ->
                            Mono.error(new IllegalArgumentException("Something went wrong"))
                    )
                    .bodyToMono(PetResponseDTO.class);
        }
    }


