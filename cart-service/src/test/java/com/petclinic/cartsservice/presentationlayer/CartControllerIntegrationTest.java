package com.petclinic.cartsservice.presentationlayer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerIntegrationTest {
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.5")
            .withExposedPorts(27017);

    static {
        mongoDBContainer.start();
    }

    private static WireMockServer wireMockServer;
    
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "test-carts");
    }

    @BeforeAll
    static void setUp() {
        // Start WireMock server
        wireMockServer = new WireMockServer(0); // Use any free port
        wireMockServer.start();
        
        // Configure WireMock stubs for external services
        configureFor("localhost", wireMockServer.port());
        
        // Stub for auth service
        stubFor(get(urlPathMatching("/api/v1/auth/validate-token.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"valid\":true,\"userId\":\"test-user-id\"}")));
                        
        // Stub for product service
        stubFor(get(urlPathMatching("/api/v1/products/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"product-1\",\"name\":\"Test Product\",\"price\":9.99}")));
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setup() {
        // First, clear all existing data
        cartRepository.deleteAll().block();

        // Initialize products and carts
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

        // Save the newly created carts to the database
        cartRepository.saveAll(Arrays.asList(cart1, cart2)).blockLast();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public WireMockServer wireMockServer() {
            return wireMockServer;
        }
    }

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
    public void testMoveProductFromWishListToCart_ValidProduct_MovesProduct() {
        // Given
        String cartId = cart1.getCartId();
        String productId = wishListProduct1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .consumeWith(response -> {
                    CartResponseModel cartResponse = response.getResponseBody();
                    assertNotNull(cartResponse);
                    // Verify the product has been added to the cart
                    assertTrue(cartResponse.getProducts().stream()
                            .anyMatch(product -> product.getProductId().equals(productId)));
                    // Verify the product is no longer in the wishlist
                    assertFalse(cartResponse.getWishListProducts().stream()
                            .anyMatch(product -> product.getProductId().equals(productId)));
                    // Verify the size of the cart products
                    assertEquals(cart1.getProducts().size() + 1, cartResponse.getProducts().size());
                });
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
    public void testMoveProductFromWishListToCart_withInvalidCartId_thenReturnsUnprocessableEntity() {
        // Given
        String cartId = "invalidCartId";
        String productId = wishListProduct1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isEqualTo(422)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }

    @Test
    public void testMoveProductFromWishListToCart_withInvalidProductId_thenReturnsUnprocessableEntity(){
        // Given
        String cartId = cart1.getCartId();
        String productId = "invalidProductId";

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isEqualTo(422)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }
    @Test
    public void testMoveProductFromCartToWishList_ValidProduct_MovesProduct() {
        // Given
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .consumeWith(response -> {
                    CartResponseModel cartResponse = response.getResponseBody();
                    assertNotNull(cartResponse);
                    // Verify the product has been added to the wishlist
                    assertTrue(cartResponse.getWishListProducts().stream()
                            .anyMatch(product -> product.getProductId().equals(productId)));
                    // Verify the product is no longer in the cart
                    assertFalse(cartResponse.getProducts().stream()
                            .anyMatch(product -> product.getProductId().equals(productId)));
                    // Verify the size of the wishlist products
                    assertEquals(cart1.getWishListProducts().size() + 1, cartResponse.getWishListProducts().size());
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

    @Test
    public void testMoveProductFromCartToWishList_withInvalidCartId_thenReturnsUnprocessableEntity() {
        // Given
        String cartId = "invalidCartId";
        String productId = product1.getProductId();

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isEqualTo(422)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }

    @Test
    public void testMoveProductFromCartToWishList_withInvalidProductId_thenReturnsUnprocessableEntity() {
        // Given
        String cartId = cart1.getCartId();
        String productId = "invalidProductId";

        // When
        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus().isEqualTo(422)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String errorMessage = response.getResponseBody();
                    assertNotNull(errorMessage);
                });
    }

}