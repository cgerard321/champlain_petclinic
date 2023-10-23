package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Component
public class BillServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String billServiceUrl;


    public BillServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.billing-service.host}") String billingServiceHost,
            @Value("${app.billing-service.port}") String billingServicePort
    ) {
        this.webClientBuilder = webClientBuilder;

        billServiceUrl = "http://" + billingServiceHost + ":" + billingServicePort + "/bills";

    }
    public Flux<BillResponseDTO> getAllBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

}


