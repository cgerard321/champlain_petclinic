package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.VetDTO;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Component
public class VisitsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private String hostname;

    @Autowired
    public VisitsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.visits-service-new.host}") String visitsServiceHost,
            @Value("${app.visits-service-new.port}") String visitsServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        hostname = "http://" + visitsServiceHost + ":" + visitsServicePort;
    }

    public Mono<Visits> getVisitsForPets(final List<Integer> petIds) {
        return webClientBuilder.build()
                .get()
                .uri(hostname + "/pets/visits?petId={petId}", joinIds(petIds))
                .retrieve()
                .bodyToMono(Visits.class);
    }

    public Flux<VisitDetails> getVisitsForPet(final int petId){
        return webClientBuilder.build()
                .get()
                .uri(hostname + "/visits/pets/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Flux<VisitDetails> getVisitForPractitioner(final int practitionerId){
        return webClientBuilder.build()
                .get()
                .uri(hostname + "/visits/practitioner/visits/{practitionerId}", practitionerId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Flux<VisitDetails> getVisitsByPractitionerIdAndMonth(final int practitionerId, final int month) {
        return webClientBuilder.build()
                .get()
                .uri(hostname + "/visits/practitioner/{practitionerId}/{month}", practitionerId, month)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }

    public Mono<VisitDetails> updateVisitForPet(String visitId, Mono<VisitDetails> visitDetailsMono) {

        return webClientBuilder.build()
                .put()
                .uri(hostname + "/visits/visits/{visitId}", visitId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(visitDetailsMono, VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<VisitDetails> createVisitForPet(VisitDetails visit) {
        String url = hostname + "/visits";
        return webClientBuilder.build()
                .post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<Void> deleteVisitByVisitId(String visitId){
        return webClientBuilder.build()
                .delete()
                .uri(hostname + "/visits/{visitId}", visitId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private String joinIds(List<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public Mono<VisitDetails> getVisitByVisitId(String visitId) {
        return webClientBuilder
                .build()
                .get()
                .uri(hostname + "/visits/{visitId}", visitId)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }
}

