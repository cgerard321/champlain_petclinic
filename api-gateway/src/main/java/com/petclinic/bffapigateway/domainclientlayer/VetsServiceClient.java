package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author Christine Gerard
 * */

@Component
public class VetsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String vetsServiceUrl;

    public VetsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.vets-service.host}") String vetsServiceHost,
            @Value("${app.vets-service.port}") String vetsServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        vetsServiceUrl = "http://" + vetsServiceHost + ":" + vetsServicePort + "/vets";
    }

    public Flux<VetDetails> getVets() {
        return webClientBuilder.build().get()
                .uri(vetsServiceUrl)
                .retrieve()
                .bodyToFlux(VetDetails.class);
    }

    public Mono<VetDetails> getVet(final int vetId){
        return webClientBuilder.build().get().uri(vetsServiceUrl + "/{vetId}", vetId).retrieve().bodyToMono(VetDetails.class);
    }

    public Mono<VetDetails> disableVet(final int vetId){
        return webClientBuilder.build().get().uri(vetsServiceUrl + "/{vetId}/disableVet", vetId).retrieve().bodyToMono(VetDetails.class);
    }

}
