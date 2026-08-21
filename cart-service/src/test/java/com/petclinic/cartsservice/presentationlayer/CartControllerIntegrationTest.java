package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.MockServerConfigProductService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import org.junit.jupiter.api.*;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"server.port=0", "spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    public static final String NON_EXISTING_CART_ID = "3ee10bc4-2957-42dc-8d2b-2ecb76301a3c";
    public static final String NON_EXISTING_PRODUCT_ID = "3ee10bc4-2957-42dc-8d2b-2ecb76301a3c";

    private Cart cart1;
    private Cart cart2;


    private static MockServerConfigProductService mockServerConfigProductService;

    CartProduct product1 = CartProduct.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a88")
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

    CartProduct wishListProduct1 = CartProduct.builder()
            .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
            .productName("Aquarium Filter")
            .productDescription("Filter system for small to medium-sized aquariums")
            .productSalePrice(19.99)
            .quantityInCart(1)
            .productQuantity(10)
            .averageRating(0.0)
            .build();

    CartProduct wishlistProduct2 = CartProduct.builder()
            .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
            .productName("Horse Saddle")
            .productDescription("Lightweight saddle for riding horses")
            .productSalePrice(199.99)
            .quantityInCart(1)
            .productQuantity(15)
            .averageRating(0.0)
            .build();

    List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));
    List<CartProduct> wishListProducts = new ArrayList<>(Arrays.asList(wishListProduct1, wishlistProduct2));


    @BeforeEach
    void initCarts() {
        product1.setQuantityInCart(1);
        product2.setQuantityInCart(1);

        cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .products(new ArrayList<>(Arrays.asList(product1, product2)))
                .wishListProducts(new ArrayList<>(Arrays.asList(wishListProduct1, wishlistProduct2)))
                .build();

        cart2 = Cart.builder()
                .cartId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .products(new ArrayList<>())
                .wishListProducts(new ArrayList<>(Arrays.asList(product1, product2)))
                .build();

        Publisher<Cart> initializeCartData = cartRepository.deleteAll()
                .thenMany(Flux.just(cart1, cart2))
                .flatMap(cartRepository::save);

        StepVerifier.create(initializeCartData)
                .expectNextCount(2)
                .verifyComplete();
    }


    @BeforeAll
    public void startServer(){
        mockServerConfigProductService = new MockServerConfigProductService(0);
        mockServerConfigProductService.registerGetProduct1ByProductIdEndpoint();
        mockServerConfigProductService.registerGetProduct_NonExisting_ByProductIdEndpoint();
        int mockPort = mockServerConfigProductService.getPort();
        System.setProperty("app.product-service.base-url", "http://localhost:" + mockPort);
    }

    @AfterAll
    public void stopServer(){
        mockServerConfigProductService.stopServer();
    }

    @Test
    public void testMoveProductFromWishListToCart_NonExistentProduct_ReturnsNotFound() {
        // Given
        String cartId = cart1.getCartId();
        String productId = NON_EXISTING_PRODUCT_ID;

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)  // Expect the error response model
                .consumeWith(response -> {
                    CartResponseModel errorResponse = response.getResponseBody();
                    assertNotNull(errorResponse);  // Ensure the response is not null
                });
    }

    @Test
    public void testMoveProductFromWishListToCart_NonExistentCart_ReturnsNotFound() {
        // Given
        String cartId = NON_EXISTING_CART_ID;
        String productId = wishListProduct1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }

    @Test
    public void testMoveProductFromCartToWishList_NonExistentProduct_ReturnsNotFound() {
        // Given
        String cartId = cart1.getCartId();
        String productId = NON_EXISTING_PRODUCT_ID;

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)  // Expect the error response model
                .consumeWith(response -> {
                    CartResponseModel errorResponse = response.getResponseBody();
                    assertNotNull(errorResponse);  // Ensure the response is not null
                });
    }

    @Test
    public void testMoveProductFromCartToWishList_NonExistentCart_ReturnsNotFound() {
        // Given
        String cartId = NON_EXISTING_CART_ID;
        String productId = product1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }
}