package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.domainclientlayer.AddProductRequestModel;
import com.petclinic.cartsservice.domainclientlayer.UpdateProductQuantityRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
@Slf4j
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
        return cartService.getAllCarts()
                .doOnNext(e -> log.debug("cart-service controller is returning cart data: " + e.toString()));
    }

    @DeleteMapping("/{cartId}/clear")
    public Flux<CartResponseModel> clearCart(@PathVariable String cartId) {
        return Flux.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::clearCart);
    }

//    @PutMapping(value = "/{cartId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<ResponseEntity<CartResponseModel>> updateCartByCartId(@RequestBody Mono<CartRequestModel> cartRequestModel, @PathVariable String cartId) {
//        return Mono.just(cartId)
//                .filter(id -> id.length() == 36)
//                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
//                .flatMap(id -> cartService.updateCartByCartId(cartRequestModel, id))
//                .map(ResponseEntity::ok)
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//
//    }
    //we are going to subdivide this method into multiple methods

    @GetMapping("/{cartId}/count")
    public Mono<ResponseEntity<Map<String, Integer>>> getCartItemCount(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::getCartItemCount)
                .map(count -> ResponseEntity.ok(Collections.singletonMap("itemCount", count)));
    }

    @DeleteMapping("/{cartId}")
    public Mono<ResponseEntity<CartResponseModel>> deleteCartByCartId(@PathVariable String cartId){
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::deleteCartByCartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @PostMapping("/{customerId}/assign")
    public Mono<ResponseEntity<CartResponseModel>> assignCartToCustomer(
            @PathVariable String customerId,
            @RequestBody List<CartProduct> products) {
        return cartService.assignCartToCustomer(customerId, products)
                .map(cart -> ResponseEntity.status(HttpStatus.CREATED).body(cart))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseModel>> addProductToCart(@PathVariable String cartId, @RequestBody AddProductRequestModel requestModel) {
        return cartService.addProductToCart(cartId, requestModel.getProductId(), requestModel.getQuantity())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof OutOfStockException || e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else {
                        return Mono.error(e);
                    }
                });
    }

    @PutMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> updateProductQuantityInCart(@PathVariable String cartId, @PathVariable String productId, @RequestBody UpdateProductQuantityRequestModel requestModel) {
        return cartService.updateProductQuantityInCart(cartId, productId, requestModel.getQuantity())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof OutOfStockException || e instanceof InvalidInputException || e instanceof NotFoundException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else {
                        return Mono.error(e);
                    }
                });
    }

    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseModel>> checkoutCart(@PathVariable String cartId) {
        return cartService.checkoutCart(cartId)
                .map(cartResponse -> ResponseEntity.ok(cartResponse))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(ResponseEntity.badRequest().body(null)));
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> getCartByCustomerId(@PathVariable String customerId) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided customer id is invalid: " + customerId)))
                .flatMap(cartService::findCartByCustomerId)
                .map(ResponseEntity::ok);
    }


    @DeleteMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId){
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validId -> cartService.removeProductFromCart(validId, productId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
