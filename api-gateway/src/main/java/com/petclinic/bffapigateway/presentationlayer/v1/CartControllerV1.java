package com.petclinic.bffapigateway.presentationlayer.v1;


import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.AddProductRequestDTO;
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

    private Mono<ResponseEntity<CartResponseDTO>> handleCartErrors(
            Mono<CartResponseDTO> mono, String cartId, String productId) {
        return mono.map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume(e -> handleErrors(e, cartId, productId));
    }

    private Mono<ResponseEntity<CartResponseDTO>> handleAddProductErrors(Throwable e) {
        if (e instanceof InvalidInputException || e instanceof WebClientResponseException.BadRequest) {
            CartResponseDTO errorResponse = new CartResponseDTO();
            errorResponse.setMessage(e.getMessage());
            log.error("Bad request: {}", e.getMessage());
            return Mono.just(ResponseEntity.badRequest().body(errorResponse));
        } else if (e instanceof NotFoundException || e instanceof WebClientResponseException.NotFound) {
            log.error("Not found: {}", e.getMessage());
            return Mono.just(ResponseEntity.notFound().build());
        } else if (e instanceof WebClientResponseException ex) {
            log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getMessage());
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
        } else {
            log.error("Unexpected error: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    private <T> Mono<ResponseEntity<T>> handleErrors(Throwable e, String cartId, String productId) {
        if (e instanceof WebClientResponseException.UnprocessableEntity) {
            log.error("Invalid input for cartId: {} or productId: {} - {}", cartId, productId, e.getMessage());
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        } else if (e instanceof WebClientResponseException.NotFound || e instanceof NotFoundException) {
            log.error("Not found for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
            return Mono.just(ResponseEntity.notFound().build());
        } else if (e instanceof WebClientResponseException.BadRequest) {
            log.error("Bad request for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
            return Mono.just(ResponseEntity.badRequest().build());
        } else if (e instanceof WebClientResponseException ex) {
            log.error("WebClient error for cartId: {} and productId: {} - {} - {}", cartId, productId, ex.getStatusCode(), ex.getMessage());
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
        } else {
            log.error("Unexpected error for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }

    // --- Endpoints ---

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CartResponseDTO> getAllCarts() {
        return cartServiceClient.getAllCarts();
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
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
                .onErrorResume(e -> handleErrors(e, cartId, null));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping(value = "/{cartId}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return handleCartErrors(cartServiceClient.removeProductFromCart(cartId, productId), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(
            @PathVariable String cartId,
            @RequestBody AddProductRequestDTO requestDTO) {
        return cartServiceClient.addProductToCart(cartId, requestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(this::handleAddProductErrors);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> updateProductQuantityInCart(@PathVariable String cartId, @PathVariable String productId, @RequestBody UpdateProductQuantityRequestDTO requestDTO) {
        return handleCartErrors(cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return mapToOkOrNotFound(cartServiceClient.checkoutCart(cartId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartByCustomerId(@PathVariable String customerId) {
        return mapToOkOrNotFound(cartServiceClient.getCartByCustomerId(customerId));
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/wishlist/{productId}/toCart")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromWishListToCart(@PathVariable String cartId, @PathVariable String productId) {
        return handleCartErrors(cartServiceClient.moveProductFromWishListToCart(cartId, productId), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PutMapping("/{cartId}/wishlist/{productId}/toWishList")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromCartToWishlist(@PathVariable String cartId, @PathVariable String productId) {
        return handleCartErrors(cartServiceClient.moveProductFromCartToWishlist(cartId, productId), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/products/{productId}/quantity/{quantity}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToWishList(@PathVariable String cartId, @PathVariable String productId, @PathVariable int quantity) {
        return handleCartErrors(cartServiceClient.addProductToWishList(cartId, productId, quantity), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromWishlist(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return handleCartErrors(cartServiceClient.removeProductFromWishlist(cartId, productId), cartId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCartFromProducts(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return cartServiceClient.addProductToCartFromProducts(cartId, productId)
                .map(ResponseEntity::ok)
                .onErrorResume(this::handleAddProductErrors);
    }
    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/wishlist/moveAll")
    public Mono<ResponseEntity<CartResponseDTO>> moveAllWishlistToCart(
            @PathVariable String cartId) {

        return handleCartErrors(
                cartServiceClient.moveAllWishlistToCart(cartId),
                cartId,
                null
        );
    }

}
