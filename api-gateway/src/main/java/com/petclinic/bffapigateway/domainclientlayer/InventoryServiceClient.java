
package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.BundleDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class InventoryServiceClient {
    private final WebClient.Builder webClientBuilder;
    private String inventoryServiceUrl;


    public InventoryServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.inventory-service.host}") String inventoryServiceHost,
            @Value("${app.inventory-service.port}") String inventoryServicePort
    ) {
        this.webClientBuilder = webClientBuilder;

        inventoryServiceUrl = "http://" + inventoryServiceHost + ":" + inventoryServicePort + "/bundles";

    }

    public Mono<BundleDetails> getBundle(final String bundleUUID) {
        return webClientBuilder.build().get()
                .uri(inventoryServiceUrl + "/{bundleUUID}", bundleUUID)
                .retrieve()
                .bodyToMono(BundleDetails.class);
    }
    public Flux<BundleDetails> getBundlesByItem(final String item) {
        return webClientBuilder.build().get()
                .uri(inventoryServiceUrl +"/item"+ "/{item}", item)
                .retrieve()
                .bodyToFlux(BundleDetails.class);
    }
    public Flux<BundleDetails> getAllBundles() {
        return webClientBuilder.build().get()
                .uri(inventoryServiceUrl)
                .retrieve()
                .bodyToFlux(BundleDetails.class);
    }

    public Mono<BundleDetails> createBundle(final BundleDetails model){
        return webClientBuilder.build().post()
                .uri(inventoryServiceUrl)
                .body(Mono.just(model),BundleDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BundleDetails.class);
    }

    public Mono<Void> deleteBundle(final String bundleUUID) {
        return webClientBuilder.build()
                .delete()
                .uri(inventoryServiceUrl + "/{bundleUUID}", bundleUUID)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
