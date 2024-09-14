package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.MockServerConfigProductService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
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

    public static final String NON_EXISTING_CART_ID = "06a7d573-bcab-4db3-956f-773324b92a89";

    private MockServerConfigProductService mockServerConfigProductService;

    List<String> productIds = List.of("06a7d573-bcab-4db3-956f-773324b92a88");

    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productIds(productIds)
            .customerId("1")
            .build();

    ProductResponseModel productResponseModel = ProductResponseModel.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a88")
            .productName("Dog Food")
            .productDescription("Dog Food")
            .productSalePrice(10.0)
            .build();

    CartResponseModel cartResponseModel = CartResponseModel.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .products(List.of(productResponseModel))
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
    void whenGetCartByCartId_thenReturnCartResponseModel(){
        webTestClient.get()
                .uri("/api/v1/carts/" + cart1.getCartId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertNotNull(cartResponseModel);
                    assertEquals(cart1.getCartId(), result.getCartId());
                    assertEquals(cart1.getCustomerId(), result.getCustomerId());
                    assertEquals(cart1.getProductIds().size(), result.getProducts().size());
                    assertEquals(cart1.getProductIds().get(0), result.getProducts().get(0).getProductId());
                });
    }

}