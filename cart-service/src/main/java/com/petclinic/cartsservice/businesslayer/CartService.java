package com.petclinic.cartsservice.businesslayer;


import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {

    public Mono<CartResponseModel> getCartByCartId(String cartId);

    public Flux<CartResponseModel> getAllCarts();
}
