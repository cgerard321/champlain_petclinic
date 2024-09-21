package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import org.junit.jupiter.api.*;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    private Product product1 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.00)
            .averageRating(0.0)
            .build();

    private Product product2 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 2")
            .productDescription("Product 2 Description")
            .productSalePrice(50.00)
            .averageRating(0.0)
            .build();

    private ProductRequestModel productRequestModel = ProductRequestModel.builder()
            .productName("Product 3")
            .productDescription("Product 3 Description")
            .productSalePrice(25.00)
            .averageRating(0.0)
            .build();

//    private ProductRequestModel product_withNonExistentEnrollemntId_RequestModel = ProductRequestModel.builder()
//            .productId(UUID.randomUUID().toString())
//            .productName("Product 1")
//            .productDescription("Product 1 Description")
//            .productSalePrice(100.00)
//            .averageRating(0.0)
//            .build();

//    @BeforeAll
//    public startServers() {}
//
//    @AfterAll
//    public stopServers() {}

    @BeforeEach
    public void setupDB() {
        Publisher<Product> setupDB = productRepository.deleteAll()
                .thenMany(Flux.just(product1, product2)
                        .flatMap(productRepository::save));

        StepVerifier
                .create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenAddProduct_thenAddProductResponseModel() {
        webTestClient
                .post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(productRequestModel), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel.getProductId());
                    assertEquals(productRequestModel.getProductName(), productResponseModel.getProductName());
                    assertEquals(productRequestModel.getProductDescription(), productResponseModel.getProductDescription());
                    assertEquals(productRequestModel.getProductSalePrice(), productResponseModel.getProductSalePrice());
                    assertEquals(productRequestModel.getAverageRating(), productResponseModel.getAverageRating());
                });

        // assert
        StepVerifier
                .create(productRepository.count())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void whenAdd

}