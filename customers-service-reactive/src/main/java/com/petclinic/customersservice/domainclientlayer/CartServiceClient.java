package com.petclinic.customersservice.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CartServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String customerCartServiceUrl;

    public CartServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${app.cart-service.host}") String host,
                             @Value("${app.cart-service.port}") String port) {
        this.webClientBuilder = webClientBuilder;
        this.customerCartServiceUrl = "http://" + host + ":" + port + "/api/v1/customers";
    }

    public Mono<Void> deleteCartByCustomerId(String customerId) {
        return webClientBuilder.build()
                .delete()
                .uri(customerCartServiceUrl + "/{customerId}/cart", customerId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
