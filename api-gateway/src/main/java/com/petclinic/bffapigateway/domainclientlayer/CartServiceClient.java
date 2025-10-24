package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CartServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String cartServiceUrl;
    private final String promoCodeServiceUrl;
    private final String customerCartServiceUrl;

    public CartServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${app.cart-service.host}") String CartServiceHost,
                             @Value("${app.cart-service.port}") String CartServicePort) {
        this.webClientBuilder = webClientBuilder;
    cartServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/carts";
    customerCartServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/customers";
    promoCodeServiceUrl = "http://" + CartServiceHost + ":" + CartServicePort + "/api/v1/promos";
    }

    public Flux<CartResponseDTO> getAllCarts() {
        return getAllCarts(Collections.emptyMap());
    }

    public Flux<CartResponseDTO> getAllCarts(Map<String, ?> queryParams) {
        URI uri = buildCartUri(queryParams);
        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(CartResponseDTO.class);
    }

    private URI buildCartUri(Map<String, ?> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(cartServiceUrl);
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (value == null) {
                    return;
                }
                if (value instanceof Iterable<?> iterable) {
                    iterable.forEach(item -> {
                        if (item != null) {
                            builder.queryParam(key, item);
                        }
                    });
                } else {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.build(true).toUri();
    }

public Mono<CartResponseDTO> getCartByCartId(final String CartId) {
    return webClientBuilder.build()
            .get()
            .uri(cartServiceUrl + "/" + CartId)
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
            .uri(cartServiceUrl + "/" + cartId + "/products/" + productId)
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
            .uri(cartServiceUrl + "/" + CardId)
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

    public Mono<CartResponseDTO> createCart(CartRequestDTO cartRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl)
                .bodyValue(cartRequestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.BAD_REQUEST) || statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                        return Mono.error(new InvalidInputException("Invalid input when creating cart"));
                    } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Related resource not found while creating cart"));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(CartResponseDTO.class);
    }

    //method that helps to delete products from wishlist
    public Mono<CartResponseDTO> removeProductFromWishlist(String cartId, String productId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/" + cartId + "/wishlist/" + productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException(
                                "Wishlist item not found for cartId: " + cartId + " and productId: " + productId));
                    } else if (statusCode.equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                        return Mono.error(new InvalidInputException(
                                "Invalid input for cartId: " + cartId + " or productId: " + productId));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,
                        error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(CartResponseDTO.class)
                .doOnSuccess(res -> log.info("Removed product {} from wishlist in cart {}", productId, cartId))
                .doOnError(e -> log.error("Error removing product {} from wishlist in cart {}: {}", productId, cartId, e.getMessage()));
    }



    public Mono<Void> deleteAllItemsInCart(String cartId) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/" + cartId + "/items")
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
                .doOnError(e -> log.error("Failed to delete items for cart {}: {}", cartId, e.getMessage()));
    }

    public Flux<PromoCodeResponseDTO> getAllPromoCodes() {
        return webClientBuilder.build()
                .get()
                .uri(promoCodeServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(PromoCodeResponseDTO.class);
    }

    public Flux<PromoCodeResponseDTO> getActivePromos() {
        return webClientBuilder.build()
                .get()
                .uri(promoCodeServiceUrl + "/actives")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("No active promos found"));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToFlux(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> getPromoCodeById(String promoCodeId) {
        return webClientBuilder.build()
                .get()
                .uri(promoCodeServiceUrl + "/" + promoCodeId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> createPromoCode(PromoCodeRequestDTO promoCodeRequestDTO) {
        return webClientBuilder.build()
                .post()
                .uri(promoCodeServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(promoCodeRequestDTO)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> updatePromoCode(String promoCodeId, PromoCodeRequestDTO promoCodeRequestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(promoCodeServiceUrl + "/" + promoCodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(promoCodeRequestDTO)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<PromoCodeResponseDTO> deletePromoCode(String promoCodeId) {
        return webClientBuilder.build()
                .delete()
                .uri(promoCodeServiceUrl + "/" + promoCodeId)
                .retrieve()
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<Void> assignCartToUser(String customerId) {
        return webClientBuilder.build().post()
                .uri(cartServiceUrl + "/" + customerId + "/assign")
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

    public Mono<CartResponseDTO> addProductToCart(String cartId, CartItemRequestDTO requestDTO) {
    CartItemRequestDTO payload = requestDTO == null
        ? new CartItemRequestDTO(null, 1)
        : new CartItemRequestDTO(requestDTO.getProductId(), requestDTO.resolveQuantity());

    if (payload.getProductId() != null) {
        payload.setProductId(payload.getProductId().trim());
    }

    return webClientBuilder.build()
        .post()
        .uri(cartServiceUrl + "/" + cartId + "/products")
        .bodyValue(payload)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == 400, clientResponse -> clientResponse.bodyToMono(CartResponseDTO.class)
                        .flatMap(cartResponseDTO -> {
                            if (cartResponseDTO.getMessage() != null) {
                                return Mono.error(new InvalidInputException(cartResponseDTO.getMessage()));
                            } else {
                                return Mono.error(new InvalidInputException("Invalid input"));
                            }
                        }))
                .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(new Exception("An error occurred while adding product to cart")))
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> updateProductQuantityInCart(String cartId, String productId, UpdateProductQuantityRequestDTO requestDTO) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/" + cartId + "/products/" + productId)
                .body(Mono.just(requestDTO), UpdateProductQuantityRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> checkoutCart(final String cartId) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/" + cartId + "/checkout")
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }


    public Mono<CartResponseDTO> getCartByCustomerId(final String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerCartServiceUrl + "/" + customerId + "/cart")
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> moveProductFromCartToWishlist(String cartId, String productId) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/" + cartId + "/wishlist/" + productId + "/toWishList")

                .retrieve()
                .bodyToMono(CartResponseDTO.class)  // Use bodyToMono to return CartResponseDTO directly
                .doOnSuccess(cartResponseDTO -> log.info("Moved product {} to wishlist from cart {}", productId, cartId))
                .doOnError(e -> log.error("CartServiceClient Error moving product {} to wishlist from cart {}: {}", productId, cartId, e.getMessage()));
    }


    public Mono<CartResponseDTO> moveProductFromWishListToCart(String cartId, String productId) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/" + cartId + "/wishlist/" + productId + "/toCart")
                .retrieve()
                .bodyToMono(CartResponseDTO.class) // Use bodyToMono to return CartResponseDTO directly
                .doOnSuccess(cartResponseDTO -> log.info("Moved product {} from wishlist to cart {}", productId, cartId))
                .doOnError(e -> log.error("Error moving product {} from wishlist to cart {}: {}", productId, cartId, e.getMessage()));
    }

    public Mono<CartResponseDTO> addProductToWishList(String cartId, String productId, int quantity) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/" + cartId + "/products/" + productId + "/quantity/" + quantity)
                .retrieve()
                .bodyToMono(CartResponseDTO.class)
                .doOnSuccess(cartResponseDTO -> log.info("Added product {} from product view to wishlist in cart {}", productId, cartId))
                .doOnError(e -> log.error("Error moving product {} to wishlist from cart {}: {}", productId, cartId, e.getMessage()));
    }

    public Mono<PromoCodeResponseDTO> validatePromoCode(String promoCode) {
        return webClientBuilder.build()
                .get()
                .uri(promoCodeServiceUrl + "/validate/{promoCode}", promoCode)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.BAD_REQUEST)) {
                        return Mono.error(new InvalidInputException("Promo code is not valid: " + promoCode));
                    }
                    return Mono.error(new IllegalArgumentException("Client error during promo code validation"));
                })
                .bodyToMono(PromoCodeResponseDTO.class);
    }

    public Mono<CartResponseDTO> addProductToCartFromProducts(String cartId, String productId) {
    return webClientBuilder.build()
        .post()
        .uri(cartServiceUrl + "/" + cartId + "/" + productId)
        .bodyValue(new CartItemRequestDTO(productId, 1))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(CartResponseDTO.class)
                        .flatMap(cartResponseDTO -> {
                            if (cartResponseDTO.getMessage() != null) {
                                return Mono.error(new InvalidInputException(cartResponseDTO.getMessage()));
                            } else {
                                return Mono.error(new InvalidInputException("Invalid input"));
                            }
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(CartResponseDTO.class);
    }
    // move all Wishlist items into cart
    public Mono<CartResponseDTO> moveAllWishlistToCart(String cartId) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/" + cartId + "/wishlist/moveAll")
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(CartResponseDTO.class);
                    }
                    return resp.createException().flatMap(Mono::error);
                });
    }
    public Mono<List<CartProductResponseDTO>> getRecentPurchases(String cartId) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/{cartId}/recent-purchases", cartId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CartProductResponseDTO>>() {});
    }

    public Mono<List<CartProductResponseDTO>> getRecommendationPurchases(String cartId) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/{cartId}/recommendation-purchases", cartId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CartProductResponseDTO>>() {});
    }
    public Mono<CartResponseDTO> applyPromoToCart(String cartId, Double promoPercent) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/{cartId}/promo?promoPercent={promoPercent}", cartId, promoPercent)
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

    public Mono<CartResponseDTO> clearPromo(String cartId) {
        return webClientBuilder.build()
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(cartServiceUrl + "/{cartId}/promo")
                        .queryParam("promoPercent", 0)
                        .build(cartId))
                .retrieve()
                .bodyToMono(CartResponseDTO.class);
    }

}
