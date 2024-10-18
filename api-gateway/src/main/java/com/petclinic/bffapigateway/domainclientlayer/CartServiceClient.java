package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.*;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

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
            .onStatus(HttpStatusCode::is4xxClientError, error -> {
                HttpStatusCode statusCode = error.statusCode();
                if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                    return Mono.error(new NotFoundException("Cart not found for CartId: " + CartId));
                }
                else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                    return Mono.error(new InvalidInputException("Cart id is invalid for cart id: " + CartId));
                }
                return Mono.error(new IllegalArgumentException("Client error"));
            })
            .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
            .bodyToMono(CartResponseDTO.class);
}

public Mono<CartResponseDTO> removeProductFromCart(String cartId, String productId) {
    return webClientBuilder.build()
            .delete()
            .uri(CartServiceUrl + "/" + cartId + "/" + productId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, error -> {
                HttpStatusCode statusCode = error.statusCode();
                if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                    return Mono.error(new NotFoundException("Cart or product not found for cartId: " + cartId + " and productId: " + productId));
                }
                else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                    return Mono.error(new InvalidInputException("Invalid input for cartId: " + cartId + " or productId: " + productId));
                }
                return Mono.error(new IllegalArgumentException("Client error"));
            })
            .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
            .bodyToMono(CartResponseDTO.class);
}

public Mono<CartResponseDTO> deleteCartByCartId(String CardId) {
    return webClientBuilder.build()
            .delete()
            .uri(CartServiceUrl + "/" + CardId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, error -> {
                HttpStatusCode statusCode = error.statusCode();
                if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                    return Mono.error(new NotFoundException("Cart not found for CartId: " + CardId));
                }
                else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                    return Mono.error(new InvalidInputException("Cart is invalid for cart id: " + CardId));
                }
                return Mono.error(new IllegalArgumentException("Client error"));
            })
            .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
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
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Cart not found for cartId: " + cartId));
                    }
                    else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                        return Mono.error(new InvalidInputException("Cart id is invalid: " + cartId));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to clear cart with id {}: {}", cartId, e.getMessage()));
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
                .bodyValue(Collections.emptyList())  // Pass an empty list of CartProduct
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Customer not found for customerId: " + customerId));
                    }
                    else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                        return Mono.error(new InvalidInputException("Invalid input for customerId: " + customerId));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(Void.class);
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
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Cart not found for cartId: " + cartId));
                    }
                    else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                        return Mono.error(new InvalidInputException("Cart id invalid for cart id: " + cartId));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> getCartByCustomerId(final String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(CartServiceUrl + "/customer/" + customerId)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> moveProductFromCartToWishlist(String cartId, String productId) {
        return webClientBuilder.build()
                .put()
                .uri(CartServiceUrl + "/" + cartId + "/wishlist/" + productId + "/toWishList")
                .retrieve()
                .bodyToMono(CartResponseDTO.class)  // Use bodyToMono to return CartResponseDTO directly
                .doOnSuccess(cartResponseDTO -> log.info("Moved product {} to wishlist from cart {}", productId, cartId))
                .doOnError(e -> log.error("CartServiceClient Error moving product {} to wishlist from cart {}: {}", productId, cartId, e.getMessage()));
    }


    public Mono<CartResponseDTO> moveProductFromWishListToCart(String cartId, String productId) {
        return webClientBuilder.build()
                .put()
                .uri(CartServiceUrl + "/" + cartId + "/wishlist/" + productId + "/toCart")
                .retrieve()
                .bodyToMono(CartResponseDTO.class) // Use bodyToMono to return CartResponseDTO directly
                .doOnSuccess(cartResponseDTO -> log.info("Moved product {} from wishlist to cart {}", productId, cartId))
                .doOnError(e -> log.error("Error moving product {} from wishlist to cart {}: {}", productId, cartId, e.getMessage()));
    }


}
