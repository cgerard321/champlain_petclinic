package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Products.ProductQuantityRequest;
import com.petclinic.bffapigateway.dtos.Products.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
        productsServiceUrl = "http://" + productsServiceHost + ":" + productsServicePort + "/api/v1/products";
        this.webClient = webClientBuilder
                .baseUrl(productsServiceUrl)
                .build();

    }

//    public ProductsServiceClient(WebClient.Builder webClientBuilder,
//                                 @Value("${app.products-service.host}") String productsServiceHost,
//                                 @Value("${app.products-service.port}") String productsServicePort) {
//        this.webClientBuilder = webClientBuilder;
//        productsServiceUrl = "http://" + productsServiceHost + ":" + productsServicePort + "/api/v1/products";
//        this.webClient = webClientBuilder
//                .baseUrl(productsServiceUrl)
//                .build();
//
//    }

    public Flux<ProductResponseDTO> getAllProducts(Double minPrice, Double maxPrice) {
        return webClient.get()
                .uri(uriBuilder -> {
                    if (minPrice != null) {
                        uriBuilder.queryParam("minPrice", minPrice);
                    }
                    if (maxPrice != null) {
                        uriBuilder.queryParam("maxPrice", maxPrice);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToFlux(ProductResponseDTO.class);
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
                .body(Mono.just(productRequestDTO), ProductResponseDTO.class)
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

    public Mono<ProductResponseDTO> deleteProduct(final String productId) {
        return webClientBuilder.build()
                .delete()
                .uri(productsServiceUrl + "/"  + productId)
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




}
