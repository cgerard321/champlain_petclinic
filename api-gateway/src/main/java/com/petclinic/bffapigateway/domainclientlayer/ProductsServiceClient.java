package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@Slf4j
public class ProductsServiceClient {

    private final WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final String productsServiceUrl;

    public ProductsServiceClient(WebClient.Builder webClientBuilder,
                                 @Value("${app.products-service.host}") String productsServiceHost,
                                 @Value("${app.products-service.port}") String productsServicePort) {
        this.webClientBuilder = webClientBuilder;
        productsServiceUrl = "http://" + productsServiceHost + ":" + productsServicePort + "/products";
        this.webClient = webClientBuilder
                .baseUrl(productsServiceUrl)
                .build();

    }

    public Flux<ProductResponseDTO> getAllProducts(Double minPrice, Double maxPrice,Double minRating, Double maxRating, String sort,String deliveryType, String productType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    if (minPrice != null) {
                        uriBuilder.queryParam("minPrice", minPrice);
                    }
                    if (maxPrice != null) {
                        uriBuilder.queryParam("maxPrice", maxPrice);
                    }
                    if (minRating != null) {
                        uriBuilder.queryParam("minRating", minRating);
                    }
                    if (maxRating != null) {
                        uriBuilder.queryParam("maxRating", maxRating);
                    }
                    if (sort != null) {
                        uriBuilder.queryParam("sort", sort);
                    }
                    if (deliveryType != null) {
                        uriBuilder.queryParam("deliveryType", deliveryType);
                    }
                    if (productType != null) {
                        uriBuilder.queryParam("productType", productType);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToFlux(ProductResponseDTO.class)
                .filter(product -> {
                    boolean ratingFilter = (minRating == null || product.getAverageRating() >= minRating)
                            && (maxRating == null || product.getAverageRating() <= maxRating);
                    return ratingFilter;
                });
    }

    public Mono<ProductResponseDTO> getProductByProductId(final String productId) {
        return webClientBuilder.build()
                .get()
                .uri(productsServiceUrl + "/" + productId)
                .retrieve()
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<ProductResponseDTO> createProduct(final ProductRequestDTO productRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(productsServiceUrl)
                .body(Mono.just(productRequestDTO), ProductRequestDTO.class)
                .retrieve()
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<ProductResponseDTO> updateProduct(final String productId, ProductRequestDTO productRequestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(productsServiceUrl + "/"  + productId)
                .body(Mono.just(productRequestDTO), ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<ProductResponseDTO> patchListingStatus(final String productId, ProductRequestDTO productRequestDTO) {
        return webClientBuilder.build()
                .patch()
                .uri(productsServiceUrl + "/" + productId + "/status")
                .body(Mono.just(productRequestDTO), ProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("Product not found for ProductId: " + productId));

                    else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY))
                        return Mono.error(new InvalidInputException("Invalid input for ProductId: " + productId));

                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(ProductResponseDTO.class);

    }

    public Mono<ProductResponseDTO> deleteProduct(final String productId, boolean cascadeBundles) {
        return webClientBuilder.build()
                .delete()
                .uri(uri -> uri.path("/"  + productId)
                        .queryParam("cascadeBundles", cascadeBundles)
                        .build())
                .retrieve()
                .bodyToMono(ProductResponseDTO.class);
    }

    public Mono<Void> requestCount(final String productId) {
        return webClientBuilder.build()
                .patch()
                .uri(productsServiceUrl + "/" + productId)
                .retrieve()
                .bodyToMono(Void.class);

    }
    public Flux<ProductResponseDTO> getProductsByType(final String type){
        return webClientBuilder.build()
                .get()
                .uri(productsServiceUrl + "/filter/"  + type)
                .retrieve()
                .bodyToFlux(ProductResponseDTO.class);
    }
    public Mono<Void> decreaseProductQuantity(final String productId) {
        return webClientBuilder.build()
                .patch()
                .uri(productsServiceUrl + "/" + productId)
                .retrieve()
                .bodyToMono(Void.class);

    }
    public Mono<Void> changeProductQuantity(final String productId, Integer productQuantity) {
        return webClientBuilder.build()
                .patch()
                .uri(productsServiceUrl + "/" + productId + "/quantity")
                .bodyValue(new ProductQuantityRequest(productQuantity))
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Methods for product bundles
    public Flux<ProductBundleResponseDTO> getAllProductBundles() {
        return webClient.get()
                .uri("/bundles")
                .retrieve()
                .bodyToFlux(ProductBundleResponseDTO.class);
    }
    public Mono<ProductBundleResponseDTO> getProductBundleById(String bundleId) {
        return webClient.get()
                .uri("/bundles/{bundleId}", bundleId)
                .retrieve()
                .bodyToMono(ProductBundleResponseDTO.class);
    }
    public Mono<ProductBundleResponseDTO> createProductBundle(ProductBundleRequestDTO requestDTO) {
        return webClient.post()
                .uri("/bundles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ProductBundleResponseDTO.class);
    }
    public Mono<ProductBundleResponseDTO> updateProductBundle(String bundleId, ProductBundleRequestDTO requestDTO) {
        return webClient.put()
                .uri("/bundles/{bundleId}", bundleId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ProductBundleResponseDTO.class);
    }
    public Mono<Void> deleteProductBundle(String bundleId) {
        return webClient.delete()
                .uri("/bundles/{bundleId}", bundleId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<ProductEnumsResponseDTO> getProductEnumsValues(){
        return webClient.get()
                .uri("/enums")
                .retrieve()
                .bodyToMono(ProductEnumsResponseDTO.class);
    }








}
