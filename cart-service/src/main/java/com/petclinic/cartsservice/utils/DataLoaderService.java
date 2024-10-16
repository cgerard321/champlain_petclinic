package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
                .productQuantity(5)
                .build();

        CartProduct product2 = CartProduct.builder()
                .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .productName("Cat Litter")
                .productDescription("Clumping cat litter with odor control")
                .productSalePrice(12.99)
                .quantityInCart(1)
                .productQuantity(8)
                .build();

        CartProduct product3 = CartProduct.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .quantityInCart(1)
                .productQuantity(10)
                .build();

        CartProduct product4 = CartProduct.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .quantityInCart(1)
                .productQuantity(9)
                .build();

        CartProduct wishListProduct1 = CartProduct.builder()
                .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
                .productName("Aquarium Filter")
                .productDescription("Filter system for small to medium-sized aquariums")
                .productSalePrice(19.99)
                .quantityInCart(1)
                .productQuantity(9)
                .averageRating(0.0)
                .build();

        CartProduct wishlistProduct2 = CartProduct.builder()
                .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
                .productName("Horse Saddle")
                .productDescription("Lightweight saddle for riding horses")
                .productSalePrice(199.99)
                .quantityInCart(1)
                .productQuantity(0)
                .averageRating(0.0)
                .build();

        List<CartProduct> products1 = new ArrayList<>(List.of(product1, product2));
        List<CartProduct> products2 = new ArrayList<>(List.of(product3, product4));
        List<CartProduct> wishListProducts = new ArrayList<>(List.of(wishListProduct1, wishlistProduct2));

        Cart cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .products(products1)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart2 = Cart.builder()
                .cartId("34f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        // Insert both carts using a single Flux and log success or error
        Flux.just(cart1, cart2)
                .flatMap(cartRepository::insert)
                .doOnNext(savedCart -> System.out.println("Inserted cart: " + savedCart.getCartId()))
                .doOnError(error -> System.err.println("Error inserting cart: " + error.getMessage()))
                .subscribe();
    }
}
