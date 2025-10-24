package com.petclinic.bffapigateway.presentationlayer.v2;

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


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/carts")
@Validated
@Slf4j
public class CartController {

    private final CartServiceClient cartServiceClient;

    //later we will need to check if the user that is logged in isn't getting someone else's carts, but that's for sprint 3
    //@SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping("/{cartId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartById(@PathVariable String cartId) {
        return cartServiceClient.getCartByCartId(cartId)
                .map(cart -> ResponseEntity.status(HttpStatus.OK).body(cart))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping
    public Flux<CartResponseDTO> getAllCarts() {
        return cartServiceClient.getAllCarts()
                .doOnNext(cart -> log.debug("The cart response from the API gateway is: " + cart.toString()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> deleteCartByCartId(@PathVariable String cartId){
        return cartServiceClient.deleteCartByCartId(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{cartId}/items")
    public Mono<ResponseEntity<Void>> deleteAllItemsInCart(@PathVariable String cartId) {
        return cartServiceClient.deleteAllItemsInCart(cartId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(ResponseEntity.unprocessableEntity().build()))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).build()));
    }

    @DeleteMapping(value = "/{cartId}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return cartServiceClient.removeProductFromCart(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
}

    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(
            @PathVariable String cartId,
            @RequestBody CartItemRequestDTO requestDTO) {
        final String requestedProductId = requestDTO != null && requestDTO.getProductId() != null
                ? requestDTO.getProductId().trim()
                : null;

        return cartServiceClient.addProductToCart(cartId, requestDTO)
                .map(cartResponseDTO -> {
                    if (requestedProductId != null && !requestedProductId.isBlank()) {
                        return ResponseEntity.created(URI.create(String.format("/api/v1/carts/%s/items/%s", cartId, requestedProductId)))
                                .body(cartResponseDTO);
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).body(cartResponseDTO);
                })
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException || e instanceof WebClientResponseException.BadRequest) {
                        CartResponseDTO errorResponse = new CartResponseDTO();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        return Mono.just(ResponseEntity.notFound().build());
                    } else if (e instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) e;
                        return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                    } else {
                        return Mono.error(e);
                    }
                });
    }


    @PutMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> updateProductQuantityInCart(@PathVariable String cartId, @PathVariable String productId, @RequestBody UpdateProductQuantityRequestDTO requestDTO) {
        return cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    }
                    else if (e instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    else {
                        return Mono.error(e);
                    }
                });
    }

    //admin restriction at check out
    @SecuredEndpoint(allowedRoles = {Roles.OWNER})
    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return cartServiceClient.checkoutCart(cartId)
                .map(cart -> new ResponseEntity<>(cart, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartByCustomerId(@PathVariable String customerId) {
        return cartServiceClient.getCartByCustomerId(customerId)
                .map(cart -> ResponseEntity.status(HttpStatus.OK).body(cart))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    /*
    --------------------------------------------------------------------------------------------------------------------
        from here it's related to the wishlist
    */


    @PutMapping("/{cartId}/wishlist/{productId}/toCart")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromWishListToCart(@PathVariable String cartId, @PathVariable String productId) {
        return cartServiceClient.moveProductFromWishListToCart(cartId, productId)
                .map(cartResponseDTO -> ResponseEntity.ok(cartResponseDTO)) // Map directly to ResponseEntity
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        log.error("Invalid input for cartId: {} or productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    } else if (e instanceof WebClientResponseException.NotFound) {
                        log.error("Cart or product not found for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        log.error("An unexpected error occurred: {}", e.getMessage());
                        return Mono.error(e);
                    }
                });
    }


    @PutMapping("/{cartId}/wishlist/{productId}/toWishList")
    public Mono<ResponseEntity<CartResponseDTO>> moveProductFromCartToWishlist(@PathVariable String cartId, @PathVariable String productId) {
        return cartServiceClient.moveProductFromCartToWishlist(cartId, productId)
                .map(cartResponseDTO -> ResponseEntity.ok(cartResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        log.error("Invalid input for cartId: {} or productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    } else if (e instanceof WebClientResponseException.NotFound) {
                        log.error("Cart or product not found for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        log.error("An unexpected error occurred: {}", e.getMessage());
                        return Mono.error(e);
                    }
                });
    }

    @PostMapping("/{cartId}/products/{productId}/quantity/{quantity}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToWishList(@PathVariable String cartId, @PathVariable String productId, @PathVariable int quantity) {
        return cartServiceClient.addProductToWishList(cartId, productId, quantity)
                .map(cartResponseDTO -> ResponseEntity.ok(cartResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        log.error("Invalid input for cartId: {} or productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    } else if (e instanceof WebClientResponseException.NotFound) {
                        log.error("Cart or product not found for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        log.error("An unexpected error occurred: {}", e.getMessage());
                        return Mono.error(e);
                    }
                });
    }



    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromWishlist(
            @PathVariable String cartId,
            @PathVariable String productId) {

        return cartServiceClient.removeProductFromWishlist(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        log.error("Invalid input for cartId: {} or productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    } else if (e instanceof WebClientResponseException.NotFound) {
                        log.error("Wishlist item not found for cartId: {} and productId: {} - {}", cartId, productId, e.getMessage());
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        log.error("Unexpected error while removing wishlist item: {}", e.getMessage());
                        return Mono.error(e);
                    }
                });
    }


    // move all Wishlist items into cart
    @PostMapping(value = "/{cartId}/wishlist/moveAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> moveAllWishlistToCart(@PathVariable String cartId) {
        return cartServiceClient.moveAllWishlistToCart(cartId)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.NotFound) {
                        CartResponseDTO dto = new CartResponseDTO();
                        dto.setMessage("Cart not found: " + cartId);
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto));
                    }
                    if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.UnprocessableEntity
                            || ex instanceof com.petclinic.bffapigateway.exceptions.InvalidInputException) {
                        CartResponseDTO dto = new CartResponseDTO();
                        dto.setMessage(ex.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(dto));
                    }
                    if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException wce) {
                        CartResponseDTO dto = new CartResponseDTO();
                        String msg = (wce.getResponseBodyAsString() != null && !wce.getResponseBodyAsString().isBlank())
                                ? wce.getResponseBodyAsString()
                                : wce.getStatusText();
                        dto.setMessage(msg);
                        return Mono.just(ResponseEntity.status(wce.getStatusCode()).body(dto));
                    }
                    log.error("moveAllWishlistToCart unexpected error for cartId {}: {}", cartId, ex.getMessage(), ex);
                    CartResponseDTO dto = new CartResponseDTO();
                    dto.setMessage("Unexpected error");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto));
                });
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "promos", produces= MediaType.APPLICATION_JSON_VALUE)
    public Flux<PromoCodeResponseDTO> getAllPromos() {
        return cartServiceClient.getAllPromoCodes();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "promos/{promoCodeId}", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseDTO>> getPromoCodeById(@PathVariable String promoCodeId) {
        return cartServiceClient.getPromoCodeById(promoCodeId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCartFromProducts(
            @PathVariable String cartId,
            @PathVariable String productId) {

        return cartServiceClient.addProductToCartFromProducts(cartId, productId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException || e instanceof WebClientResponseException.BadRequest) {
                        CartResponseDTO errorResponse = new CartResponseDTO();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        return Mono.just(ResponseEntity.notFound().build());
                    } else if (e instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) e;
                        return Mono.just(ResponseEntity.status(ex.getStatusCode()).build());
                    } else {
                        return Mono.error(e);
                    }
                });
    }

}



