package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {


    public Mono<CartResponseModel> getCartByCartId(String cartId);
    public Flux<CartResponseModel> getAllCarts();
//    Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
    // Combining both features: clearCart and updateCartByCartId
    Flux<CartResponseModel> clearCart(String cartId);  // From feat/CART-CPC-1144_clear_cart_feature

   // Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
    Mono<CartResponseModel> createNewCart(CartRequestModel cartRequestModel);
    Mono<Integer> getCartItemCount(String cartId);
    // Combining both features: clearCart and updateCartByCartId
    Mono<CartResponseModel> deleteCartByCartId(String cartId);

    //feature for the chekout cart
    Mono<CartResponseModel> checkoutCart(String cartId);



}
