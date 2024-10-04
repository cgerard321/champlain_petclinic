package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class CartServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String CartServiceUrl;

    public CartServiceClient(WebClient.Builder webClientBuilder,
                                 @Value("${app.cart-service.host}") String CartServiceHost,
                                 @Value("${app.cart-service.port}") String CartServicePort) {
        this.webClientBuilder = webClientBuilder;
        CartServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/carts";
    }

    public Flux<CartResponseDTO> getAllCarts() {
        return webClientBuilder.build()
                .get()
                .uri(CartServiceUrl)
                .retrieve()
                .bodyToFlux(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> getCartByCartId(final String CartId) {
        return webClientBuilder.build()
                .get()
                .uri(CartServiceUrl + "/" + CartId)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> updateCartByCartId(Mono<CartRequestDTO> cartRequestDTOMono, String CartId) {
        return webClientBuilder.build()
                .put()
                .uri(CartServiceUrl + "/" + CartId)
                .body(Mono.just(cartRequestDTOMono), CartRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> deleteCartByCartId(String CardId) {
        return webClientBuilder.build()
                .delete()
                .uri(CartServiceUrl + "/" + CardId)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public CartResponseDTO createCart(CartRequestDTO cartRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(CartServiceUrl)
                .bodyValue(cartRequestDTO)
                .retrieve()
                .bodyToMono(CartResponseDTO.class)
                .block();
    }

    public Mono<Void> clearCart(String cartId) {
        return webClientBuilder.build()
                .delete()
                .uri(CartServiceUrl + "/" + cartId + "/clear")
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<CartResponseDTO> checkoutCart(final String cartId) {
        return webClientBuilder.build()
                .post()
                .uri(CartServiceUrl + "/" + cartId + "/checkout")
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }
}


