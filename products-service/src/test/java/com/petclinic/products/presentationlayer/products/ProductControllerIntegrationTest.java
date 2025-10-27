package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Autowired
    private RatingRepository ratingRepository;

    private final String NON_EXISTENT_PRODUCT_ID = UUID.randomUUID().toString();
    private final String INVALID_PRODUCT_ID = "INVALID_PRODUCT_ID";

    private Product product1 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.00)
            .averageRating(4.5)
            .productQuantity(2)
            .build();

    private Product product2 = Product.builder()
            .productId(UUID.randomUUID().toString())
            .productName("Product 2")
            .productDescription("Product 2 Description")
            .productSalePrice(50.00)
            .averageRating(3.0)
            .productQuantity(2)
            .build();

    private ProductRequestModel productRequestModel = ProductRequestModel.builder()
            .productName("Product 3")
            .productDescription("Product 3 Description")
            .productSalePrice(25.00)
            .productQuantity(2)
            .build();

    private ProductRequestModel productRequestModelWithInavlidSalePrice = ProductRequestModel.builder()
            .productName("Product 3")
            .productDescription("Product 3 Description")
            .productSalePrice(0.00)
            .productQuantity(2)

            .build();

    private ProductRequestModel productRequestModel2 = ProductRequestModel.builder()
            .productName("Product 4")
            .productDescription("Product 4 Description")
            .productSalePrice(25.00)
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
    public void whenGetAllProductsSortedByRatingAsc_thenReturnProductsSortedAscending() {

        productRepository.saveAll(List.of(product1, product2)).blockLast();


        Rating ratingForProduct1 = Rating.builder()
                .productId(product1.getProductId())
                .customerId(UUID.randomUUID().toString())
                .rating((byte) 4)
                .build();

        Rating ratingForProduct2 = Rating.builder()
                .productId(product2.getProductId())
                .customerId(UUID.randomUUID().toString())
                .rating((byte) 2)
                .build();

        ratingRepository.saveAll(List.of(ratingForProduct1, ratingForProduct2)).blockLast();

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("sort", "asc")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModels -> {
                    assertNotNull(productResponseModels);
                    assertEquals(2, productResponseModels.size());
                    assertEquals(product2.getProductId(), productResponseModels.get(0).getProductId());
                    assertEquals(product1.getProductId(), productResponseModels.get(1).getProductId());
                });
    }
    @Test
    public void whenGetAllProductsSortedByRatingDesc_thenReturnProductsSortedDescending() {

        productRepository.deleteAll().block();
        ratingRepository.deleteAll().block();

        Product product1 = Product.builder()
                .productId("product-1-id")
                .productName("Product 1")
                .productDescription("Product 1 Description")
                .productSalePrice(100.00)
                .productQuantity(2)
                .build();

        Product product2 = Product.builder()
                .productId("product-2-id")
                .productName("Product 2")
                .productDescription("Product 2 Description")
                .productSalePrice(50.00)
                .productQuantity(2)
                .build();

        productRepository.saveAll(List.of(product1, product2)).blockLast();
        Rating ratingForProduct1 = Rating.builder()
                .productId(product1.getProductId())
                .customerId(UUID.randomUUID().toString())
                .rating((byte) 5)
                .build();

        Rating ratingForProduct2 = Rating.builder()
                .productId(product2.getProductId())
                .customerId(UUID.randomUUID().toString())
                .rating((byte) 3)
                .build();

        ratingRepository.saveAll(List.of(ratingForProduct1, ratingForProduct2)).blockLast();

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("sort", "desc")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModels -> {
                    assertNotNull(productResponseModels);
                    assertEquals(2, productResponseModels.size());
                    assertEquals(product1.getProductId(), productResponseModels.get(0).getProductId());
                    assertEquals(product2.getProductId(), productResponseModels.get(1).getProductId());
                });
    }

    @Test
    public void whenGetAllProductsWithInvalidSortParameter_thenReturnBadRequest() {
        // Arrange
        String invalidSortParameter = "invalidSort";

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("sort", invalidSortParameter)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBody(String.class)
                .value(responseBody -> {
                    assertNotNull(responseBody);
                    String expectedMessage = "Invalid sort parameter: " + invalidSortParameter;
                    assertTrue(responseBody.contains(expectedMessage));
                });
    }


    @Test
    public void whenGetAllProducts_thenReturnAllProducts() {
        StepVerifier.create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();

        webTestClient.get()
                .uri("/products")
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
                .uri("/products")
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
                .uri("/products")
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
                .uri("/products")
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
                .uri("/products/" +  product1.getProductId())
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
                .uri("/products/" + NON_EXISTENT_PRODUCT_ID)
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
                .uri("/products/" + INVALID_PRODUCT_ID)
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
    public void whenPatchListingStatus_thenReturnUpdatedProduct() {
        ProductRequestModel patchProductRequestModel = ProductRequestModel.builder()
                .isUnlisted(true)
                .build();

        webTestClient
                .patch()
                .uri("/products/" + product1.getProductId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(patchProductRequestModel), ProductRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertEquals(patchProductRequestModel.getIsUnlisted(), productResponseModel.getIsUnlisted());
                });

        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenPatchListingStatusWithNonExistentProductId_thenThrowNotFoundException() {
        ProductRequestModel patchProductRequestModel = ProductRequestModel.builder()
                .isUnlisted(true)
                .build();

        webTestClient
                .patch()
                .uri("/products/" + NON_EXISTENT_PRODUCT_ID + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(patchProductRequestModel), ProductRequestModel.class)
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
    public void whenPatchListingStatusWithInvalidProductId_thenThrowInvalidInputException() {
        ProductRequestModel patchProductRequestModel = ProductRequestModel.builder()
                .isUnlisted(true)
                .build();

        webTestClient
                .patch()
                .uri("/products/" + INVALID_PRODUCT_ID + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(patchProductRequestModel), ProductRequestModel.class)
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
                .uri("/products/" + product1.getProductId())
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
                .uri("/products/" + NON_EXISTENT_PRODUCT_ID)
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
                .uri("/products/" + INVALID_PRODUCT_ID)
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
                .uri(uriBuilder -> uriBuilder.path("/products")
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
                .uri(uriBuilder -> uriBuilder.path("/products")
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
                .uri(uriBuilder -> uriBuilder.path("/products")
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
                .uri(uriBuilder -> uriBuilder.path("/products")
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
                .uri("/products/"+product1.getProductId()+ "/quantity")
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
    @Test
        void addProduct_FutureReleaseDate_SetsStatusToPreOrder() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(1);
            ProductRequestModel requestModel = new ProductRequestModel();
            requestModel.setProductName("Future Product");
            requestModel.setProductSalePrice(10.0);
            requestModel.setReleaseDate(futureDate);

            // Act & Assert
            webTestClient.post()
                    .uri("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestModel), ProductRequestModel.class)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProductResponseModel.class)
                    .value(response -> {
                        assertNotNull(response.getProductId());
                        assertEquals("Future Product", response.getProductName());
                        assertEquals(ProductStatus.PRE_ORDER, response.getProductStatus());
                        assertEquals(futureDate, requestModel.getReleaseDate());
                    });
        }

        @Test
        void addProduct_PastReleaseDate_SetsStatusToAvailable() {
            // Arrange
            LocalDate pastDate = LocalDate.now().minusDays(1);
            ProductRequestModel requestModel = new ProductRequestModel();
            requestModel.setProductName("Past Product");
            requestModel.setProductSalePrice(10.0);
            requestModel.setReleaseDate(pastDate);

            // Act & Assert
            webTestClient.post()
                    .uri("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestModel), ProductRequestModel.class)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProductResponseModel.class)
                    .value(response -> {
                        assertNotNull(response.getProductId());
                        assertEquals("Past Product", response.getProductName());
                        assertEquals(ProductStatus.AVAILABLE, response.getProductStatus());
                        assertEquals(pastDate, requestModel.getReleaseDate());
                    });
        }

        @Test
        void addProduct_TodayReleaseDate_SetsStatusToAvailable() {
            // Arrange
            LocalDate today = LocalDate.now();
            ProductRequestModel requestModel = new ProductRequestModel();
            requestModel.setProductName("Today Product");
            requestModel.setProductSalePrice(10.0);
            requestModel.setReleaseDate(today);

            // Act & Assert
            webTestClient.post()
                    .uri("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestModel), ProductRequestModel.class)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProductResponseModel.class)
                    .value(response -> {
                        assertNotNull(response.getProductId());
                        assertEquals("Today Product", response.getProductName());
                        assertEquals(ProductStatus.AVAILABLE, response.getProductStatus());
                        assertEquals(today, requestModel.getReleaseDate());
                    });
        }

        @Test
        void addProduct_NullReleaseDate_SetsStatusToAvailable() {
            // Arrange
            ProductRequestModel requestModel = new ProductRequestModel();
            requestModel.setProductName("No Release Date Product");
            requestModel.setProductSalePrice(10.0);
            requestModel.setReleaseDate(null);

            // Act & Assert
            webTestClient.post()
                    .uri("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(requestModel), ProductRequestModel.class)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProductResponseModel.class)
                    .value(response -> {
                        assertNotNull(response.getProductId());
                        assertEquals("No Release Date Product", response.getProductName());
                        assertEquals(ProductStatus.AVAILABLE, response.getProductStatus());
                        assertEquals(null, requestModel.getReleaseDate());
                    });
        }
    }
