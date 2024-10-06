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

    private final String NON_EXISTENT_PRODUCT_ID = UUID.randomUUID().toString();
    private final String INVALID_PRODUCT_ID = "INVALID_PRODUCT_ID";

    private Product product1 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.00)
            .averageRating(0.0)
            .productQuantity(2)
            .build();

    private Product product2 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 2")
            .productDescription("Product 2 Description")
            .productSalePrice(50.00)
            .averageRating(0.0)
            .productQuantity(2)
            .build();

    private ProductRequestModel productRequestModel = ProductRequestModel.builder()
            .productName("Product 3")
            .productDescription("Product 3 Description")
            .productSalePrice(25.00)
            .averageRating(0.0)
            .productQuantity(2)
            .build();

    private ProductRequestModel productRequestModelWithInavlidSalePrice = ProductRequestModel.builder()
            .productName("Product 3")
            .productDescription("Product 3 Description")
            .productSalePrice(0.00)
            .averageRating(0.0)
            .productQuantity(2)

            .build();

    private ProductRequestModel productRequestModel2 = ProductRequestModel.builder()
            .productName("Product 4")
            .productDescription("Product 4 Description")
            .productSalePrice(25.00)
            .averageRating(0.0)
            .productQuantity(2)
            .build();

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
    public void whenGetAllProducts_thenReturnAllProducts() {
        StepVerifier.create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();

        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(2, productResponseModel.size());
                });

    }

    @Test
    public void whenNoProductsExist_thenReturnEmptyList() {

        StepVerifier.create(productRepository.deleteAll())
                .verifyComplete();


        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(0, productResponseModel.size());
                });
    }

    @Test
    public void whenAddProduct_thenReturnProduct() {
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
                    assertNotNull(productResponseModel);
                    assertNotNull(productResponseModel.getProductId());
                    assertEquals(productRequestModel.getProductName(), productResponseModel.getProductName());
                    assertEquals(productRequestModel.getProductDescription(), productResponseModel.getProductDescription());
                    assertEquals(productRequestModel.getProductSalePrice(), productResponseModel.getProductSalePrice());
                    assertEquals(productRequestModel.getAverageRating(), productResponseModel.getAverageRating());
                });

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void whenAddProductWithInvalidSalePrice_thenThrowInvalidAmountException() {
        webTestClient
                .post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(productRequestModelWithInavlidSalePrice), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product sale price must be greater than 0");

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenUpdateProduct_thenReturnUpdatedProduct() {
        webTestClient
                .put()
                .uri("/api/v1/products/" +  product1.getProductId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(productRequestModel), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertEquals(product1.getProductId(), productResponseModel.getProductId());
                    assertEquals(productRequestModel.getProductName(), productResponseModel.getProductName());
                    assertEquals(productRequestModel.getProductDescription(), productResponseModel.getProductDescription());
                    assertEquals(productRequestModel.getProductSalePrice(), productResponseModel.getProductSalePrice());
                    assertEquals(productRequestModel.getAverageRating(), productResponseModel.getAverageRating());
                });

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenUpdateProductWithNonExistentProductId_thenThrowNotFoundException() {
        webTestClient
                .put()
                .uri("/api/v1/products/" + NON_EXISTENT_PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(productRequestModel2), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product id was not found: " +
                        NON_EXISTENT_PRODUCT_ID);

        StepVerifier
                .create(productRepository.findAll())
                .expectNext(product1)
                .expectNext(product2);
    }

    @Test
    public void whenUpdateWithInvalidProductId_thenThrowInvalidInputException() {
        webTestClient
                .put()
                .uri("/api/v1/products/" + INVALID_PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(productRequestModel2), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " +
                        INVALID_PRODUCT_ID);

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenDeleteProduct_thenDeleteProduct() {
        webTestClient
                .delete()
                .uri("/api/v1/products/" + product1.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertEquals(product1.getProductId(), productResponseModel.getProductId());
                    assertEquals(product1.getProductName(), productResponseModel.getProductName());
                    assertEquals(product1.getProductDescription(), productResponseModel.getProductDescription());
                    assertEquals(product1.getProductSalePrice(), productResponseModel.getProductSalePrice());
                    assertEquals(product1.getAverageRating(), productResponseModel.getAverageRating());
                });

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void whenDeleteProductWithNonExistentProductId_thenThrowNotFoundException() {
        webTestClient
                .delete()
                .uri("/api/v1/products/" + NON_EXISTENT_PRODUCT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product id was not found: " +
                        NON_EXISTENT_PRODUCT_ID);

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenDeleteWithInvalidProductId_thenThrowInvalidInputException() {
        webTestClient
                .delete()
                .uri("/api/v1/products/" + INVALID_PRODUCT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " +
                        INVALID_PRODUCT_ID);

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenGetAllProductsWithMinPrice_thenReturnFilteredProducts() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/products")
                        .queryParam("minPrice", 60.00)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(1, productResponseModel.size());
                    assertEquals(product1.getProductId(), productResponseModel.get(0).getProductId());
                });
    }

    @Test
    public void whenGetAllProductsWithMaxPrice_thenReturnFilteredProducts() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/products")
                        .queryParam("maxPrice", 60.00)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(1, productResponseModel.size());
                    assertEquals(product2.getProductId(), productResponseModel.get(0).getProductId());
                });
    }

    @Test
    public void whenGetAllProductsWithMinAndMaxPrice_thenReturnFilteredProducts() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/products")
                        .queryParam("minPrice", 40.00)
                        .queryParam("maxPrice", 60.00)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(1, productResponseModel.size());
                    assertEquals(product2.getProductId(), productResponseModel.get(0).getProductId());
                });
    }

    @Test
    public void whenGetAllProductsWithNoMatchingPrice_thenReturnEmptyList() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/products")
                        .queryParam("minPrice", 200.00)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(0, productResponseModel.size());
                });
    }

    @Test
    public void testChangeProductQuantity() {
        String productId = "4affcab7-3ab1-4917-a114-2b6301aa5565";
        ProductRequestModel requestModel = new ProductRequestModel();
        requestModel.setProductQuantity(5);

        webTestClient.patch()
                .uri("/api/v1/products/"+product1.getProductId()+ "/quantity")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestModel), ProductRequestModel.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testDecreaseProductQuantity_NotFound() {
        String nonExistentProductId = "nonExistentProductId";

        webTestClient.patch()
                .uri("/products/{productId}", nonExistentProductId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testChangeProductQuantity_NotFound() {
        String nonExistentProductId = "nonExistentProductId";
        ProductRequestModel requestModel = new ProductRequestModel();
        requestModel.setProductQuantity(5);

        webTestClient.patch()
                .uri("/products/{productId}/quantity", nonExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestModel), ProductRequestModel.class)
                .exchange()
                .expectStatus().isNotFound();
    }
}