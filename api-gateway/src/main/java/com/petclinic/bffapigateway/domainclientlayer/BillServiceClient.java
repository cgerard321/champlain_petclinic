package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.BillDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class BillServiceClient {

    private final WebClient.Builder webClientBuilder;
    private String billServiceUrl;


    public BillServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.billing-service.host}") String billingServiceHost,
            @Value("${app.billing-service.port}") String billingServicePort
    ) {
        this.webClientBuilder = webClientBuilder;

        billServiceUrl = "http://" + billingServiceHost + ":" + billingServicePort + "/bills";

    }

    public Mono<BillDetails> getBilling(final String billId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(BillDetails.class);
    }
    public Flux<BillDetails> getBillsByOwnerId(final String ownerId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/owner/{ownerId}", ownerId)
                .retrieve()
                .bodyToFlux(BillDetails.class);
    }
    public Flux<BillDetails> getBillsByVetId(final String vetId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/vet/{vetId}", vetId)
                .retrieve()
                .bodyToFlux(BillDetails.class);
    }
    public Flux<BillDetails> getAllBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl)
                .retrieve()
                .bodyToFlux(BillDetails.class);
    }

    public Mono<BillDetails> createBill(final BillDetails model){
        return webClientBuilder.build().post()
                .uri(billServiceUrl)
                .body(Mono.just(model),BillDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BillDetails.class);
    }

    public Mono<Void> deleteBill(final String billId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<Void> deleteBillsByVetId(final String vetId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/vet/{vetId}", vetId)
                .retrieve()
                .bodyToFlux(Void.class);
    }

    public Flux<Void> deleteBillsByOwnerId(final String ownerId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/owner/{ownerId}", ownerId)
                .retrieve()
                .bodyToFlux(Void.class);
    }
}


