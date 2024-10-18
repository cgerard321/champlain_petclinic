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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/carts")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
@Slf4j
public class CartController {

    private final CartServiceClient cartServiceClient;

    //later we will need to check if the user that is logged in isnt getting someone else's carts, but thats for sprint 3
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

//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @PutMapping(value = "/{cartId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<CartResponseDTO>> updateCartById(@RequestBody Mono<CartRequestDTO> cartRequestDTO,
//                                                                @PathVariable String cartId){
//        return cartServiceClient.updateCartByCartId(cartRequestDTO, cartId)
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.notFound().build());
//    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> deleteCartByCartId(@PathVariable String cartId){
        return cartServiceClient.deleteCartByCartId(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{cartId}/clear")
    public Mono<ResponseEntity<String>> clearCart(@PathVariable String cartId) {
        return cartServiceClient.clearCart(cartId)
                .thenReturn(ResponseEntity.ok("Cart successfully cleared"))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseDTO>> addProductToCart(@PathVariable String cartId, @RequestBody AddProductRequestDTO requestDTO) {
        return cartServiceClient.addProductToCart(cartId, requestDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().build());
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
                    if (e instanceof WebClientResponseException.BadRequest) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    } else {
                        return Mono.error(e);
                    }
                });
    }

    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return cartServiceClient.checkoutCart(cartId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("Checkout error: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }


    @GetMapping("/customer/{customerId}")
    public Mono<ResponseEntity<CartResponseDTO>> getCartByCustomerId(@PathVariable String customerId) {
        return cartServiceClient.getCartByCustomerId(customerId)
                .map(cart -> ResponseEntity.status(HttpStatus.OK).body(cart))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @DeleteMapping(value = "/{cartId}/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseDTO>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return cartServiceClient.removeProductFromCart(cartId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

}


