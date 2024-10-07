package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.*;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class CartServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String CartServiceUrl;
    private final String PromoCodeServiceUrl;

    public CartServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${app.cart-service.host}") String CartServiceHost,
                             @Value("${app.cart-service.port}") String CartServicePort) {
        this.webClientBuilder = webClientBuilder;
        CartServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/carts";
        PromoCodeServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/promos";
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

    public Mono<CartResponseDTO> removeProductFromCart(String cartId, String productId) {
        return webClientBuilder.build()
                .delete()
                .uri(CartServiceUrl + "/" + cartId + "/" + productId)
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

    public Flux<PromoCodeResponseDTO> getAllPromoCodes() {
        return webClientBuilder.build()
                .get()
                .uri(PromoCodeServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> getPromoCodeById(String promoCodeId) {
        return webClientBuilder.build()
                .get()
                .uri(PromoCodeServiceUrl + "/" + promoCodeId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> createPromoCode(PromoCodeRequestDTO promoCodeRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(PromoCodeServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(promoCodeRequestDTO), PromoCodeRequestDTO.class)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> updatePromoCode(String promoCodeId, PromoCodeRequestDTO promoCodeRequestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(PromoCodeServiceUrl + "/" + promoCodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(promoCodeRequestDTO), PromoCodeRequestDTO.class)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> deletePromoCode(String promoCodeId) {
        return webClientBuilder.build()
                .delete()
                .uri(PromoCodeServiceUrl + "/" + promoCodeId)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<Void> assignCartToUser(String customerId) {
        return webClientBuilder.build().post()
                .uri(CartServiceUrl + "/" + customerId + "/assign")
                .bodyValue(Collections.emptyList())  //Pass an empty list of CartProduct
                .retrieve()
                .bodyToMono(Void.class);
    }


}

    public Mono<CartResponseDTO> addProductToCart(String cartId, AddProductRequestDTO requestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(CartServiceUrl + "/" + cartId + "/products")
                .body(Mono.just(requestDTO), AddProductRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> updateProductQuantityInCart(String cartId, String productId, UpdateProductQuantityRequestDTO requestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(CartServiceUrl + "/" + cartId + "/products/" + productId)
                .body(Mono.just(requestDTO), UpdateProductQuantityRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> checkoutCart(final String cartId) {
        return webClientBuilder.build()
                .post()
                .uri(CartServiceUrl + "/" + cartId + "/checkout")
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> getCartByCustomerId(final String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(CartServiceUrl + "/customer/" + customerId)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

}
