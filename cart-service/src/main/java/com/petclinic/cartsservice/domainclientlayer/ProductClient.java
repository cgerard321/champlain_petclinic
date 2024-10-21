package com.petclinic.cartsservice.domainclientlayer;

import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(@Value("${products-service.base-url}") String productsBaseURL) {
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
                .map(product -> {
                    // Mock the stock quantity until the real data is available
                    product.setProductQuantity(10); // Arbitrary stock level for testing
                    product.setImageId(product.getImageId());
                    return product;
                });
    }
}
