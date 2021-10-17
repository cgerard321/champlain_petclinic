package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.UserDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Christine Gerard
 * */

@Component
@Slf4j
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

    public Mono<VetDetails> getVet(final long vetId) {
        return webClientBuilder.build().get()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve()
                .bodyToMono(VetDetails.class);
    }

    public Mono<VetDetails> createVet (final VetDetails model) {
        return webClientBuilder.build().post()
                .uri(vetsServiceUrl + model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(VetDetails.class);
    }

    public Mono<VetDetails> deleteVet(final long vetId) {
        return webClientBuilder.build().delete()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve().bodyToMono(VetDetails.class);
    }

    public Mono<VetDetails> updateVet(final int vetId, final VetDetails model){
        return webClientBuilder.build().put()
                .uri(vetsServiceUrl + vetId + model)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(VetDetails.class);
    }

}