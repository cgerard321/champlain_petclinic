package com.petclinic.cartsservice.presentationlayer;


import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

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

    // New endpoint to get the number of items in the cart
    @GetMapping("/{cartId}/count")
    public Mono<ResponseEntity<Map<String, Integer>>> getCartItemCount(@PathVariable String cartId) {
        return cartService.getCartItemCount(cartId)
                .map(count -> ResponseEntity.ok(Collections.singletonMap("itemCount", count)))
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found for ID: " + cartId)));
    }

}
