package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Visits.*;
import com.petclinic.bffapigateway.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.IOException;
/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Component
@Slf4j
public class VisitsServiceClient {
    private final WebClient webClient;

    @Autowired
    public VisitsServiceClient(
            @Value("${app.visits-service-new.host}") String visitsServiceHost,
            @Value("${app.visits-service-new.port}") String visitsServicePort
    ) {
        this.webClient = WebClient.builder()
                .baseUrl("http://" + visitsServiceHost + ":" + visitsServicePort + "/visits")
                .build();
    }

    public Flux<VisitResponseDTO> getAllVisits(){
        return this.webClient
                .get()
                .uri("")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> Mono.error(new IllegalArgumentException("Something went wrong and we got a 400 error")))
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Something went wrong and we got a 500 error")))
                .bodyToFlux(VisitResponseDTO.class);
    }

    public Flux<VisitResponseDTO> getVisitsForStatus(final String status){
        return webClient
                .get()
                .uri("/status/{status}", status)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
    }


    public Flux<VisitResponseDTO> getVisitsForPet(final String petId){
        return webClient
                .get()
                .uri("/pets/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
    }
    public Flux<VisitResponseDTO> getVisitByPractitionerId(final String practitionerId){
        return webClient
                .get()
                .uri("/practitioner/{practitionerId}", practitionerId)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
    }


    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return webClient
                .get()
                .uri("/{visitId}", visitId)
                .retrieve()
                .bodyToMono(VisitResponseDTO.class);
    }



    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status) {

        Status newStatus = switch (status) {
            case "CONFIRMED" -> Status.CONFIRMED;
            case "COMPLETED" -> Status.COMPLETED;
            default -> Status.CANCELLED;
        };

        return webClient
            .put()
            .uri("/"+ visitId +"/status/" + newStatus)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(VisitResponseDTO.class);
    }


    public Mono<VisitResponseDTO> createVisitForPet(VisitRequestDTO visit) {
        return webClient
            .post()
            .uri("")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(visit), VisitRequestDTO.class)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> {
                HttpStatusCode httpStatus = response.statusCode();
                return response.bodyToMono(String.class)
                    .flatMap(errorMessage -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode errorNode = objectMapper.readTree(errorMessage);
                            String message = errorNode.get("message").asText();

                            if (httpStatus == HttpStatus.NOT_FOUND) {
                                return Mono.error(new NotFoundException(message));
                            } else {
                                return Mono.error(new BadRequestException(message));
                            }
                        } catch (IOException e) {
                            // Handle parsing error
                            return Mono.error(new BadRequestException("Bad Request"));
                        }
                    });
            })
            .bodyToMono(VisitResponseDTO.class);
    }

    public Mono<Void> deleteVisitByVisitId(String visitId){
        return webClient
                .delete()
                .uri("/{visitId}", visitId)
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Mono<Void> deleteAllCancelledVisits(){
        return webClient
                .delete()
                .uri("/cancelled")
                .retrieve()
                .bodyToMono(Void.class);
    }

    /*
    public Flux<VisitDetails> getPreviousVisitsForPet(final String petId) {
        return webClient
                .get()
                .uri("/visits/previous/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }



    public Flux<VisitDetails> getScheduledVisitsForPet(final String petId) {
        return webClient
                .get()
                .uri("/visits/scheduled/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }
*/
//    public Mono<VisitDetails> updateVisitForPet(VisitDetails visit) {
//        URI uri = UriComponentsBuilder
//                .fromPath("/owners/*/pets/{petId}/visits/{visitId}")
//                .buildAndExpand(visit.getPetId(), visit.getVisitId())
//                .toUri();
/*
        return webClient
                .put()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response  -> Mono.error(new BadRequestException("Failed to update visit")))
                .bodyToMono(VisitDetails.class);
    }

    private String joinIds(List<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }


    public Mono<Visits> getVisitsForPets(final List<Integer> petIds) {
        return this.webClient
                .get()
                .uri("/pets/visits?petId={petId}", joinIds(petIds))
                .retrieve()
                .bodyToMono(Visits.class);
    }

    public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(final int practitionerId, final String startDate, final String endDate) {
        return webClient
                .get()
                .uri("/visits/calendar/{practitionerId}?dates={startDate},{endDate}", practitionerId, startDate, endDate)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

        return webClient
                .post()
                .uri("/visits")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);

    }*/
}

