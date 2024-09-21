package com.petclinic.cartsservice.businesslayer;


import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {

    public Mono<CartResponseModel> getCartByCartId(String cartId);
    Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
    Mono<Integer> getCartItemCount(String cartId);


}
