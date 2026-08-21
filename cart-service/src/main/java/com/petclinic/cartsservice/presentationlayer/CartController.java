package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartQueryCriteria;
import com.petclinic.cartsservice.businesslayer.CartService;
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

    @DeleteMapping("/{cartId}/products")
    public Mono<ResponseEntity<Void>> deleteAllProductsInCart(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36) // validate the cart id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
        .flatMap(validId -> cartService.deleteAllItemsInCart(validId)
            .thenReturn(ResponseEntity.noContent().build()));
    }


    @DeleteMapping("/{cartId}")
    public Mono<ResponseEntity<Void>> deleteCartByCartId(@PathVariable String cartId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(cartService::deleteCartByCartId)
        .map(r -> ResponseEntity.noContent().build());

    }

    @DeleteMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<Void>> removeProductFromCart(@PathVariable String cartId, @PathVariable String productId) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validId -> cartService.removeProductFromCart(validId, productId))
        .map(r -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public Mono<ResponseEntity<CartResponseModel>> createCart(@RequestBody CartRequestModel requestModel) {
        if (requestModel == null || requestModel.getCustomerId() == null || requestModel.getCustomerId().isBlank()) {
            return Mono.error(new InvalidInputException("customerId must be provided"));
        }

        final String normalizedCustomerId = requestModel.getCustomerId().trim();

        return cartService.assignCartToCustomer(normalizedCustomerId)
                .map(cart -> {
                    String cartId = cart.getCartId();
                    URI location = cartId != null && !cartId.isBlank()
                            ? URI.create(String.format("/api/v1/carts/%s", cartId))
                            : URI.create("/api/v1/carts");
                    return ResponseEntity.created(location).body(cart);
                });
    }

    @PostMapping("/{cartId}/products")
    public Mono<ResponseEntity<CartResponseModel>> addProductToCart(@PathVariable String cartId, @RequestBody CartItemRequestModel requestModel) {
        final String requestedProductId = (requestModel != null && requestModel.getProductId() != null)
                ? requestModel.getProductId().trim()
                : null;

        return cartService.addProductToCart(cartId, requestModel)
                .map(cartResponse -> {
                    if (requestedProductId != null && !requestedProductId.isBlank()) {
            return ResponseEntity.created(URI.create(String.format("/api/v1/carts/%s/products/%s", cartId, requestedProductId)))
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

    private Mono<ResponseEntity<CartResponseModel>> handleProductQuantityUpdate(String cartId, String productId, UpdateProductQuantityRequestModel requestModel) {
        if (requestModel == null) {
            return Mono.error(new InvalidInputException("Quantity must be provided."));
        }

        return cartService.updateProductQuantityInCart(cartId, productId, requestModel.getQuantity())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof OutOfStockException || e instanceof InvalidInputException) {
                        CartResponseModel errorResponse = new CartResponseModel();
                        errorResponse.setMessage(e.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else if (e instanceof NotFoundException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    } else {
                        return Mono.error(e);
                    }
                });
    }

    @PatchMapping("/{cartId}/products/{productId}")
    public Mono<ResponseEntity<CartResponseModel>> patchProductQuantityInCart(@PathVariable String cartId,
                                                                              @PathVariable String productId,
                                                                              @RequestBody UpdateProductQuantityRequestModel requestModel) {
        return handleProductQuantityUpdate(cartId, productId, requestModel);
    }

    @PostMapping("/{cartId}/checkout")
    public Mono<ResponseEntity<CartResponseModel>> checkoutCart(@PathVariable String cartId) {
        return cartService.checkoutCart(cartId)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(ResponseEntity.badRequest().body(null)));
    }

    @PostMapping("/{cartId}/wishlist")
    public Mono<ResponseEntity<CartResponseModel>> addProductToWishlist(
            @PathVariable String cartId,
            @RequestBody WishlistItemRequestModel requestModel) {
        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
                .flatMap(validCartId -> cartService.addProductToWishlist(validCartId, requestModel))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
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
                .map(r -> ResponseEntity.noContent().<CartResponseModel>build())
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
    //Create wishlist transfer resource
    @PostMapping(value = "/{cartId}/wishlist-transfers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> createWishlistTransfer(
            @PathVariable String cartId,
            @RequestBody(required = false) WishlistTransferRequestModel transferRequest) {
    List<String> productIds = transferRequest != null
        ? transferRequest.normalizedProductIds()
        : List.of();
    WishlistTransferDirection direction = transferRequest != null
        ? transferRequest.resolvedDirection()
        : WishlistTransferDirection.defaultDirection();

        return Mono.just(cartId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided cart id is invalid: " + cartId)))
        .flatMap(validId -> cartService.transferWishlist(validId, productIds, direction))
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
                .onErrorResume(Throwable.class, e -> {
                    CartResponseModel resp = new CartResponseModel();
                    resp.setMessage("Unexpected error");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp));
                });
    }

    @PutMapping("/{cartId}/promo")
    public Mono<ResponseEntity<CartResponseModel>> applyPromoToCart(
            @PathVariable String cartId,
            @RequestBody(required = false) CartPromoRequestModel promoRequest) {
        if (promoRequest == null || promoRequest.getPromoPercent() == null) {
            return Mono.error(new InvalidInputException("promoPercent must be provided"));
        }

        return cartService.applyPromoToCart(cartId, promoRequest.getPromoPercent())
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{cartId}/promo")
    public Mono<ResponseEntity<Void>> clearPromoFromCart(@PathVariable String cartId) {
        return cartService.applyPromoToCart(cartId, null)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
