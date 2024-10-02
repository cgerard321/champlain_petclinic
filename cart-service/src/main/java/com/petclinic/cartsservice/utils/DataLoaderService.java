package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
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

        CartProduct product1 = CartProduct.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
                .productName("Dog Food")
                .productDescription("Premium dry food for adult dogs")
                .productSalePrice(45.99)
                .quantityInCart(1)
                .build();

        CartProduct product2 = CartProduct.builder()
                .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .productName("Cat Litter")
                .productDescription("Clumping cat litter with odor control")
                .productSalePrice(12.99)
                .quantityInCart(2)
                .build();

        CartProduct product3 = CartProduct.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .quantityInCart(3)
                .build();

        CartProduct product4 = CartProduct.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .quantityInCart(4)
                .build();

        List<CartProduct> products1 = List.of(product1, product2);

        List<CartProduct> products2 = List.of(product3, product4);

        Cart cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .products(products1)
                .build();

        Cart cart2 = Cart.builder()
                .cartId("34f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .products(products2)
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
