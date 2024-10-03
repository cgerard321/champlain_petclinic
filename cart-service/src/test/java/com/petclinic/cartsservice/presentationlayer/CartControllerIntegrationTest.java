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

    private final CartProduct product1 = CartProduct.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Product1")
            .productDescription("Description1")
            .productSalePrice(100.0)
            .quantityInCart(1)
            .averageRating(4.5)
            .build();

    private final CartProduct product2 = CartProduct.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .productName("Product2")
            .productDescription("Description2")
            .productSalePrice(200.0)
            .quantityInCart(1)
            .averageRating(4.0)
            .build();

    private final CartProduct product3 = CartProduct.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .productName("Product3")
            .productDescription("Description3")
            .productSalePrice(300.0)
            .quantityInCart(1)
            .averageRating(3.5)
            .build();

    private final List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));

    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .products(products)
            .customerId("1")
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
    public void setup(){
        Publisher<Cart> setupDB = cartRepository.deleteAll()
                .thenMany(Flux.just(cart1))
                .flatMap(cartRepository::save);

        StepVerifier.create(setupDB).expectNextCount(1).verifyComplete();
    }

//    @Test
//    void whenUpdateByCartId_thenReturnCartResponseModel(){
//        webTestClient.put()
//                .uri("/api/v2/carts/" + cart1.getCartId())
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(cartRequestModel)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(CartResponseModel.class)
//                .value(updated -> {
//                    assertEquals(cartRequestModel.getCustomerId(), updated.getCustomerId());
//                });
//    }

  /*@Test
    void whenGetCartByCartId_thenReturnCartResponseModel(){
        webTestClient.get()
                .uri("/api/v1/carts/" + cart1.getCartId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertNotNull(cartResponseModel);
                    assertEquals(cart1.getCustomerId(), result.getCustomerId());
                    assertEquals(cart1.getProducts().size(), result.getProducts().size());
                    assertEquals(cart1.getProducts().get(0).getProductId(), result.getProducts().get(0).getProductId());
                    assertEquals(cart1.getProducts().get(0).getProductName(), result.getProducts().get(0).getProductName());
                    assertEquals(cart1.getProducts().get(0).getProductDescription(), result.getProducts().get(0).getProductDescription());
                    assertEquals(cart1.getProducts().get(0).getProductSalePrice(), result.getProducts().get(0).getProductSalePrice());
                    assertEquals(cart1.getProducts().get(0).getQuantityInCart(), result.getProducts().get(0).getQuantityInCart());
                    assertEquals(cart1.getProducts().get(0).getAverageRating(), result.getProducts().get(0).getAverageRating());
                });
    }
*/


}