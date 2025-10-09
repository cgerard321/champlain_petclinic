package com.petclinic.bffapigateway.presentationlayer.v1;


import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.AddProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/carts")
@Validated
@Slf4j
public class CartControllerV1 {
    private final CartServiceClient cartServiceClient;

    // --- Utility Methods ---
    private <T> Mono<ResponseEntity<T>> mapToOkOrNotFound(Mono<T> mono) {
        return mono.map(ResponseEntity::ok)
                   .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Parameter object to configure error mapping (builder pattern)
    private static final class ErrorOptions {
        final String context;
        final String cartId;
        final String productId;
        final boolean includeBadRequestBodyMessage;
        final boolean invalidInputAsUnprocessable;

        private ErrorOptions(Builder b) {
            this.context = b.context;
            this.cartId = b.cartId;
            this.productId = b.productId;
            this.includeBadRequestBodyMessage = b.includeBadRequestBodyMessage;
            this.invalidInputAsUnprocessable = b.invalidInputAsUnprocessable;
        }

        static Builder builder(String context) { return new Builder(context); }

        static final class Builder {
            private final String context;
            private String cartId;
            private String productId;
            private boolean includeBadRequestBodyMessage;
            private boolean invalidInputAsUnprocessable;

            private Builder(String context) { this.context = context; }
            Builder cartId(String v) { this.cartId = v; return this; }
            Builder productId(String v) { this.productId = v; return this; }
            Builder includeBadRequestBodyMessage(boolean v) { this.includeBadRequestBodyMessage = v; return this; }
            Builder invalidInputAsUnprocessable(boolean v) { this.invalidInputAsUnprocessable = v; return this; }
            ErrorOptions build() { return new ErrorOptions(this); }
        }
    }

    // Centralized error mapper (status-only). Success mapping stays at endpoints.
    private <T> Mono<ResponseEntity<T>> mapCartError(Throwable e, ErrorOptions o) {
        log.error("{} error for cartId: {}, productId: {} - {}", o.context, o.cartId, o.productId, e.getMessage());

        if (e instanceof WebClientResponseException.UnprocessableEntity) {
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }

        if (e instanceof InvalidInputException) {
            if (o.invalidInputAsUnprocessable) {
                return Mono.just(ResponseEntity.unprocessableEntity().build());
            }
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (e instanceof NotFoundException || e instanceof WebClientResponseException.NotFound) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        if (e instanceof WebClientResponseException.BadRequest) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (e instanceof WebClientResponseException ex) {
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    // Variant that can include a CartResponseDTO error body message on 400 scenarios
    private Mono<ResponseEntity<CartResponseDTO>> mapCartErrorWithMessage(Throwable e, ErrorOptions o) {
        log.error("{} error for cartId: {}, productId: {} - {}", o.context, o.cartId, o.productId, e.getMessage());

        if (e instanceof WebClientResponseException.UnprocessableEntity) {
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }

        if (e instanceof InvalidInputException) {
            if (o.invalidInputAsUnprocessable) {
                return Mono.just(ResponseEntity.unprocessableEntity().build());
            }
            if (o.includeBadRequestBodyMessage) {
                CartResponseDTO errorBody = new CartResponseDTO();
                errorBody.setMessage(e.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(errorBody));
            }
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (e instanceof NotFoundException || e instanceof WebClientResponseException.NotFound) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        if (e instanceof WebClientResponseException.BadRequest) {
            if (o.includeBadRequestBodyMessage) {
                CartResponseDTO errorBody = new CartResponseDTO();
                errorBody.setMessage(e.getMessage());
                return Mono.just(ResponseEntity.badRequest().body(errorBody));
            }
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (e instanceof WebClientResponseException ex) {
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    // --- Endpoints ---

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping
    public Mono<ResponseEntity<CartResponseDTO>> createCart(@RequestBody CartRequestDTO cartRequestDTO) {
        return cartServiceClient.createCart(cartRequestDTO)
                .map(cart -> ResponseEntity.status(HttpStatus.CREATED).body(cart))
                .onErrorResume(e -> mapCartErrorWithMessage(e,
                        ErrorOptions.builder("createCart")
                                .includeBadRequestBodyMessage(true)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{cartId}/count")
    public Mono<ResponseEntity<Map<String, Integer>>> getCartItemCount(@PathVariable String cartId) {
        return cartServiceClient.getCartItemCount(cartId)
                .map(count -> ResponseEntity.ok(Collections.singletonMap("itemCount", count)))
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("getCartItemCount")
                                .cartId(cartId)
                                .invalidInputAsUnprocessable(true)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CartResponseDTO> getAllCarts() {
        return cartServiceClient.getAllCarts();
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{cartId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartById(@PathVariable String cartId) {
        return mapToOkOrNotFound(cartServiceClient.getCartByCartId(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> deleteCartByCartId(@PathVariable String cartId){
        return mapToOkOrNotFound(cartServiceClient.deleteCartByCartId(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/clear")
    public Mono<ResponseEntity<String>> clearCart(@PathVariable String cartId) {
        return cartServiceClient.clearCart(cartId)
                .thenReturn(ResponseEntity.ok("Cart successfully cleared"))
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("clearCart")
                                .cartId(cartId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value = "/{cartId}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return cartServiceClient.removeProductFromCart(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("removeProductFromCart")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(
            @PathVariable String cartId,
            @RequestBody AddProductRequestDTO requestDTO) {
        return cartServiceClient.addProductToCart(cartId, requestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> mapCartErrorWithMessage(e,
                        ErrorOptions.builder("addProductToCart")
                                .cartId(cartId)
                                .includeBadRequestBodyMessage(true)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> updateProductQuantityInCart(@PathVariable String cartId, @PathVariable String productId, @RequestBody UpdateProductQuantityRequestDTO requestDTO) {
        return cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("updateProductQuantityInCart")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return mapToOkOrNotFound(cartServiceClient.checkoutCart(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartByCustomerId(@PathVariable String customerId) {
        return mapToOkOrNotFound(cartServiceClient.getCartByCustomerId(customerId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/wishlist/{productId}/toCart")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromWishListToCart(@PathVariable String cartId, @PathVariable String productId) {
        return cartServiceClient.moveProductFromWishListToCart(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("moveProductFromWishListToCart")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/wishlist/{productId}/toWishList")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromCartToWishlist(@PathVariable String cartId, @PathVariable String productId) {
        return cartServiceClient.moveProductFromCartToWishlist(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("moveProductFromCartToWishlist")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products/{productId}/quantity/{quantity}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToWishList(@PathVariable String cartId, @PathVariable String productId, @PathVariable int quantity) {
        return cartServiceClient.addProductToWishList(cartId, productId, quantity)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("addProductToWishList")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromWishlist(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return cartServiceClient.removeProductFromWishlist(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("removeProductFromWishlist")
                                .cartId(cartId)
                                .productId(productId)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCartFromProducts(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return cartServiceClient.addProductToCartFromProducts(cartId, productId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> mapCartErrorWithMessage(e,
                        ErrorOptions.builder("addProductToCartFromProducts")
                                .cartId(cartId)
                                .productId(productId)
                                .includeBadRequestBodyMessage(true)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/wishlist/moveAll")
    public Mono<ResponseEntity<CartResponseDTO>> moveAllWishlistToCart(
            @PathVariable String cartId) {

        return cartServiceClient.moveAllWishlistToCart(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e,
                        ErrorOptions.builder("moveAllWishlistToCart")
                                .cartId(cartId)
                                .build()
                ));
    }
    @GetMapping("/{cartId}/recent-purchases")
    public Mono<ResponseEntity<List<CartProductResponseDTO>>> getRecentPurchases(@PathVariable String cartId) {
        return cartServiceClient.getRecentPurchases(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
