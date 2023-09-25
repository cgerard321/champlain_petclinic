package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Bills.BillDetails;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    public Mono<BillResponseDTO> getBilling(final String billId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(BillResponseDTO.class);
    }
    public Flux<BillResponseDTO> getBillsByOwnerId(final int customerId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
    public Flux<BillResponseDTO> getBillsByVetId(final String vetId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/vet/{vetId}", vetId)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
    public Flux<BillResponseDTO> getAllBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Mono<BillResponseDTO> createBill(final BillRequestDTO model){
        return webClientBuilder.build().post()
                .uri(billServiceUrl)
                .body(Mono.just(model), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BillResponseDTO.class);
    }

    public Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO){
        return webClientBuilder
                .build()
                .put()
                .uri(billServiceUrl + "/{billId}", billId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(billRequestDTO, BillResponseDTO.class)
                .retrieve()
                .bodyToMono(BillResponseDTO.class);
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

    public Flux<Void> deleteBillsByCustomerId(final int customerId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(Void.class);
    }
}


