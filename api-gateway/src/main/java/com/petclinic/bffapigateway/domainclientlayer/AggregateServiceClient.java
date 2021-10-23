package com.petclinic.bffapigateway.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AggregateServiceClient {
    private final WebClient.Builder webClientBuilder;

    private final String customersServiceUrl;
    private final String vetsServiceUrl;
    private final String billServiceUrl;

    public AggregateServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort,

            @Value("${app.vets-service.host}") String vetsServiceHost,
            @Value("${app.vets-service.port}") String vetsServicePort,

            @Value("${app.visits-service.host}") String visitsServiceHost,
            @Value("${app.visits-service.port}") String visitsServicePort,

            @Value("${app.billing-service.host}") String billingServiceHost,
            @Value("${app.billing-service.port}") String billingServicePort
    ){
        this.webClientBuilder = webClientBuilder;
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/owners";
        vetsServiceUrl = "http://" + vetsServiceHost + ":" + vetsServicePort + "/vets";
        billServiceUrl = "http://" + billingServiceHost + ":" + billingServicePort + "/bills";

    }

   
}
