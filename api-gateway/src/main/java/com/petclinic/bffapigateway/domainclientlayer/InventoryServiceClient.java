
package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputsInventoryException;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.exceptions.ProductListNotFoundException;
import com.petclinic.bffapigateway.utils.Rethrower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;


@Component
public class InventoryServiceClient {
    private final WebClient webClient;
    private String inventoryServiceUrl;
    @Autowired
    private Rethrower rethrower;

    public InventoryServiceClient(
            @Value("${app.inventory-service.host}") String inventoryServiceHost,
            @Value("${app.inventory-service.port}") String inventoryServicePort
    ) {

        inventoryServiceUrl = "http://" + inventoryServiceHost + ":" + inventoryServicePort + "/inventory";
        this.webClient = WebClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }


    public Mono<InventoryResponseDTO> getInventoryById(final String inventoryId) {
        return webClient.get()
                .uri(inventoryServiceUrl + "/{inventoryId}", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InventoryNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToMono(InventoryResponseDTO.class);
    }

    public Mono<ProductResponseDTO> addProductToInventory(final ProductRequestDTO model, final String inventoryId){
        return webClient.post()
                .uri(inventoryServiceUrl + "/{inventoryId}/products", inventoryId)
                .body(Mono.just(model),ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(ProductResponseDTO.class);
    }




    public Mono<InventoryResponseDTO> addInventory(final InventoryRequestDTO model){
        return webClient.post()
                .uri(inventoryServiceUrl)
                .body(Mono.just(model),InventoryRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(InventoryResponseDTO.class);
    }



    public Mono<InventoryResponseDTO> updateInventory(InventoryRequestDTO model, String inventoryId){
        return webClient.put()
                .uri(inventoryServiceUrl + "/{inventoryId}" , inventoryId)
                .body(Mono.just(model),InventoryRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(InventoryResponseDTO.class);
    }





    public Mono<ProductResponseDTO> updateProductInInventory(final ProductRequestDTO model, final String inventoryId, final String productId){
        return webClient
                .put()
                .uri(inventoryServiceUrl + "/{inventoryId}/products/{productId}", inventoryId, productId)
                .body(Mono.just(model),ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<Void> deleteProductInInventory(final String inventoryId, final String productId){
        return webClient.delete()
                .uri(inventoryServiceUrl + "/{inventoryId}/products/{productId}", inventoryId, productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToMono(Void.class);
    }

    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(final String inventoryId, final String productName, final Double productPrice, final Integer productQuantity){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryType}/products")
                .queryParamIfPresent("productName", Optional.ofNullable(productName))
                .queryParamIfPresent("productPrice", Optional.ofNullable(productPrice))
                .queryParamIfPresent("productQuantity", Optional.ofNullable(productQuantity));

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToFlux(ProductResponseDTO.class);
    }

    public Flux<InventoryResponseDTO> getAllInventory(){
        return webClient.get()
                .uri(inventoryServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(InventoryResponseDTO.class);
    }
    //delete all

    public Mono<Void> deleteAllProductForInventory(final String inventoryId) {
        return webClient.delete()
                .uri(inventoryServiceUrl + "/{inventoryId}/products", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToMono(Void.class);
    }
    public Mono<Void> deleteAllInventories() {
        return webClient.delete()
                .uri(inventoryServiceUrl)  // Notice that we don't append any specific path
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class);
    }




}
