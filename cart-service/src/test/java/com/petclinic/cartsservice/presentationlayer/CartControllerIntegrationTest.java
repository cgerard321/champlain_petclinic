package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.MockServerConfigProductService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
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


import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port = 0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    public static final String NON_EXISTING_CART_ID = "3ee10bc4-2957-42dc-8d2b-2ecb76301a3c";

    private MockServerConfigProductService mockServerConfigProductService;

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

    CartProduct wishListProduct1 = CartProduct.builder()
            .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
            .productName("Aquarium Filter")
            .productDescription("Filter system for small to medium-sized aquariums")
            .productSalePrice(19.99)
            .quantityInCart(1)
            .averageRating(0.0)
            .build();

    CartProduct wishlistProduct2 = CartProduct.builder()
            .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
            .productName("Horse Saddle")
            .productDescription("Lightweight saddle for riding horses")
            .productSalePrice(199.99)
            .quantityInCart(1)
            .averageRating(0.0)
            .build();


    List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));
    List<CartProduct> wishListProducts = new ArrayList<>(Arrays.asList(wishListProduct1, wishlistProduct2));


    Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
            .products(products)
            .wishListProducts(wishListProducts)
            .build();

    Cart cart2 = Cart.builder()
            .cartId("34f7b33a-d62a-420a-a84a-05a27c85fc91")
            .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
            .products(products)
            .wishListProducts(wishListProducts)
            .build();


    CartResponseModel cartResponseModel = CartResponseModel.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .products(products)
            .customerId("1")
            .build();

    CartRequestModel cartRequestModel = CartRequestModel.builder()
            .customerId("1")
            .build();

    @BeforeAll
    public void startServer(){
        mockServerConfigProductService = new MockServerConfigProductService();
        mockServerConfigProductService.registerGetProduct1ByProductIdEndpoint();
    }

    @AfterAll
    public void stopServer(){
        mockServerConfigProductService.stopServer();
    }

    @BeforeEach
    public void setup() {
        Publisher<Cart> initializeCartData = cartRepository.deleteAll()
                .thenMany(Flux.just(cart1))
                .flatMap(cartRepository::save);

        StepVerifier.create(initializeCartData)
                .expectNextCount(1)
                .verifyComplete();
    }


//    @Test
//    public void testGetAllCarts_CartsExist_ReturnsCarts() {
//        // Given: Add a few carts to the database
//        Publisher<Cart> initializeCartData = cartRepository.deleteAll()
//                .thenMany(Flux.just(cart1, cart2))
//                .flatMap(cartRepository::save);
//
//        StepVerifier.create(initializeCartData)
//                .expectNextCount(2)
//                .verifyComplete();
//
//        // When: Send a request to get all carts
//        webTestClient.get()
//                .uri("/api/v1/carts")
//                .exchange()
//                // Then: Verify the response status and body
//                .expectStatus().isOk()
//                .expectBodyList(CartResponseModel.class)
//                .consumeWith(response -> {
//                    List<CartResponseModel> carts = response.getResponseBody();
//                    assertNotNull(carts);
//                    assertEquals(2, carts.size());
//                });
//    }

//    @Test
//    public void testGetCartById_ValidCartId_ReturnsCart() {
//        // When: Send a request to get the cart by its ID
//        webTestClient.get()
//                .uri("/api/v1/carts/{cartId}", cart1.getCartId())
//                .exchange()
//                // Then: Verify the response status and body
//                .expectStatus().isOk()
//                .expectBody(CartResponseModel.class)
//                .consumeWith(response -> {
//                    CartResponseModel retrievedCart = response.getResponseBody();
//                    assertNotNull(retrievedCart);
//                    assertEquals(cart1.getCartId(), retrievedCart.getCartId());
//                    assertEquals(cart1.getProducts().size(), retrievedCart.getProducts().size());
//                    // Optionally check details of the products
//                    assertEquals(cart1.getProducts().get(0).getProductId(), retrievedCart.getProducts().get(0).getProductId());
//                });
//    }


//    @Test
//    public void testMoveProductFromWishListToCart_ValidProduct_MovesProduct() {
//        // Given
//        String cartId = cart1.getCartId(); // Assumes cart1 is a pre-defined cart object
//        String productId = wishListProduct1.getProductId();
//
//        // When
//        webTestClient.put()
//                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                // Then
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(CartResponseModel.class)
//                .consumeWith(response -> {
//                    CartResponseModel cartResponse = response.getResponseBody();
//                    assertNotNull(cartResponse);
//                    // Verify the product has been added to the cart
//                    assertTrue(cartResponse.getProducts().stream()
//                            .anyMatch(product -> product.getProductId().equals(productId)));
//                    // Optionally, you could also verify the size of the cart products if needed
//                    assertEquals(cart1.getProducts().size() + 1, cartResponse.getProducts().size());
//                });
//    }


}