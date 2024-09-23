package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DataLoaderService implements CommandLineRunner {

    @Autowired
    CartRepository cartRepository;

    @Override
    public void run(String... args) throws Exception {

        List<String> productIds = List.of("1501f30e-1db1-44b2-a555-bca6f64450e4", "baee7cd2-b67a-449f-b262-91f45dde8a6d");

        Cart cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .productIds(productIds)
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .build();

        Cart cart2 = Cart.builder()
                .cartId("34f7b33a-d62a-420a-a84a-05a27c85fc91")
                .productIds(productIds)
                .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .build();


        Flux.just(cart1)
                .flatMap(s -> cartRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();

        Flux.just(cart2)
                .flatMap(s -> cartRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();
    }
}
