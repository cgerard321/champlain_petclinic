package com.petclinic.bffapigateway.presentationlayer.v2;


import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
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

    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseDTO>> checkoutCart(@PathVariable String cartId) {
        return cartServiceClient.checkoutCart(cartId)
                .map(cart -> new ResponseEntity<>(cart, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}


