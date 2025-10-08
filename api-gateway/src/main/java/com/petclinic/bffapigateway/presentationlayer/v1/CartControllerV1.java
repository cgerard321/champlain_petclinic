package com.petclinic.bffapigateway.presentationlayer.v1;


import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.AddProductRequestDTO;
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

    // Centralized error mapper used by endpoints' onErrorResume. Success mapping stays in endpoints.
    private <R> Mono<ResponseEntity<R>> mapCartError(
            Throwable e,
            String context,
            String cartId,
            String productId,
            Class<R> bodyType,
            boolean includeBadRequestBodyMessage,
            boolean invalidInputAsUnprocessable
    ) {
        log.error("{} error for cartId: {}, productId: {} - {}", context, cartId, productId, e.getMessage());

        // 422 for explicit UnprocessableEntity from downstream
        if (e instanceof WebClientResponseException.UnprocessableEntity) {
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }

        // InvalidInput exception can map to 400 or 422 depending on endpoint needs
        if (e instanceof InvalidInputException) {
            if (invalidInputAsUnprocessable) {
                return Mono.just(ResponseEntity.unprocessableEntity().build());
            }
            if (includeBadRequestBodyMessage && bodyType != null && CartResponseDTO.class.isAssignableFrom(bodyType)) {
                CartResponseDTO errorBody = new CartResponseDTO();
                errorBody.setMessage(e.getMessage());
                R casted = bodyType.cast(errorBody);
                return Mono.just(ResponseEntity.badRequest().body(casted));
            }
            return Mono.just(ResponseEntity.badRequest().build());
        }

        // 404 mapping
        if (e instanceof NotFoundException || e instanceof WebClientResponseException.NotFound) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        // 400 mapping (optionally with CartResponseDTO body message)
        if (e instanceof WebClientResponseException.BadRequest) {
            if (includeBadRequestBodyMessage && bodyType != null && CartResponseDTO.class.isAssignableFrom(bodyType)) {
                CartResponseDTO errorBody = new CartResponseDTO();
                errorBody.setMessage(e.getMessage());
                R casted = bodyType.cast(errorBody);
                return Mono.just(ResponseEntity.badRequest().body(casted));
            }
            return Mono.just(ResponseEntity.badRequest().build());
        }

        // Pass-through status for other WebClient exceptions
        if (e instanceof WebClientResponseException ex) {
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
        }

        // Default 500
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    // --- Endpoints ---

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping
    public Mono<ResponseEntity<CartResponseDTO>> createCart(@RequestBody CartRequestDTO cartRequestDTO) {
        return cartServiceClient.createCart(cartRequestDTO)
                .map(cart -> ResponseEntity.status(HttpStatus.CREATED).body(cart))
                .onErrorResume(e -> mapCartError(e, "createCart", null, null, CartResponseDTO.class, true, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{cartId}/count")
    public Mono<ResponseEntity<Map<String, Integer>>> getCartItemCount(@PathVariable String cartId) {
        return cartServiceClient.getCartItemCount(cartId)
                .map(count -> ResponseEntity.ok(Collections.singletonMap("itemCount", count)))
                .onErrorResume(e -> mapCartError(e, "getCartItemCount", cartId, null, null, false, true));
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
                .onErrorResume(e -> mapCartError(e, "clearCart", cartId, null, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value = "/{cartId}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return cartServiceClient.removeProductFromCart(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "removeProductFromCart", cartId, productId, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(
            @PathVariable String cartId,
            @RequestBody AddProductRequestDTO requestDTO) {
        return cartServiceClient.addProductToCart(cartId, requestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> mapCartError(e, "addProductToCart", cartId, null, CartResponseDTO.class, true, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> updateProductQuantityInCart(@PathVariable String cartId, @PathVariable String productId, @RequestBody UpdateProductQuantityRequestDTO requestDTO) {
        return cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "updateProductQuantityInCart", cartId, productId, null, false, false));
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
                .onErrorResume(e -> mapCartError(e, "moveProductFromWishListToCart", cartId, productId, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/wishlist/{productId}/toWishList")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromCartToWishlist(@PathVariable String cartId, @PathVariable String productId) {
        return cartServiceClient.moveProductFromCartToWishlist(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "moveProductFromCartToWishlist", cartId, productId, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products/{productId}/quantity/{quantity}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToWishList(@PathVariable String cartId, @PathVariable String productId, @PathVariable int quantity) {
        return cartServiceClient.addProductToWishList(cartId, productId, quantity)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "addProductToWishList", cartId, productId, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromWishlist(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return cartServiceClient.removeProductFromWishlist(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "removeProductFromWishlist", cartId, productId, null, false, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCartFromProducts(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return cartServiceClient.addProductToCartFromProducts(cartId, productId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> mapCartError(e, "addProductToCartFromProducts", cartId, productId, CartResponseDTO.class, true, false));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/wishlist/moveAll")
    public Mono<ResponseEntity<CartResponseDTO>> moveAllWishlistToCart(
            @PathVariable String cartId) {

        return cartServiceClient.moveAllWishlistToCart(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> mapCartError(e, "moveAllWishlistToCart", cartId, null, null, false, false));
    }

}
