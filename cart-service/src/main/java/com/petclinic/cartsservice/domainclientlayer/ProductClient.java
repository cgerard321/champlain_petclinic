package com.petclinic.cartsservice.domainclientlayer;

import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductClient {

    private final WebClient webClient;

    private final String productsBaseURL;

    public ProductClient(@Value("products-service") String productsServiceHost,
                         @Value("8080") String productsServicePort) {
        this.productsBaseURL = "http://" + productsServiceHost + ":" + productsServicePort + "/products";

        this.webClient = WebClient.builder()
                .baseUrl(productsBaseURL)
                .build();
    }

    public Mono<ProductResponseModel> getProductByProductId(String productId){
        return webClient.get()
                .uri("/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        error -> switch (error.statusCode().value()) {
                            case 404 -> Mono.error(new NotFoundException("ProductId not found: " + productId));
                            case 422 -> Mono.error(new InvalidInputException("ProductId invalid: " + productId));
                            default -> Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                .bodyToMono(ProductResponseModel.class)
                .map(this::normalizeInventoryFields);
    }

    public Flux<ProductResponseModel> getProductsByType(String productType) {
        return webClient.get()
                .uri("/filter/{productType}", productType)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(
                                new IllegalArgumentException(
                                        "Unable to retrieve products for type: " + productType
                                )
                        )
                )
                .bodyToFlux(ProductResponseModel.class);
    }

    private ProductResponseModel normalizeInventoryFields(ProductResponseModel product) {
        if (product == null) {
            return null;
        }

        if (product.getProductQuantity() == null) {
            if (product.getProductStock() != null) {
                product.setProductQuantity(product.getProductStock());
            } else if (product.getQuantity() != null) {
                product.setProductQuantity(product.getQuantity());
            }
        }

        return product;
    }
}