package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.BillDetails;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Mono<BillDetails> getBilling(final int billId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(BillDetails.class);
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

    public Mono<Void> deleteBill(final int billId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}


