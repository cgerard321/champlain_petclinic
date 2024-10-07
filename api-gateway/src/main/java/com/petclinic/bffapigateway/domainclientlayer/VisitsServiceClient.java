package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.BadRequestException;
import com.petclinic.bffapigateway.exceptions.DuplicateTimeException;
import com.petclinic.bffapigateway.exceptions.InvalidInputsInventoryException;
import com.petclinic.bffapigateway.utils.Rethrower;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Slf4j
@Component
public class VisitsServiceClient {
    private final WebClient webClient;
    private final String reviewUrl;

    private Rethrower rethrower;

    @Autowired
    public VisitsServiceClient(
            @Value("${app.visits-service-new.host}") String visitsServiceHost,
            @Value("${app.visits-service-new.port}") String visitsServicePort
    ) {
        reviewUrl = "http://" + visitsServiceHost + ":" + visitsServicePort + "/visits";
        this.webClient = WebClient.builder()
                .baseUrl(reviewUrl)
                .build();
    }

    public Flux<VisitResponseDTO> getAllVisits(String description){
        return this.webClient
                .get()
                .uri(uriBuilder -> {
                    if (description != null) {
                        uriBuilder.queryParam("description", description).build();
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> Mono.error(new IllegalArgumentException("Something went wrong and we got a 400 error")))
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Something went wrong and we got a 500 error")))
                .bodyToFlux(VisitResponseDTO.class);
    }

    public Flux<VisitResponseDTO> getVisitsForStatus(final String status) {
        return webClient
                .get()
                .uri("/status/{status}", status)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
    }


    public Flux<VisitResponseDTO> getVisitsForPet(final String petId) {
        return webClient
                .get()
                .uri("/pets/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
    }

    public Flux<VisitResponseDTO> getVisitByPractitionerId(final String practitionerId) {
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

    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTO) {
        return visitRequestDTO.flatMap(visitRequestDTO1 -> {
            if (visitRequestDTO1.getVisitDate() != null) {
                LocalDateTime originalDate = visitRequestDTO1.getVisitDate();
                LocalDateTime adjustedDate = originalDate.minusHours(4);
                visitRequestDTO1.setVisitDate(adjustedDate);
            } else {
                throw new BadRequestException("Visit date is required");
            }
            return webClient
                    .post()
                    .uri(reviewUrl)
                    .body(BodyInserters.fromValue(visitRequestDTO1))
                    .retrieve()
                    .bodyToMono(VisitResponseDTO.class);
        });
    }

    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status) {

        Status newStatus = switch (status) {
            case "CONFIRMED" -> Status.CONFIRMED;
            case "COMPLETED" -> Status.COMPLETED;
            case "ARCHIVED" -> Status.ARCHIVED;
            default -> Status.CANCELLED;
        };

        return webClient
                .put()
                .uri("/" + visitId + "/status/" + newStatus)
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
                                    } else if (httpStatus == HttpStatus.CONFLICT) {
                                        return Mono.error(new DuplicateTimeException(message));
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

    public Mono<Void> deleteVisitByVisitId(String visitId) {
        return webClient
                .delete()
                .uri("/{visitId}", visitId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteAllCancelledVisits() {
        return webClient
                .delete()
                .uri("/cancelled")
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<VisitResponseDTO> archiveCompletedVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTO) {
        return webClient
                .put()
                .uri("/completed/" + visitId + "/archive")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromPublisher(visitRequestDTO, VisitRequestDTO.class))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new BadRequestException(errorBody))))
                .bodyToMono(VisitResponseDTO.class);
    }
    public Flux<VisitResponseDTO> getAllArchivedVisits() {
        return webClient
                .get()
                .uri("/archived")
//                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(VisitResponseDTO.class);
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


    public Flux<ReviewResponseDTO> getAllReviews() {
        return webClient
                .get()
                .uri(reviewUrl + "/reviews")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ReviewResponseDTO.class);
    }

    public Mono<ReviewResponseDTO> createReview(Mono<ReviewRequestDTO> model) {
        String reviewId = UUID.randomUUID().toString();
        return model.flatMap(reviewRequestDTO -> {
            return webClient
                    .post()
                    .uri(reviewUrl + "/reviews")
                    .body(BodyInserters.fromValue(reviewRequestDTO))
                    .retrieve()
                    .bodyToMono(ReviewResponseDTO.class);
        });

    }

    public Mono<ReviewResponseDTO> updateReview(String reviewId, Mono<ReviewRequestDTO> reviewRequestDTO) {
        return reviewRequestDTO.flatMap(requestDTO ->
                webClient
                        .put()
                        .uri(reviewUrl + "/reviews/" + reviewId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(ReviewResponseDTO.class)
        );
    }

    public Mono<ReviewResponseDTO> getReviewByReviewId(String reviewId) {
        return webClient
                .get()
                .uri(reviewUrl + "/reviews/" + reviewId)
                .retrieve()
                .bodyToMono(ReviewResponseDTO.class);
    }

    public Mono<VisitResponseDTO> updateVisitByVisitId(String visitId,
                                                       Mono<VisitRequestDTO> visitRequestDTO) {
        return visitRequestDTO.flatMap(requestDTO -> {
            if (requestDTO.getVisitDate() != null) {
                LocalDateTime originalDate = requestDTO.getVisitDate();
                LocalDateTime adjustedDate = originalDate.minusHours(4);
                requestDTO.setVisitDate(adjustedDate);
            } else {
                throw new BadRequestException("Visit date is required");
            }
            return webClient
                    .put()
                    .uri(reviewUrl + "/" + visitId)
                    .body(BodyInserters.fromValue(requestDTO))
                    .retrieve()
                    .bodyToMono(VisitResponseDTO.class);
        });
    }


    //Emergency
    public Flux<EmergencyResponseDTO> getAllEmergency(){
        return webClient
                .get()
                .uri(reviewUrl + "/emergency")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(EmergencyResponseDTO.class);
    }

    public Mono<EmergencyResponseDTO> createEmergency(Mono<EmergencyRequestDTO> model) {
        String emergencyId= UUID.randomUUID().toString();
        return model.flatMap(emergencyRequestDTO -> {
            return webClient
                    .post()
                    .uri(reviewUrl + "/emergency")
                    .body(BodyInserters.fromValue(emergencyRequestDTO))
                    .retrieve()
                    .bodyToMono(EmergencyResponseDTO.class);
        });

    }

    public Mono<EmergencyResponseDTO> updateEmergency(String emergencyId, Mono<EmergencyRequestDTO> emergencyRequestDTOMono) {
        return emergencyRequestDTOMono.flatMap(requestDTO ->
                webClient
                        .put()
                        .uri(reviewUrl + "/emergency/" + emergencyId)
                        .body(BodyInserters.fromValue(requestDTO))
                        .retrieve()
                        .bodyToMono(EmergencyResponseDTO.class)
        );
    }

    public Mono<EmergencyResponseDTO> getEmergencyByEmergencyId(String emergencyId) {
        return webClient
                .get()
                .uri(reviewUrl + "/emergency/" + emergencyId)
                .retrieve()
                .bodyToMono(EmergencyResponseDTO.class);
    }

    public Mono<EmergencyResponseDTO> deleteEmergency(String emergencyId) {
        return webClient
                .delete()
                .uri(reviewUrl + "/emergency/" + emergencyId)
                .retrieve()
                .bodyToMono(EmergencyResponseDTO.class);
    }

    public Mono<VisitResponseDTO> patchVisitStatus(String visitId, String status) {
        return webClient.patch()
                .uri(reviewUrl + "/" + visitId + "/" + status) // Adjust URI based on visit-service
                .retrieve()
                .bodyToMono(VisitResponseDTO.class); // Parse response into VisitResponseDTO
    }

    public Mono<ReviewResponseDTO> deleteReview(String reviewId){
        return webClient.delete()
                .uri(reviewUrl + "/reviews/" + reviewId)
                .retrieve()
                .bodyToMono(ReviewResponseDTO.class);
    }
}

