package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartQueryCriteria;
import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CartItemRequestModel;
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

import java.net.URI;
import java.util.List;

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
    public Mono<ResponseEntity<List<CartResponseModel>>> getAllCartsAsList(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "assigned", required = false) Boolean assigned
    ) {
        CartQueryCriteria criteria = buildCriteria(page, size, customerId, customerName, assigned);
        return cartService.getAllCarts(criteria)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CartResponseModel> getAllCartsStream(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "assigned", required = false) Boolean assigned
    ) {
        CartQueryCriteria criteria = buildCriteria(page, size, customerId, customerName, assigned);
        return cartService.getAllCarts(criteria);
    }

    private CartQueryCriteria buildCriteria(Integer page, Integer size, String customerId, String customerName, Boolean assigned) {
        if (page != null && page < 0) {
            throw new InvalidInputException("page must be greater than or equal to 0");
        }
        if (size != null && size <= 0) {
            throw new InvalidInputException("size must be greater than 0");
        }

        return CartQueryCriteria.builder()
                .page(page)
                .size(size)
                .customerId(customerId)
                .customerName(customerName)
                .assigned(assigned)
                .build();
    }

    @DeleteMapping("/{cartId}/items")
    public Mono<ResponseEntity<Void>> deleteAllItemsInCart(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validId -> cartService.deleteAllItemsInCart(validId)
                        .thenReturn(ResponseEntity.noContent().build()));
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

    @DeleteMapping("/{cartId}/products/{productId}")
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
    public Mono<ResponseEntity<CartResponseModel>> addProductToCart(@PathVariable String cartId, @RequestBody CartItemRequestModel requestModel) {
        final String requestedProductId = (requestModel != null && requestModel.getProductId() != null)
                ? requestModel.getProductId().trim()
                : null;

        return cartService.addProductToCart(cartId, requestModel)
                .map(cartResponse -> {
                    if (requestedProductId != null && !requestedProductId.isBlank()) {
                        return ResponseEntity.created(URI.create(String.format("/api/v1/carts/%s/items/%s", cartId, requestedProductId)))
                                .body(cartResponse);
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).body(cartResponse);
                })
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
