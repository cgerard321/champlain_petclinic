package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.exceptions.BadRequestException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.exceptions.ProductImageDependencyException;
import com.petclinic.bffapigateway.exceptions.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
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

    public Flux<ProductResponseDTO> getAllProducts(
            Double minPrice, Double maxPrice, Double minRating, Double maxRating,
            String sort, String deliveryType, String productType) {
        return getAllProducts(
                minPrice, maxPrice, minRating, maxRating, sort, deliveryType,
                productType, null, false);
    }

    public Flux<ProductResponseDTO> getAllProducts(
            Double minPrice, Double maxPrice, Double minRating, Double maxRating,
            String sort, String deliveryType, String productType,
            String productName) {
        return getAllProducts(
                minPrice, maxPrice, minRating, maxRating, sort, deliveryType,
                productType, productName, false);
    }

    public Flux<ProductResponseDTO> getAllProducts(
            Double minPrice, Double maxPrice, Double minRating, Double maxRating,
            String sort, String deliveryType, String productType,
            boolean includeImage) {
        return getAllProducts(
                minPrice, maxPrice, minRating, maxRating, sort, deliveryType,
                productType, null, includeImage);
    }

    public Flux<ProductResponseDTO> getAllProducts(
            Double minPrice, Double maxPrice, Double minRating, Double maxRating,
            String sort, String deliveryType, String productType,
            String productName, boolean includeImage) {
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
                    if (productName != null && !productName.isBlank()) {
                        uriBuilder.queryParam("productName", productName.trim());
                    }
                    uriBuilder.queryParam("includeImage", includeImage);
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(
                        status -> status.value() == 424,
                        response -> productError(
                                response, "Unable to load product images"))
                .bodyToFlux(ProductResponseDTO.class)
                .filter(product -> {
                    boolean ratingFilter = (minRating == null || product.getAverageRating() >= minRating)
                            && (maxRating == null || product.getAverageRating() <= maxRating);
                    return ratingFilter;
                });
    }

    public Mono<ProductResponseDTO> getProductByProductId(String productId) {
        return getProductByProductId(productId, false);
    }

    public Mono<ProductResponseDTO> getProductByProductId(
            String productId, boolean includeImage) {

        return webClientBuilder.build()
                .get()
                .uri(
                        productsServiceUrl
                                + "/{productId}?includeImage={includeImage}",
                        productId,
                        includeImage)
                .retrieve()
                .onStatus(
                        status -> status.value() == 400
                                || status.value() == 404
                                || status.value() == 424,
                        response -> productError(
                                response, "Unable to load product"))
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

    public Mono<ProductResponseDTO> deleteProductImage(String productId) {
        return webClientBuilder.build()
                .delete()
                .uri(productsServiceUrl + "/{productId}/image", productId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 400
                                || status.value() == 404
                                || status.value() == 422
                                || status.value() == 424,
                        response -> productError(
                                response, "Unable to delete product image"))
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

    public Mono<ProductResponseDTO> updateProductImage(
            String productId, FileDetails image) {

        return webClientBuilder.build()
                .patch()
                .uri(productsServiceUrl + "/{productId}/image", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(image)
                .retrieve()
                // Preserve expected product-service errors with safe messages.
                .onStatus(
                        status -> status.value() == 400
                                || status.value() == 404
                                || status.value() == 422
                                || status.value() == 424,
                        response -> productError(
                                response, "Unable to update product image"))
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
    public Flux<ProductResponseDTO> getProductsByType(final String type) {
        return getProductsByType(type, false);
    }

    public Flux<ProductResponseDTO> getProductsByType(
            final String type, boolean includeImage) {
        return webClientBuilder.build()
                .get()
                .uri(productsServiceUrl
                                + "/filter/{type}?includeImage={includeImage}",
                        type, includeImage)
                .retrieve()
                .onStatus(
                        status -> status.value() == 424,
                        response -> productError(
                                response, "Unable to load product images"))
                .bodyToFlux(ProductResponseDTO.class);
    }

    private Mono<? extends Throwable> productError(
            ClientResponse response, String message) {
        return switch (response.statusCode().value()) {
            case 400 -> Mono.just(new BadRequestException(message));
            case 404 -> Mono.just(new ProductNotFoundException(message));
            case 422 -> Mono.just(new InvalidInputException(message));
            case 424 -> Mono.just(new ProductImageDependencyException(message));
            default -> Mono.just(new IllegalStateException(message));
        };
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

    public Flux<ProductTypeResponseDTO> getAllProductTypes() {
        return webClientBuilder.build().get()
                .uri(productsServiceUrl + "/types")
                .retrieve()
                .bodyToFlux(ProductTypeResponseDTO.class);
    }

    public Mono<ProductTypeResponseDTO> getProductTypeByProductTypeId(final String productTypeId) {
        return webClientBuilder.build()
                .get()
                .uri(productsServiceUrl + "/types/" + productTypeId)
                .retrieve()
                .bodyToMono(ProductTypeResponseDTO.class);
    }

    public Mono<ProductTypeResponseDTO> createProductType(final ProductTypeRequestDTO productTypeRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(productsServiceUrl + "/types")
                .body(Mono.just(productTypeRequestDTO), ProductTypeRequestDTO.class)
                .retrieve()
                .bodyToMono(ProductTypeResponseDTO.class);
    }

    public Mono<ProductTypeResponseDTO> updateProductType(final String productTypeId, ProductTypeRequestDTO productTypeRequestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(productsServiceUrl + "/types/"  + productTypeId)
                .body(Mono.just(productTypeRequestDTO), ProductTypeRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductTypeResponseDTO.class);
    }

    public Mono<ProductTypeResponseDTO> deleteProductType(final String productTypeId) {
        return webClientBuilder.build()
                .delete()
                .uri(productsServiceUrl + "/types/"  + productTypeId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 409,
                        response -> Mono.error(new GenericHttpException(
                                "Cannot delete product type that is still used by products",
                                HttpStatus.CONFLICT)))
                .bodyToMono(ProductTypeResponseDTO.class);
    }




}
