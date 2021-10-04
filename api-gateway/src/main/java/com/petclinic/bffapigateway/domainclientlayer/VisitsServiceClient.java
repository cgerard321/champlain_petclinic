package com.petclinic.bffapigateway.domainclientlayer;

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

import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Component
//@RequiredArgsConstructor
public class VisitsServiceClient {

//    private final WebClient.Builder webClientBuilder;
//
//    @Value("${app.visits-service.host}") String visitsServiceHost;
//    @Value("${app.visits-service.port}") String visitsServicePort;
//    private final String visitsServiceURL = "http://" + visitsServiceHost + ":" + visitsServicePort + "/";
//    private String hostname = "http://" + visitsServiceHost + "/";

    private final WebClient.Builder webClientBuilder;
    //private final String visitsServiceURL;
    private String hostname = "http://visits-service";

    @Autowired
    public VisitsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.visits-service.host}") String visitsServiceHost,
            @Value("${app.visits-service.port}") String visitsServicePort
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
                .uri(hostname + "/visits/{petId}", petId)
                .retrieve()
                .bodyToFlux(VisitDetails.class);
    }
/*
    public Mono<Visits> createVisitForPets(final VisitDetails visitDetails){
        return webClientBuilder.build()
                .post()
                .uri(hostname + "/pets/visits")
                .body(Mono.just(visitDetails), VisitDetails.class)
                .retrieve()
                .bodyToMono(Visits.class);

    }

*/

    public Mono<VisitDetails> updateVisitForPets(final int petId){
        return webClientBuilder.build()
                .put()
                .uri(hostname + "/pets/visits/{petId}", petId)
                .body(Mono.just(petId), Visits.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<Void> deleteVisitForPets(final int petId){
        return webClientBuilder.build()
                .delete()
                .uri(hostname + "/pets/visits/{petId}", petId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    //Testing purpose

    public Mono<Visits> getAllVisits(){
        return webClientBuilder.build()
                .get()
                .uri(hostname + "/pets/visits/All")
                .retrieve()
                .bodyToMono(Visits.class);
    }
    public Mono<VisitDetails> updateVisitForPet(VisitDetails visit) {
        String url = hostname + "/owners/*/pets/" + visit.getPetId() + "/visits/" + visit.getId();
        return webClientBuilder.build()
                .put()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<VisitDetails> createVisitForPet(VisitDetails visit) {
        String url = hostname + "/visit/owners/*/pets/" + visit.getPetId() + "/visits";
        return webClientBuilder.build()
                .post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(visit), VisitDetails.class)
                .retrieve()
                .bodyToMono(VisitDetails.class);
    }

    public Mono<Void> deleteVisitsById(int visitId){
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
}

