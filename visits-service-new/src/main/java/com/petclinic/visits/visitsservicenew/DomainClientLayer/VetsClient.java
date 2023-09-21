package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
}
