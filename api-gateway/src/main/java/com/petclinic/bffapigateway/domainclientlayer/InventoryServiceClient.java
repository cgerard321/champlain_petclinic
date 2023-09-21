
package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Component
public class InventoryServiceClient {
    private final WebClient webClient;
    private String inventoryServiceUrl;


    public InventoryServiceClient(
            @Value("${app.inventory-service.host}") String inventoryServiceHost,
            @Value("${app.inventory-service.port}") String inventoryServicePort
    ) {

        inventoryServiceUrl = "http://" + inventoryServiceHost + ":" + inventoryServicePort + "/inventory";
        this.webClient = WebClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public Mono<ProductResponseDTO> addProductToInventory(final ProductRequestDTO model, final String inventoryId){
        return webClient.post()
                .uri(inventoryServiceUrl + "/{inventoryId}/products", inventoryId)
                .body(Mono.just(model),ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(ProductResponseDTO.class);
    }




    public Mono<InventoryResponseDTO> addInventory(final InventoryRequestDTO model){
        return webClient.post()
                .uri(inventoryServiceUrl)
                .body(Mono.just(model),InventoryRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(InventoryResponseDTO.class);
    }



    public Mono<InventoryResponseDTO> updateInventory(InventoryRequestDTO model, String inventoryId){
        return webClient.put()
                .uri(inventoryServiceUrl + "/{inventoryId}" , inventoryId)
                .body(Mono.just(model),InventoryRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(InventoryResponseDTO.class);
    }
    public Mono<Void> deleteProductInInventory(final String inventoryId, final String productId){
        return webClient.delete()
                .uri(inventoryServiceUrl + "/{inventoryId}/products/{productId}", inventoryId, productId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(final String inventoryId, final String inventoryName, final Double productPrice, final Integer productQuantity){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryType}/products")
                .queryParamIfPresent("productName", Optional.ofNullable(inventoryName))
                .queryParamIfPresent("productPrice", Optional.ofNullable(productPrice))
                .queryParamIfPresent("productQuantity", Optional.ofNullable(productQuantity));

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ProductResponseDTO.class);
    }

    public Flux<InventoryResponseDTO> getAllInventory(){
        return webClient.get()
                .uri(inventoryServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(InventoryResponseDTO.class);
    }
//    public Mono<BundleDetails> getBundle(final String bundleUUID) {
//        return webClientBuilder.build().get()
//                .uri(inventoryServiceUrl + "/{bundleUUID}", bundleUUID)
//                .retrieve()
//                .bodyToMono(BundleDetails.class);
//    }
//    public Flux<BundleDetails> getBundlesByItem(final String item) {
//        return webClientBuilder.build().get()
//                .uri(inventoryServiceUrl +"/item"+ "/{item}", item)
//                .retrieve()
//                .bodyToFlux(BundleDetails.class);
//    }
//    public Flux<BundleDetails> getAllBundles() {
//        return webClientBuilder.build().get()
//                .uri(inventoryServiceUrl)
//                .retrieve()
//                .bodyToFlux(BundleDetails.class);
//    }
//
//    public Mono<BundleDetails> createBundle(final BundleDetails model){
//        return webClientBuilder.build().post()
//                .uri(inventoryServiceUrl)
//                .body(Mono.just(model),BundleDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve().bodyToMono(BundleDetails.class);
//    }
//
//    public Mono<Void> deleteBundle(final String bundleUUID) {
//        return webClientBuilder.build()
//                .delete()
//                .uri(inventoryServiceUrl + "/{bundleUUID}", bundleUUID)
//                .retrieve()
//                .bodyToMono(Void.class);
//    }

}
