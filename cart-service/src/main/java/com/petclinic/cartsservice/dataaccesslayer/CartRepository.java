package com.petclinic.cartsservice.dataaccesslayer;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CartRepository extends ReactiveMongoRepository<Cart, String> {
    public Mono<Cart> findCartByCartId(String cartId);
    public Mono<Cart>  findCartByCustomerId(String customerId);

}
