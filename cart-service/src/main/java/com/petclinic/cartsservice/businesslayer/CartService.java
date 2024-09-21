package com.petclinic.cartsservice.businesslayer;


import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {



    Mono<CartResponseModel> updateCartByCartId(Mono<CartRequestModel> cartRequestModel, String cartId);
}

}
