package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.VetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Christine Gerard
 */
//Old Version  - new version vet
@Component
@Slf4j
public class VetsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String vetsServiceUrl;

    public VetsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.vet-service.host}") String vetsServiceHost,
            @Value("${app.vet-service.port}") String vetsServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        vetsServiceUrl = "http://" + vetsServiceHost + ":" + vetsServicePort + "/vets";
    }

    public Flux<VetDTO> getVets() {
        Flux<VetDTO> vetDTOFlux =

               webClientBuilder
                 .build()
                 .get()
                 .uri(vetsServiceUrl)
                 .retrieve()
                 .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }

    public Mono<VetDTO> getVetByVetId(String vetId) {
        Mono<VetDTO> vetDTOMono =
                webClientBuilder
                  .build()
                  .get()
                  .uri(vetsServiceUrl + "/{vetId}", vetId)
                  .retrieve()
                  .bodyToMono(VetDTO.class);


        return vetDTOMono;
    }

    public Flux<VetDTO> getInactiveVets() {
        Flux<VetDTO> vetDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/inactive")
                        .retrieve()
                        .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }

    public Flux<VetDTO> getActiveVets() {
        Flux<VetDTO> vetDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/active")
                        .retrieve()
                        .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }


    public Mono<VetDTO> createVet(Mono<VetDTO> model) {
        Mono<VetDTO> vetDTO =
                webClientBuilder
                        .build()
                        .post()
                .uri(vetsServiceUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetDTO.class)
                .retrieve()
                .bodyToMono(VetDTO.class);

        return vetDTO;
    }

    public Mono<Void> deleteVet(String vetId) {
        Mono<Void> response = webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve()
                .bodyToMono(Void.class);

        return response;
    }

    public Mono<VetDTO> updateVet(String vetId,Mono<VetDTO> model) {
        Mono<VetDTO> vetDTOMono = webClientBuilder
                .build()
                .put()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetDTO.class)
                .retrieve()
                .bodyToMono(VetDTO.class);

        return vetDTOMono;
    }

}

