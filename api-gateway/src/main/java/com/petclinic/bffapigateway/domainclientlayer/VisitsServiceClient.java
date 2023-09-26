package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Visits.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Component
public class VisitsServiceClient {
    private final WebClient webClient;

    @Autowired
    public VisitsServiceClient(
            @Value("${app.visits-service-new.host}") String visitsServiceHost,
            @Value("${app.visits-service-new.port}") String visitsServicePort
    ) {
        this.webClient = WebClient.builder()
                .baseUrl("http://" + visitsServiceHost + ":" + visitsServicePort)
                .build();
    }

    public Flux<VisitResponseDTO> getAllVisits(){
        return this.webClient
                .get()
                .uri("/visits")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
//                    HttpStatusCode statusCode = error.statusCode();
                    return Mono.error(new IllegalArgumentException("Something went wrong and got a 400 error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Something went wrong and we got a 500 error")))
                .bodyToFlux(VisitResponseDTO.class);
    }
    public Mono<Visits> getVisitsForPets(final List<Integer> petIds) {
        return this.webClient
                .get()
                .uri("/pets/visits?petId={petId}", joinIds(petIds))
                .retrieve()
                .bodyToMono(Visits.class);
    }

    public Flux<VisitDetails> getVisitsForPet(final int petId){
        return webClient
                .get()
                .uri("/visits/pets/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Flux<VisitDetails> getVisitsForStatus(final String status){
        return webClient
                .get()
                .uri("/visits/status/{status}", status)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Flux<VisitDetails> getPreviousVisitsForPet(final int petId) {
        return webClient
                .get()
                .uri("/visits/previous/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Flux<VisitDetails> getVisitForPractitioner(final int practitionerId){
        return webClient
                .get()
                .uri("visits/vets/{practitionerId}", practitionerId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    /*
    public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(final int practitionerId, final String startDate, final String endDate) {
        return webClient
                .get()
                .uri("/visits/calendar/{practitionerId}?dates={startDate},{endDate}", practitionerId, startDate, endDate)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }
     */

    public Flux<VisitDetails> getScheduledVisitsForPet(final int petId) {
        return webClient
                .get()
                .uri("/visits/scheduled/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return webClient
                .get()
                .uri("/visit/{visitId}", visitId)
                .retrieve()
                .bodyToMono(VisitResponseDTO.class);
    }

    public Mono<VisitDetails> updateVisitForPet(VisitDetails visit, Integer petId) {
        return webClient
                .put()
                .uri("/visits/pets/{visitId}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<VisitResponseDTO> updateVisitByVisitId(VisitRequestDTO visit, String visitId) {
        return webClient
                .put()
                .uri("/visits/{visitId}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitRequestDTO.class)
                .retrieve()
                .bodyToMono(VisitResponseDTO.class);
    }

    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status) {

        Status newStatus;
        switch (status){
            case "CONFIRMED":
                newStatus = Status.CONFIRMED;
                break;

            case "IN_PROGRESS":
                newStatus = Status.IN_PROGRESS;
                break;

            case "COMPLETED":
                newStatus = Status.COMPLETED;
                break;

            default:
                newStatus = Status.CANCELLED;
                break;
        }
        return webClient
                .put()
                .uri("/visits/"+ visitId +"/status/" + newStatus)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(VisitResponseDTO.class);
    }

    public Mono<VisitDetails> createVisitForPet(VisitDetails visit) {
        return webClient
                .post()
                .uri("/owners/*/pets/" + visit.getPetId() + "/visits")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<Void> deleteVisitByVisitId(String visitId){
        return webClient
                .delete()
                .uri("/visits/{visitId}", visitId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private String joinIds(List<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }



}

