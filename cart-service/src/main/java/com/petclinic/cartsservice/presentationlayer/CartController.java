package com.petclinic.cartsservice.presentationlayer;


import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping(value= "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> addCourse(@RequestBody CartRequestModel cartRequestModel) {
        return cartService.CreateNewCart(cartRequestModel)
                .map(c-> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

}
