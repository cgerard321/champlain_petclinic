package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Service
public class BillServiceClient {
    private final WebClient webClient;
    public BillServiceClient(
            @Value("${app.billing-service.host}") String billingServiceHost,
            @Value("${app.billing-service.port}") String billingServicePort
    ) {
//        this.webClient = WebClient.builder().baseUrl("http://" + billingServiceHost + ":" + billingServicePort).build();
        this.webClient = WebClient.builder().baseUrl("http://" + "localhost" + ":" + "7004").build();
    }
    public Flux<BillResponseDTO> getAllBilling() {
        return webClient.get()
                .uri("/bills")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
}