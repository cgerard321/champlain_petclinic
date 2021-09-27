package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
        hostname = "http://" + visitsServiceHost + ":" + visitsServicePort + "/";
    }

    public Mono<Visits> getVisitsForPets(final List<Integer> petIds) {
        return webClientBuilder.build()
                .get()
                .uri(hostname + "pets/visits?petId={petId}", joinIds(petIds))
                .retrieve()
                .bodyToMono(Visits.class);
    }

/*
    public Mono<Visits> createVisitForPets(final VisitDetails visitDetails){
        return webClientBuilder.build()
                .post()
                .uri(hostname + "pets/visits")
                .body(Mono.just(visitDetails), VisitDetails.class)
                .retrieve()
                .bodyToMono(Visits.class);

    }
*/
    //Testing purpose

    public Mono<Visits> getAllVisits(){
        return webClientBuilder.build()
                .get()
                .uri(hostname + "pets/visits/All")
                .retrieve()
                .bodyToMono(Visits.class);
    }

    private String joinIds(List<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }
}

