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

        List<String> productIds = List.of("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", "d819e4f4-25af-4d33-91e9-2c45f0071606");

        Cart cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .productIds(productIds)
                .customerId("1")
                .build();


        Flux.just(cart1)
                .flatMap(s -> cartRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();
    }
}
