package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.*;



import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping(value = "/{cartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> getCartByCartId(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::getCartByCartId)
                .map(ResponseEntity::ok);
    }



    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CartResponseModel> getAllCarts() {
        return cartService.getAllCarts();

    // Adding the clearCart method from feat/CART-CPC-1144_clear_cart_feature
    @DeleteMapping("/{cartId}/clear")
    public Flux<ProductResponseModel> clearCart(@PathVariable String cartId) {
        return cartService.clearCart(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found")));
    }

    // Adding the updateCartByCartId method from main
    @PutMapping(value = "/{cartId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> updateCartByCartId(@RequestBody Mono<CartRequestModel> cartRequestModel, @PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(id -> cartService.updateCartByCartId(cartRequestModel, id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }


    }

