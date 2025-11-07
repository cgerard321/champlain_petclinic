package com.petclinic.bffapigateway.presentationlayer.v1;


import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.*;
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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
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
    public static final class ErrorOptions {
        final boolean includeBadRequestBodyMessage;
        final boolean invalidInputAsUnprocessable;

        private ErrorOptions(Builder builder) {
            this.includeBadRequestBodyMessage = builder.includeBadRequestBodyMessage;
            this.invalidInputAsUnprocessable = builder.invalidInputAsUnprocessable;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private boolean includeBadRequestBodyMessage;
            private boolean invalidInputAsUnprocessable;

            public Builder includeBadRequestBodyMessage(boolean value) {
                this.includeBadRequestBodyMessage = value;
                return this;
            }

            public Builder invalidInputAsUnprocessable(boolean value) {
                this.invalidInputAsUnprocessable = value;
                return this;
            }

            public ErrorOptions build() {
                return new ErrorOptions(this);
            }
        }
    }

    // Centralized error mapper (status-only). Success mapping stays at endpoints.
    private <T> Mono<ResponseEntity<T>> mapCartError(Throwable e, ErrorOptions o) {
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
    public Mono<ResponseEntity<CartResponseDTO>> mapCartErrorWithMessage(Throwable e, ErrorOptions o) {
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

    private Map<String, Object> buildCartQueryParams(Integer page, Integer size, String customerId, String customerName, Boolean assigned) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (page != null) params.put("page", page);
        if (size != null) params.put("size", size);
        if (customerId != null && !customerId.isBlank()) params.put("customerId", customerId.trim());
        if (customerName != null && !customerName.isBlank()) params.put("customerName", customerName.trim());
        if (assigned != null) params.put("assigned", assigned);
        return params;
    }

    // --- Endpoints ---

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping
    public Mono<ResponseEntity<CartResponseDTO>> createCart(@RequestBody CartRequestDTO cartRequestDTO) {
        return cartServiceClient.createCart(cartRequestDTO)
                .map(cart -> ResponseEntity.status(HttpStatus.CREATED).body(cart))
                .onErrorResume(e -> mapCartErrorWithMessage(e,
                        ErrorOptions.builder()
                                .includeBadRequestBodyMessage(true)
                                .build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CartResponseDTO>>> getAllCartsAsJson(
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "customerId", required = false) String customerId,
        @RequestParam(value = "customerName", required = false) String customerName,
        @RequestParam(value = "assigned", required = false) Boolean assigned
    ) {
    Map<String, Object> queryParams = buildCartQueryParams(page, size, customerId, customerName, assigned);

    return cartServiceClient
        .getAllCarts(queryParams)
        .collectList()
        .map(ResponseEntity::ok)
        .onErrorResume(e -> mapCartError(
            e,
            ErrorOptions.builder()
                .invalidInputAsUnprocessable(true)
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CartResponseDTO> getAllCartsStream(
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "customerId", required = false) String customerId,
        @RequestParam(value = "customerName", required = false) String customerName,
        @RequestParam(value = "assigned", required = false) Boolean assigned
    ) {
    Map<String, Object> queryParams = buildCartQueryParams(page, size, customerId, customerName, assigned);
    return cartServiceClient.getAllCarts(queryParams);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{cartId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartById(@PathVariable String cartId) {
        return mapToOkOrNotFound(cartServiceClient.getCartByCartId(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> deleteCartByCartId(@PathVariable String cartId){
    return cartServiceClient.deleteCartByCartId(cartId)
        .thenReturn(ResponseEntity.noContent().<Void>build())
        .onErrorResume(e -> this.<Void>mapCartError(e,
            ErrorOptions.builder().build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/products")
    public Mono<ResponseEntity<Void>> deleteAllItemsInCart(@PathVariable String cartId) {
    return cartServiceClient.deleteAllItemsInCart(cartId)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .onErrorResume(e -> this.<Void>mapCartError(e,
            ErrorOptions.builder()
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value = "/{cartId}/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
    return cartServiceClient.removeProductFromCart(cartId, productId)
        .thenReturn(ResponseEntity.noContent().<Void>build())
        .onErrorResume(e -> this.<Void>mapCartError(e,
            ErrorOptions.builder()
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(
        @PathVariable String cartId,
        @RequestBody CartItemRequestDTO requestDTO) {
    final String requestedProductId = requestDTO != null && requestDTO.getProductId() != null
        ? requestDTO.getProductId().trim()
        : null;

    return cartServiceClient.addProductToCart(cartId, requestDTO)
        .map(cartResponse -> {
            if (requestedProductId != null && !requestedProductId.isBlank()) {
            return ResponseEntity.created(URI.create(String.format("/api/gateway/carts/%s/products/%s", cartId, requestedProductId)))
                .body(cartResponse);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(cartResponse);
        })
        .onErrorResume(e -> mapCartErrorWithMessage(e,
            ErrorOptions.builder()
                .includeBadRequestBodyMessage(true)
                .build()
        ));
    }

    private Mono<ResponseEntity<CartResponseDTO>> handleQuantityUpdate(String cartId,
                                    String productId,
                                    UpdateProductQuantityRequestDTO requestDTO) {
    return cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> mapCartError(e,
            ErrorOptions.builder()
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PatchMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> patchProductQuantityInCart(@PathVariable String cartId,
                                        @PathVariable String productId,
                                        @RequestBody UpdateProductQuantityRequestDTO requestDTO) {
    return handleQuantityUpdate(cartId, productId, requestDTO);
    }
    
    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return mapToOkOrNotFound(cartServiceClient.checkoutCart(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/wishlist")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToWishlist(
        @PathVariable String cartId,
        @RequestBody WishlistItemRequestDTO requestDTO) {

    return cartServiceClient.addProductToWishlist(cartId, requestDTO)
        .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
        .onErrorResume(e -> mapCartErrorWithMessage(
            e,
            ErrorOptions.builder()
                .includeBadRequestBodyMessage(true)
                .invalidInputAsUnprocessable(false)
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<Void>> removeProductFromWishlist(
        @PathVariable String cartId,
        @PathVariable String productId) {
    return cartServiceClient.removeProductFromWishlist(cartId, productId)
        .thenReturn(ResponseEntity.noContent().<Void>build())
        .onErrorResume(e -> this.<Void>mapCartError(e,
            ErrorOptions.builder()
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/wishlist-transfers")
    public Mono<ResponseEntity<CartResponseDTO>> createWishlistTransfer(
        @PathVariable String cartId,
        @RequestBody(required = false) WishlistTransferRequestDTO request) {

    List<String> productIds = request != null ? request.normalizedProductIds() : List.of();
    WishlistTransferDirectionDTO direction = request != null
        ? request.resolvedDirection()
        : WishlistTransferDirectionDTO.defaultDirection();

    return cartServiceClient.createWishlistTransfer(cartId, productIds, direction)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build())
        .onErrorResume(e -> mapCartError(e,
            ErrorOptions.builder()
                .build()
        ));
    }
    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/promo")
    public Mono<ResponseEntity<CartResponseDTO>> applyPromoToCart(
        @PathVariable String cartId,
        @RequestBody(required = false) ApplyPromoRequestDTO promoRequest) {

    Double promoPercent = promoRequest != null ? promoRequest.getPromoPercent() : null;

    return cartServiceClient.applyPromoToCart(cartId, promoPercent)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> mapCartErrorWithMessage(
            e,
            ErrorOptions.builder()
                .includeBadRequestBodyMessage(true)
                .build()
        ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/promos/validate/{promoCode}")
    public Mono<ResponseEntity<PromoCodeResponseDTO>> validatePromo(
            @PathVariable String promoCode) {
        return cartServiceClient.validatePromoCode(promoCode)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> mapCartError(
                        e, ErrorOptions.builder().build()
                ));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/promo")
    public Mono<ResponseEntity<Void>> clearPromo(@PathVariable String cartId) {
    return cartServiceClient.clearPromo(cartId)
        .thenReturn(ResponseEntity.noContent().<Void>build())
        .onErrorResume(e -> this.<Void>mapCartError(
            e, ErrorOptions.builder().build()
        ));
    }


}
