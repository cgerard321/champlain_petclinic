package com.petclinic.products.domainclientlayer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CartClient {

    private final WebClient webClient;
    private final String cartsBaseURL;

    public CartClient(
            @Value("${app.cart-service.host:cart-service}") String cartServiceHost,
            @Value("${app.cart-service.port:8080}") String cartServicePort
    ) {
        this.cartsBaseURL = "http://" + cartServiceHost + ":" + cartServicePort + "/api/v1/carts";
        this.webClient = WebClient.builder()
                .baseUrl(this.cartsBaseURL)
                .build();
    }


    public Mono<Void> purgeProductFromAllCarts(String productId) {
        log.info("[CartClient] Sending purge request for product {}", productId);
        return webClient.delete()
                .uri("/internal/products/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.warn("[CartClient] Error response while purging product {} -> {}", productId, response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "CartService purge failed (" + response.statusCode() + "): " + body
                            )));
                })
                .toBodilessEntity()
                .doOnSuccess(__ -> log.info("[CartClient] Successfully purged product {} from all carts", productId))
                .doOnError(e -> log.warn("[CartClient] Failed to purge product {}: {}", productId, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
