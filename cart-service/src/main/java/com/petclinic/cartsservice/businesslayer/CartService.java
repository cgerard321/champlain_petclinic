package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CartService {


    Mono<CartResponseModel> getCartByCartId(String cartId);
    Flux<CartResponseModel> getAllCarts();
    Mono<Void> deleteAllItemsInCart(String cartId);

    Mono<CartResponseModel> deleteCartByCartId(String cartId);
    Mono<CartResponseModel> removeProductFromCart(String cartId, String productId);
    Mono<CartResponseModel> addProductToCart(String cartId, String productId, int quantity);
    Mono<CartResponseModel> updateProductQuantityInCart(String cartId, String productId, int quantity);

    Mono<CartResponseModel> checkoutCart(String cartId);

    Mono<CartResponseModel> assignCartToCustomer(String customerId, List<CartProduct> products);

    Mono<CartResponseModel>  findCartByCustomerId(String customerId);

    //move product between cart and wishlist
    Mono<CartResponseModel> moveProductFromCartToWishlist(String cartId, String productId);
    Mono<CartResponseModel> moveProductFromWishListToCart(String cartId, String productId);

    Mono<CartResponseModel> addProductToWishList(String cartId, String productId, int quantity);
    Mono<CartResponseModel> addProductToCartFromProducts(String cartId, String productId);
    // add this new method to the interface
    Mono<CartResponseModel> removeProductFromWishlist(String cartId, String productId);

    Mono<CartResponseModel> moveAllWishlistToCart(String cartId);

    Mono<List<CartProduct>> getRecentPurchases(String cartId);

    Mono<List<CartProduct>> getRecommendationPurchases(String cartId);

    Mono<CartResponseModel> applyPromoToCart(String cartId, Double promoPercent);

}

