package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PetsClient {

    private final WebClient webClient;
    private final String petClientServiceBaseURL;

    public PetsClient(@Value("${app.customers-service-reactive.host}") String petsServiceHost,
                      @Value("${app.customers-service-reactive.port}") String petsServicePort){

        petClientServiceBaseURL = "http://" + petsServiceHost + ":" + petsServicePort + "/vets";

        this.webClient = WebClient.builder()
                .baseUrl(petClientServiceBaseURL)
                .build();

    }

    
}
