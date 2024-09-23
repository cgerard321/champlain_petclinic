package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {

   Mono<CartResponseModel> getCartByCartId(String cartId);
    Flux<CartResponseModel> getAllCarts();
    Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
    Mono<CartResponseModel> createNewCart(CartRequestModel cartRequestModel);
    Mono<Integer> getCartItemCount(String cartId);
    // Combining both features: clearCart and updateCartByCartId
    Flux<ProductResponseModel> clearCart(String cartId);  // From feat/CART-CPC-1144_clear_cart_feature


}
