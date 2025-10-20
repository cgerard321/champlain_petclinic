package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("local")
@Slf4j
@RequiredArgsConstructor
public class DataLoaderService implements CommandLineRunner {

    private final CartRepository cartRepository;

    @Override
    public void run(String... args) {
        CartProduct product1 = CartProduct.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
                .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
                .productName("Dog Food")
                .productDescription("Premium dry food for adult dogs")
                .productSalePrice(45.99)
                .quantityInCart(1)
                .productQuantity(5)
                .build();

        CartProduct product2 = CartProduct.builder()
                .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
                .productName("Cat Litter")
                .productDescription("Clumping cat litter with odor control")
                .productSalePrice(12.99)
                .quantityInCart(1)
                .productQuantity(8)
                .build();

        CartProduct product3 = CartProduct.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .quantityInCart(1)
                .productQuantity(10)
                .build();

        CartProduct product4 = CartProduct.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .imageId("7074e0ef-d041-452f-8a0f-cb9ab20d1fed")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .quantityInCart(1)
                .productQuantity(9)
                .build();

        CartProduct wishListProduct1 = CartProduct.builder()
                .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
                .imageId("392c42d9-9505-4c27-b82e-20351b25d33f")
                .productName("Aquarium Filter")
                .productDescription("Filter system for small to medium-sized aquariums")
                .productSalePrice(19.99)
                .quantityInCart(1)
                .productQuantity(9)
                .averageRating(0.0)
                .build();

        CartProduct wishlistProduct2 = CartProduct.builder()
                .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
                .imageId("664aa14b-db66-4b25-9d05-f3a9164eb401")
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
                .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart3 = Cart.builder()
                .cartId("14f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("3f59dca2-903e-495c-90c3-7f4d01f3a2aa")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart4 = Cart.builder()
                .cartId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .customerId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .products(products1)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart5 = Cart.builder()
                .cartId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart6 = Cart.builder()
                .cartId("b3d09eab-4085-4b2d-a121-78a0a2f9e501")
                .customerId("b3d09eab-4085-4b2d-a121-78a0a2f9e501")
                .products(products1)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart7 = Cart.builder()
                .cartId("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd")
                .customerId("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart8 = Cart.builder()
                .cartId("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7")
                .customerId("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7")
                .products(products1)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart9 = Cart.builder()
                .cartId("9f6accd1-e943-4322-932e-199d93824317")
                .customerId("9f6accd1-e943-4322-932e-199d93824317")
                .products(products2)
                .wishListProducts(wishListProducts)
                .build();

        Cart cart10 = Cart.builder()
                .cartId("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f")
                .customerId("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f")
                .products(products1)
                .wishListProducts(wishListProducts)
                .build();

        Flux<Cart> demoCarts = Flux.just(cart1, cart2, cart3, cart4, cart5, cart6, cart7, cart8, cart9, cart10);

        cartRepository.findAll().hasElements()
                .flatMapMany(has -> has ? Flux.empty() : demoCarts.flatMap(cartRepository::insert))
                .then()
                .doOnSuccess(v -> log.info("Demo carts loaded (if collection was empty)"))
                .doOnError(e -> log.error("Failed to load demo carts", e))
                .subscribe();
    }
}
