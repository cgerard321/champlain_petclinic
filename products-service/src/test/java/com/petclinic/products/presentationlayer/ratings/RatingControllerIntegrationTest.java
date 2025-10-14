package com.petclinic.products.presentationlayer.ratings;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port= 0"})
@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@AutoConfigureWebTestClient
class RatingControllerIntegrationTest {
    @Autowired
    WebTestClient webClient;

    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private ProductRepository productRepository;

    private final String INVALID_PRODUCT_ID = "e39286b6-d3cab6222f58";
    private final String UNFOUND_PRODUCT_ID = "828537fe-907d-4ece-8455-c7e483327a68";

    private final String INVALID_USER_ID = "186a7ca6-5fb756923d9d";
    // No not found for users, as we receive the id directly from api-gateway, which checks for user existence

    Product product1 = Product.builder()
            .productId("e39286b6-d3ca-4f58-8b6f-30f1f1b9a2f6")
            .productName("product1")
            .productName("product1 description")
            .productSalePrice(100.0)
            .build();

    Product product2 = Product.builder()
            .productId("6866ec50-898d-435c-bfec-8ee513c5a6c1")
            .productName("product2")
            .productName("product2 description")
            .productSalePrice(200.0)
            .build();

    Rating rating1Prod1 = Rating.builder()
            .rating((byte) 2)
            .review("It's not bad neither good")
            .productId("e39286b6-d3ca-4f58-8b6f-30f1f1b9a2f6")
            .customerId("8e599d37-a69a-48d2-880e-1b621abf77b6")
            .build();

    Rating rating2Prod1 = Rating.builder()
            .rating((byte) 4)
            .review("It's good for its value")
            .productId("e39286b6-d3ca-4f58-8b6f-30f1f1b9a2f6")
            .customerId("3a994002-47d9-4dea-9966-9df78050cf09")
            .build();

    Rating rating1Prod2 = Rating.builder()
            .rating((byte) 5)
            .review("It's great")
            .productId("6866ec50-898d-435c-bfec-8ee513c5a6c1")
            .customerId("8e599d37-a69a-48d2-880e-1b621abf77b6")
            .build();

    Rating rating2Prod2 = Rating.builder()
            .rating((byte) 3)
            .review("It's not bad")
            .productId("6866ec50-898d-435c-bfec-8ee513c5a6c1")
            .customerId("3a994002-47d9-4dea-9966-9df78050cf09")
            .build();

    @BeforeEach
    public void setupDB(){
        Publisher<Product> productSetup = productRepository.deleteAll()
                .thenMany(Flux.just(product1, product2))
                .flatMap(productRepository::save);
        StepVerifier.create(productSetup)
                .expectNextCount(2)
                .verifyComplete();

        Publisher<Rating> ratingSetup = ratingRepository.deleteAll()
                .thenMany(Flux.just(rating1Prod1, rating2Prod1, rating1Prod2, rating2Prod2))
                .flatMap(ratingRepository::save);
        StepVerifier.create(ratingSetup)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenGetRatingsForProduct_thenReturnRatings(){
        webClient.get()
                .uri("/ratings/" + product1.getProductId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .expectBodyList(RatingResponseModel.class)
                .contains(
                        EntityModelUtil.toRatingResponseModel(rating1Prod1),
                        EntityModelUtil.toRatingResponseModel(rating2Prod1)
                );
    }

    @Test
    public void whenGetRatingsForNotFoundProduct_thenReturnNotFoundProduct(){
        webClient.get()
                .uri("/ratings/" + UNFOUND_PRODUCT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product id not found: " + UNFOUND_PRODUCT_ID);
    }

    @Test
    public void whenGetRatingsForInvalidProduct_thenReturnInvalidProduct(){
        webClient.get()
                .uri("/ratings/" + INVALID_PRODUCT_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + INVALID_PRODUCT_ID);
    }

    @Test
    public void whenGetRatingForProductByCustomer_thenReturnRating(){
        webClient.get()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseModel.class)
                .isEqualTo(EntityModelUtil.toRatingResponseModel(rating1Prod1));
    }

    @Test
    public void whenGetRatingForValidProductInvalidCustomer_thenReturnInvalidCustomer(){
        webClient.get()
                .uri("/ratings/" + product1.getProductId() + "/" + INVALID_USER_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided customer id is invalid: " + INVALID_USER_ID);
    }

    @Test
    public void whenGetRatingForInvalidProductValidCustomer_thenReturnInvalidProduct(){
        webClient.get()
                .uri("/ratings/" + INVALID_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + INVALID_PRODUCT_ID);
    }

//    @Test
    public void whenAddRatingForProductByCustomer_thenReturnRating(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .review("It's great")
                .rating((byte) 5)
                .build();
        String randomCustomer = UUID.randomUUID().toString();

        webClient.post()
                .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseModel.class)
                .consumeWith(response -> {
                    RatingResponseModel responseModel = response.getResponseBody();
                    assertNotNull(responseModel);
                    assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
                    assertEquals(ratingRequestModel.getReview(), responseModel.getReview());
                });
        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(5)
                .verifyComplete();
    }

//    @Test
//    public void whenAddRatingWithNullReview_thenReturnEmptyStringReview(){
//        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
//                .review(null)
//                .rating((byte) 5)
//                .build();
//        String randomCustomer = UUID.randomUUID().toString();
//
//        webClient.post()
//                .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(ratingRequestModel)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(RatingResponseModel.class)
//                .consumeWith(response -> {
//                    RatingResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
//                    assertNotNull(responseModel.getReview());
//                    assertEquals("", responseModel.getReview());
//                });
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(4)
//                .verifyComplete();
//    }
//
//    @Test
//    public void whenAddRatingWithTooLongReview_thenReturnInvalidInput(){
//        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
//                .review("It's great".repeat(200))
//                .rating((byte) 5)
//                .build();
//        String randomCustomer = UUID.randomUUID().toString();
//
//        webClient.post()
//                .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(ratingRequestModel)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Review must be less than 2000 characters");
//
//
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(4)
//                .verifyComplete();
//    }

    @Test
    public void whenAddNullRating_thenReturnInvalidInput(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .review("It's great")
                .rating(null)
                .build();
        String randomCustomer = UUID.randomUUID().toString();

        webClient.post()
                .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating must be provided");

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    //TEST FLAKY
//    @Test
//    public void whenAddRatingForProduct_thenRecalculateAverage(){
//
//        productRepository.deleteAll().block();
//        ratingRepository.deleteAll().block();
//
//        productRepository.save(product1).block();
//        ratingRepository.saveAll(Flux.just(rating1Prod1, rating2Prod1)).blockLast();
//
//        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
//                .rating((byte) 5)
//                .build();
//        String randomCustomer = UUID.randomUUID().toString();
//
//        Double sum = (rating1Prod1.getRating().doubleValue() + rating2Prod1.getRating().doubleValue() + ratingRequestModel.getRating().doubleValue()) / 3d;
//
//        webClient.post()
//                .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(ratingRequestModel)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(RatingResponseModel.class)
//                .consumeWith(response -> {
//                    RatingResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
//                });
//
//        webClient.get()
//                .uri("/products/" + product1.getProductId())
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(ProductResponseModel.class)
//                .consumeWith(response -> {
//                    ProductResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(product1.getProductId(), responseModel.getProductId());
//                    assertEquals(
//                            (Math.floor(sum * 100) / 100),
//                            responseModel.getAverageRating());
//                });
//
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(3)
//                .verifyComplete();
//    }

    @Test
    public void whenAddRatingForProductWithExistingCustomer_thenReturnRatingAlreadyExists(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.post()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating already exists for customer " + rating1Prod1.getCustomerId() + " for product " + product1.getProductId());

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    // @Test
    // public void whenAddInvalidRating_thenReturnInvalidRating(){
    //     RatingRequestModel ratingRequestModel1 = RatingRequestModel.builder()
    //             .rating((byte) 6)
    //             .build();

    //     RatingRequestModel ratingRequestModel2 = RatingRequestModel.builder()
    //             .rating((byte) 0)
    //             .build();

    //     String randomCustomer = UUID.randomUUID().toString();

    //     webClient.post()
    //             .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .bodyValue(ratingRequestModel1)
    //             .exchange()
    //             .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
    //             .expectHeader().contentType(MediaType.APPLICATION_JSON)
    //             .expectBody()
    //             .jsonPath("$.message").isEqualTo("Rating must be between 1 and 5");

    //     webClient.post()
    //             .uri("/ratings/" + product1.getProductId() + "/" + randomCustomer)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .bodyValue(ratingRequestModel2)
    //             .exchange()
    //             .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
    //             .expectHeader().contentType(MediaType.APPLICATION_JSON)
    //             .expectBody()
    //             .jsonPath("$.message").isEqualTo("Rating must be between 1 and 5");

    //     StepVerifier.create(ratingRepository.findAll())
    //             .expectNextCount(4)
    //             .verifyComplete();
    // }

    @Test
    public void whenAddRatingForProductWithInvalidCustomer_thenReturnInvalidCustomer(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.post()
                .uri("/ratings/" + product1.getProductId() + "/" + INVALID_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided customer id is invalid: " + INVALID_USER_ID);

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenAddRatingForInvalidProduct_thenReturnInvalidProduct(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.post()
                .uri("/ratings/" + INVALID_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + INVALID_PRODUCT_ID);

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenAddRatingForNotFoundProduct_thenReturnNotFoundProduct(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.post()
                .uri("/ratings/" + UNFOUND_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product id not found: " + UNFOUND_PRODUCT_ID);

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }
//
//    @Test
//    public void whenUpdateRating_thenReturnUpdatedRating(){
//        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
//                .rating((byte) 5)
//                .build();
//
//        webClient.put()
//                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(ratingRequestModel)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(RatingResponseModel.class)
//                .consumeWith(response -> {
//                    RatingResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
//                });
//
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(4)
//                .verifyComplete();
//    }

    // @Test
    // public void whenUpdateWithNullReview_thenReturnEmptyStringReview(){
    //     RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
    //             .rating((byte) 5)
    //             .review(null)
    //             .build();

    //     webClient.put()
    //             .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .bodyValue(ratingRequestModel)
    //             .exchange()
    //             .expectStatus().isOk()
    //             .expectHeader().contentType(MediaType.APPLICATION_JSON)
    //             .expectBody(RatingResponseModel.class)
    //             .consumeWith(response -> {
    //                 RatingResponseModel responseModel = response.getResponseBody();
    //                 assertNotNull(responseModel);
    //                 assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
    //                 assertNotNull(responseModel.getReview());
    //                 assertEquals("", responseModel.getReview());
    //             });

    //     StepVerifier.create(ratingRepository.findAll())
    //             .expectNextCount(4)
    //             .verifyComplete();
    // }

    @Test
    public void whenUpdateWithLongReview_thenReturnInvalidInput(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .review("It's great".repeat(200))
                .build();

        webClient.put()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Review must be less than 2000 characters");

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenUpdateWithNullRating_thenReturnInvalidInput(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating(null)
                .review("It's great")
                .build();

        webClient.put()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating must be provided");

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenUpdateWithInvalidRatingForProduct_thenReturnInvalidRating(){
        RatingRequestModel ratingRequestModel1 = RatingRequestModel.builder()
                .rating((byte) 6)
                .build();

        RatingRequestModel ratingRequestModel2 = RatingRequestModel.builder()
                .rating((byte) 0)
                .build();

        webClient.put()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel1)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating must be between 1 and 5");

        webClient.put()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel2)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating must be between 1 and 5");
    }

//    Flaky test
//    @Test
//    public void whenUpdateRatingForProduct_thenRecalculateAverage(){
//        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
//                .rating((byte) 1)
//                .build();
//
//        webClient.put()
//                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(ratingRequestModel)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(RatingResponseModel.class)
//                .consumeWith(response -> {
//                    RatingResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(ratingRequestModel.getRating(), responseModel.getRating());
//                });
//
//        webClient.get()
//                .uri("/products/" + product1.getProductId())
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(ProductResponseModel.class)
//                .consumeWith(response -> {
//                    ProductResponseModel responseModel = response.getResponseBody();
//                    assertNotNull(responseModel);
//                    assertEquals(product1.getProductId(), responseModel.getProductId());
//                    assertEquals(
//                            (rating2Prod1.getRating().doubleValue() + ratingRequestModel.getRating().doubleValue()) / 2d,
//                            responseModel.getAverageRating());
//                });
//
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(4)
//                .verifyComplete();
//    }

    @Test
    public void whenUpdateRatingForProductWithInvalidCustomer_thenReturnInvalidCustomer(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.put()
                .uri("/ratings/" + product1.getProductId() + "/" + INVALID_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided customer id is invalid: " + INVALID_USER_ID);
    }

    @Test
    public void whenUpdateRatingForInvalidProduct_thenReturnInvalidProduct(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.put()
                .uri("/ratings/" + INVALID_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + INVALID_PRODUCT_ID);
    }

    @Test
    public void whenUpdateRatingForNotFoundProduct_thenReturnNotFoundProduct(){
        RatingRequestModel ratingRequestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .build();

        webClient.put()
                .uri("/ratings/" + UNFOUND_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestModel)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product id not found: " + UNFOUND_PRODUCT_ID);
    }

    @Test
    public void whenDeleteRating_thenReturnRating(){
        webClient.delete()
                .uri("/ratings/" + product1.getProductId() + "/" + rating1Prod1.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseModel.class)
                .isEqualTo(EntityModelUtil.toRatingResponseModel(rating1Prod1));

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void whenDeleteRatingWithInvalidCustomer_thenReturnInvalidCustomer(){
        webClient.delete()
                .uri("/ratings/" + product1.getProductId() + "/" + INVALID_USER_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided customer id is invalid: " + INVALID_USER_ID);

        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void whenDeleteRatingForInvalidProduct_thenReturnInvalidProduct(){
        webClient.delete()
                .uri("/ratings/" + INVALID_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + INVALID_PRODUCT_ID);
        StepVerifier.create(ratingRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }
// Weird flaky test
//    @Test
//    public void whenDeleteRatingForNotFoundProduct_thenReturnNotFoundProduct(){
//        webClient.delete()
//                .uri("/ratings/" + UNFOUND_PRODUCT_ID + "/" + rating1Prod1.getCustomerId())
//                .exchange()
//                .expectStatus().isNotFound()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Product id not found: " + UNFOUND_PRODUCT_ID);
//        StepVerifier.create(ratingRepository.findAll())
//                .expectNextCount(4)
//                .verifyComplete();
//    }
}
