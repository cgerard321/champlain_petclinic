package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CartResponseModel> getAllCarts() {
        return cartService.getAllCarts();
    }

    @DeleteMapping("/{cartId}/clear")
    public Flux<CartResponseModel> clearCart(@PathVariable String cartId) {
        return Flux.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::clearCart);
    }


    @GetMapping("/{cartId}/count")
    public Mono<ResponseEntity<Map<String, Integer>>> getCartItemCount(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::getCartItemCount)
                .map(count -> ResponseEntity.ok(Collections.singletonMap("itemCount", count)));
    }

    @DeleteMapping("/{cartId}")
    public Mono<ResponseEntity<CartResponseModel>> deleteCartByCartId(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::deleteCartByCartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @DeleteMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validId -> cartService.removeProductFromCart(validId, productId))
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


    /**
     * Move product to wishlist.
     * Moves a specified product from the cart to the wishlist.
     */
    @PutMapping("/{cartId}/wishlist/{productId}/toWishList")
    public Mono<ResponseEntity<CartResponseModel>> moveProductFromCartToWishlist(@PathVariable String cartId, @PathVariable String productId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validCartId -> Mono.just(productId)
                        .filter(id -> id.length() == 36)
                        .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                        .flatMap(validProductId -> cartService.moveProductFromCartToWishlist(validCartId, validProductId))
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
                    } else {
                        return Mono.error(e); // Let other exceptions propagate
                    }
                });
    }

    /**
     * Move product from wishlist to cart.
     * Moves a specified product from the wishlist to the cart.
     */
    @PutMapping("/{cartId}/wishlist/{productId}/toCart")
    public Mono<ResponseEntity<CartResponseModel>> moveProductFromWishListToCart(@PathVariable String cartId, @PathVariable String productId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validCartId -> Mono.just(productId)
                        .filter(id -> id.length() == 36)
                        .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                        .flatMap(validProductId -> cartService.moveProductFromWishListToCart(validCartId, validProductId))
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
                    } else {

                        return Mono.error(e);
                    }
                });
    }

    @PostMapping("/{cartId}/products/{productId}/quantity/{quantity}")
    public Mono<ResponseEntity<CartResponseModel>> addProductToWishList(@PathVariable String cartId, @PathVariable String productId, @PathVariable int quantity) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validCartId -> Mono.just(productId)
                        .filter(id -> id.length() == 36)
                        .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                        .flatMap(validProductId -> cartService.addProductToWishList(validCartId, validProductId, quantity))
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
                    } else if (e instanceof OutOfStockException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                    }
                    return Mono.error(e);
                });
    }
    @DeleteMapping("/{cartId}/wishlist/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> removeProductFromWishlist(
            @PathVariable String cartId,
            @PathVariable String productId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validCartId -> Mono.just(productId)
                        .filter(id -> id.length() == 36)
                        .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                        .flatMap(validProductId -> cartService.removeProductFromWishlist(validCartId, validProductId))
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
                    } else {
                        return Mono.error(e);
                    }
                });
    }

    @PostMapping("/{cartId}/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> addProductToCartFromProducts(
            @PathVariable String cartId,
            @PathVariable String productId) {

        return cartService.addProductToCartFromProducts(cartId, productId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof OutOfStockException || e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        return Mono.error(e);
                    }
                });
    }
    //Move all wishlist items to cart
    @PostMapping(value = "/{cartId}/wishlist/moveAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> moveAllWishlistToCart(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validId -> cartService.moveAllWishlistToCart(validId))
                .map(ResponseEntity::ok)
                .onErrorResume(InvalidInputException.class, e -> {
                    CartResponseModel resp = new CartResponseModel();
                    resp.setMessage(e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resp));
                })
                .onErrorResume(NotFoundException.class, e -> {
                    CartResponseModel resp = new CartResponseModel();
                    resp.setMessage(e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp));
                })
                .onErrorResume(OutOfStockException.class, e -> {
                    CartResponseModel resp = new CartResponseModel();
                    resp.setMessage(e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp));
                })
                .onErrorResume(Throwable.class, e -> {
                    CartResponseModel resp = new CartResponseModel();
                    resp.setMessage("Unexpected error");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp));
                });
    }


    @GetMapping("/{cartId}/recent-purchases")
    public Mono<ResponseEntity<List<CartProduct>>> getRecentPurchases(@PathVariable String cartId) {
        return cartService.getRecentPurchases(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{cartId}/recommendation-purchases")
    public Mono<ResponseEntity<List<CartProduct>>> getRecommendationPurchases(@PathVariable String cartId) {
        return cartService.getRecommendationPurchases(cartId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PutMapping("/{cartId}/promo")
    public Mono<CartResponseModel> applyPromoToCart(
            @PathVariable String cartId,
            @RequestParam("promoPercent") Double promoPercent) {
        return cartService.applyPromoToCart(cartId, promoPercent);
    }

}
