package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CartItemRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.WishlistItemRequestModel;
import com.petclinic.cartsservice.presentationlayer.WishlistTransferDirection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CartService {

    Mono<CartResponseModel> getCartByCartId(String cartId);
    Flux<CartResponseModel> getAllCarts(CartQueryCriteria criteria);
    Mono<Void> deleteAllItemsInCart(String cartId);

    Mono<CartResponseModel> deleteCartByCartId(String cartId);
    Mono<CartResponseModel> removeProductFromCart(String cartId, String productId);
    Mono<CartResponseModel> addProductToCart(String cartId, CartItemRequestModel cartItemRequestModel);
    Mono<CartResponseModel> updateProductQuantityInCart(String cartId, String productId, int quantity);

    Mono<CartResponseModel> checkoutCart(String cartId);

    Mono<CartResponseModel> assignCartToCustomer(String customerId);

    Mono<CartResponseModel>  findCartByCustomerId(String customerId);

    Mono<CartResponseModel> addProductToWishlist(String cartId, WishlistItemRequestModel requestModel);
    // add this new method to the interface
    Mono<CartResponseModel> removeProductFromWishlist(String cartId, String productId);

    Mono<CartResponseModel> transferWishlist(String cartId, List<String> productIds, WishlistTransferDirection direction);

    Mono<List<CartProduct>> getRecentPurchasesByCustomerId(String customerId);

    Mono<List<CartProduct>> getRecommendationPurchasesByCustomerId(String customerId);

    Mono<CartResponseModel> applyPromoToCart(String cartId, Double promoPercent);

}

