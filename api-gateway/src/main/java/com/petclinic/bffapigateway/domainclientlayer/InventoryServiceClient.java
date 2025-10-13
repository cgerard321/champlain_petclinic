
package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputsInventoryException;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.exceptions.ProductListNotFoundException;
import com.petclinic.bffapigateway.utils.Rethrower;
import io.netty.handler.codec.http.HttpStatusClass;
import org.springframework.beans.factory.annotation.Autowired;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.nio.channels.FileChannel;
import java.util.Map;
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
                .onStatus(s -> s.value() == 404,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InventoryNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(InventoryResponseDTO.class);
    }


    public Mono<ProductResponseDTO> getProductByProductIdInInventory(final String inventoryId, final String productId) {
        return webClient.get()
                .uri(inventoryServiceUrl + "/{inventoryId}/products/{productId}", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(s -> s.value() == 404,
                        resp -> rethrower.rethrow(resp, ex ->
                                new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex ->
                                new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(ProductResponseDTO.class);
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
                .onStatus(status -> status.value() == 422,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), UNPROCESSABLE_ENTITY)))


                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), BAD_REQUEST)))

                .bodyToMono(InventoryResponseDTO.class);
    }



    public Mono<InventoryResponseDTO> updateInventory(InventoryRequestDTO model, String inventoryId){
        return webClient.put()
                .uri(inventoryServiceUrl + "/{inventoryId}" , inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(model),InventoryRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(s -> s.value() == 422,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(ex.get("message").toString(), UNPROCESSABLE_ENTITY)))

                .onStatus(s -> s.value() == 404,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InventoryNotFoundException(ex.get("message").toString(), NOT_FOUND)))

                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(InventoryResponseDTO.class);
    }


    public Mono<ProductResponseDTO> updateProductInInventory(ProductRequestDTO model, String inventoryId, String productId){


        return webClient
                .put()
                .uri(inventoryServiceUrl + "/{inventoryId}/products/{productId}", inventoryId, productId)
                .body(Mono.just(model),ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(s -> s.value() == 422, resp ->
                        rethrower.rethrow(resp, ex ->
                                new InvalidInputsInventoryException(ex.get("message").toString(), UNPROCESSABLE_ENTITY)))
                .onStatus(s -> s.value() == 404, resp ->
                        rethrower.rethrow(resp, ex ->
                                new InvalidInputsInventoryException(ex.get("message").toString(), NOT_FOUND)))
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        rethrower.rethrow(resp, ex ->
                                new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
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


    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(final String inventoryId, final String productName, final Double productPrice, final Integer productQuantity, final Double productSalePrice){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryType}/products")
                .queryParamIfPresent("productName", Optional.ofNullable(productName))
                .queryParamIfPresent("productPrice", Optional.ofNullable(productPrice))
                .queryParamIfPresent("productQuantity", Optional.ofNullable(productQuantity))
                .queryParamIfPresent("productSalePrice", Optional.ofNullable(productSalePrice));

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToFlux(ProductResponseDTO.class);
    }

    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductFieldPagination(final String inventoryId,
                                                                                                 final String productName,
                                                                                                 final Double productPrice,
                                                                                                 final Integer productQuantity,
                                                                                                 final Optional<Integer> page,
                                                                                                 final Optional<Integer> size){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/" + inventoryId + "/products-pagination");
        if (page.isPresent() && size.isPresent()) {
            uriBuilder.queryParam("page", page.get());
            uriBuilder.queryParam("size", size.get());
        }
        if (productName != null) {
            uriBuilder.queryParam("productName", productName);
        }
        if (productPrice != null) {
            uriBuilder.queryParam("productPrice", productPrice);
        }
        if (productQuantity != null) {
            uriBuilder.queryParam("productQuantity", productQuantity);
        }

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToFlux(ProductResponseDTO.class);
    }

    public Mono<Long> getTotalNumberOfProductsWithRequestParams(final String inventoryId,
                                                                final String productName,
                                                                final Double productPrice,
                                                                final Integer productQuantity){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/" + inventoryId + "/products-count")
                .queryParamIfPresent("productName", Optional.ofNullable(productName))
                .queryParamIfPresent("productPrice", Optional.ofNullable(productPrice))
                .queryParamIfPresent("productQuantity", Optional.ofNullable(productQuantity));

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new ProductListNotFoundException(ex.get("message").toString(), NOT_FOUND)))
                .bodyToMono(Long.class);
    }

    public Mono<Void> updateImportantStatus(String inventoryId, Boolean important) {
        return webClient.patch()
                .uri("/{inventoryId}/important", inventoryId)
                .body(Mono.just(Map.of("important", important)), Map.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<InventoryResponseDTO> searchInventory(
            final Optional<Integer> page,
            final Optional<Integer> size,
            final String inventoryCode,
            final String inventoryName,
            final String inventoryType,
            final String inventoryDescription,
            final Boolean importantOnly
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl)
                .queryParamIfPresent("page", page)
                .queryParamIfPresent("size", size)
                .queryParamIfPresent("inventoryCode", Optional.ofNullable(inventoryCode))
                .queryParamIfPresent("inventoryName", Optional.ofNullable(inventoryName))
                .queryParamIfPresent("inventoryType", Optional.ofNullable(inventoryType))
                .queryParamIfPresent("inventoryDescription", Optional.ofNullable(inventoryDescription))
                .queryParamIfPresent("importantOnly", Optional.ofNullable(importantOnly));


        return webClient.get()
                .uri(uriBuilder.buildAndExpand().toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // Consider adding error-handling logic here if needed.
                .bodyToFlux(InventoryResponseDTO.class);
    }


    //delete all

    public Mono<Void> deleteAllProductsInInventory(final String inventoryId) {
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
                .uri(inventoryServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Mono<InventoryTypeResponseDTO> addInventoryType(InventoryTypeRequestDTO inventoryTypeRequestDTO){
        return webClient.post()
                .uri(inventoryServiceUrl + "/type")
                .body(Mono.just(inventoryTypeRequestDTO),InventoryTypeRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(s -> s.value() == 422,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), HttpStatus.UNPROCESSABLE_ENTITY)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), HttpStatus.BAD_REQUEST)))
                .bodyToMono(InventoryTypeResponseDTO.class);
    }

    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
        return webClient.get()
                .uri(inventoryServiceUrl + "/type")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> rethrower.rethrow(
                                resp,
                                ex -> new NotFoundException(String.valueOf(ex.get("message")))))
                .bodyToFlux(InventoryTypeResponseDTO.class);
    }


    public Mono<Void> deleteInventoryByInventoryId(String inventoryId){
        return webClient.delete()
                .uri(inventoryServiceUrl + "/{inventoryId}", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> rethrower.rethrow(resp,
                                ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(Void.class);
    }


    public Flux<ProductResponseDTO> getProductsByInventoryName(String inventoryName) {
        return webClient.get()
                .uri("/{inventoryName}/products/by-name", inventoryName)
                .retrieve()
                .bodyToFlux(ProductResponseDTO.class);
    }
  
    public Flux<ProductResponseDTO> getLowStockProducts(String inventoryId, int stockThreshold) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryId}/products/lowstock")
                .queryParam("threshold", stockThreshold);

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new NotFoundException("No products below threshold in inventory: " + inventoryId)))
                .bodyToFlux(ProductResponseDTO.class);
    }

    public Flux<ProductResponseDTO> searchProducts(
            final String inventoryId,
            final String productName,
            final String productDescription,
            final Status status
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryId}/products/search")
                .queryParamIfPresent("productName", Optional.ofNullable(productName))
                .queryParamIfPresent("productDescription", Optional.ofNullable(productDescription))
                .queryParamIfPresent("status", Optional.ofNullable(status));

        return webClient.get()
                .uri(uriBuilder.buildAndExpand(inventoryId).toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new InventoryNotFoundException("No products found in inventory: " + inventoryId + " that match the search criteria", HttpStatus.NOT_FOUND)))
                .bodyToFlux(ProductResponseDTO.class);
    }
    public Mono<ProductResponseDTO> addSupplyToInventory(final ProductRequestDTO model, final String inventoryId){

        return webClient.post()
                .uri(inventoryServiceUrl + "/{inventoryId}/products", inventoryId)
                .body(Mono.just(model),ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp, ex -> new InvalidInputsInventoryException(ex.get("message").toString(), BAD_REQUEST)))
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<ProductResponseDTO> consumeProduct(String inventoryId, String productId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/{inventoryId}/products/{productId}/consume");

        return webClient.patch()
                .uri(uriBuilder.buildAndExpand(inventoryId, productId).toUri())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new InventoryNotFoundException("Product not found in inventory: " + inventoryId, NOT_FOUND)))
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<Integer> getQuantityOfProductsInInventory(final String inventoryId) {
        return webClient.get()
                .uri(inventoryServiceUrl + "/{inventoryId}/productquantity", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new InventoryNotFoundException(errorMessage, HttpStatus.NOT_FOUND))))
                .bodyToMono(Integer.class);
    }

    public Mono<byte[]> createSupplyPdf(String inventoryId) {
        return webClient.get()
                .uri("/{inventoryId}/products/download", inventoryId)
                .accept(MediaType.APPLICATION_PDF) // Expect PDF response
                .retrieve()
                .onStatus(status -> status.value() == 422,
                        resp -> rethrower.rethrow(
                                resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(),
                                        HttpStatus.UNPROCESSABLE_ENTITY)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(
                                resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(),
                                        HttpStatus.BAD_REQUEST)))
                .bodyToMono(byte[].class); // Directly read the body as byte[]
    }

    public Mono<ProductResponseDTO> updateProductInventoryId(String currentInventoryId, String productId, String newInventoryId) {
        return webClient.put()
                .uri(inventoryServiceUrl + "/{currentInventoryId}/products/{productId}/updateInventoryId/{newInventoryId}", currentInventoryId, productId, newInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new NotFoundException("Product not found in inventory: " + currentInventoryId)))
                .bodyToMono(ProductResponseDTO.class);
    }

    public Flux<InventoryResponseDTO> getAllInventories() {
        return webClient.get()
                .uri(inventoryServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> rethrower.rethrow(resp,
                                ex -> new NotFoundException(ex.get("message").toString())))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> rethrower.rethrow(resp,
                                ex -> new InvalidInputsInventoryException(
                                        ex.get("message").toString(), BAD_REQUEST)))
                .bodyToFlux(InventoryResponseDTO.class);
    }

    public Mono<ProductResponseDTO> restockLowStockProduct(final String inventoryId, final String productId, final Integer productQuantity) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{inventoryId}/products/{productId}/restockProduct")
                        .queryParam("productQuantity", productQuantity)  // Add the productQuantity as a query param
                        .build(inventoryId, productId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new NotFoundException("Product: " + productId + " not found in inventory: " + inventoryId)))
                .bodyToMono(ProductResponseDTO.class);
    }

}

