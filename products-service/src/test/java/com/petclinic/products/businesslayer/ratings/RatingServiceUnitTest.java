package com.petclinic.products.businesslayer.ratings;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.RatingAlreadyExists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceUnitTest {
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;


    private String productId = UUID.randomUUID().toString();

    Rating rating1 = Rating.builder()
            .productId(productId)
            .customerId(UUID.randomUUID().toString())
            .rating((byte) 5)
            .review("Good stuff!")
            .build();

    Rating rating2 = Rating.builder()
            .productId(productId)
            .customerId(UUID.randomUUID().toString())
            .rating((byte) 3)
            .review("It's alright..")
            .build();

    @Test
    void whenGetAllRatingsForProduct_thenReturnRatings() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingsByProductId(rating1.getProductId()))
                .thenReturn(Flux.just(rating1, rating2));

        StepVerifier.create(ratingService.getAllRatingsForProductId(productId))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    return true;
                })
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetAllRatingsForNotFoundProduct_thenReturnNotFound(){
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.empty());
        when(ratingRepository.findRatingsByProductId(rating1.getProductId()))
                .thenReturn(Flux.empty());

        StepVerifier.create(ratingService.getAllRatingsForProductId(productId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenGetRatingByProductAndCustomer_thenReturnRating(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), rating1.getProductId()))
                .thenReturn(Mono.just(rating1));

        StepVerifier.create(ratingService.getRatingForProductIdWithCustomerId(rating1.getProductId(), rating1.getCustomerId()))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetRatingForNotFoundCustomer_thenThrowNotFound(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), rating1.getProductId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.getRatingForProductIdWithCustomerId(rating1.getProductId(), rating1.getCustomerId()))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenAddRating_thenReturnRating() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.empty());
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    assertEquals(requestModel.getRating(), response.getRating());
                    assertEquals(requestModel.getReview(), response.getReview());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenAddRatingForNotFoundProduct_thenReturnNotFound(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenAddRatingWithEmptyReview_thenReturnRatingWithEmptyReview(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(null)
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.empty());
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    assertEquals(requestModel.getRating(), response.getRating());
                    assertEquals("", response.getReview());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenAddRatingWithLongReview_thenReturnInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review("a".repeat(2000))
                .build();

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    public void whenAddRatingWithEmptyRating_thenReturnInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(null)
                .review(rating1.getReview())
                .build();

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenAddRatingForExistingCustomerRating_thenReturnRatingAlreadyExists(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(RatingAlreadyExists.class)
                .verify();
    }

    @Test
    void whenAddRatingWithInvalidRating_thenThrowInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 6)
                .build();

        StepVerifier.create(ratingService.addRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenUpdateRating_thenReturnRating() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    assertEquals(requestModel.getRating(), response.getRating());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenUpdateRatingForNotFoundProduct_thenReturnNotFound(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenUpdateRatingWithEmptyReview_thenReturnRatingWithEmptyReview(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(null)
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    assertEquals(requestModel.getRating(), response.getRating());
                    assertEquals("", response.getReview());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenUpdateRatingWithLongReview_thenReturnInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review("a".repeat(2000))
                .build();

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    public void whenUpdateRatingWithEmptyRating_thenReturnInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(null)
                .review(rating1.getReview())
                .build();

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenUpdateRatingForNotFoundRating_thenReturnNotFound(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(Product.builder().build()));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenUpdateRatingWithInvalidRating_thenReturnInvalidInput(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 6)
                .build();

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void whenDeleteRating_thenReturnRating() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(ratingRepository.delete(any(Rating.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.deleteRatingForProduct(productId, rating1.getCustomerId()))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenDeleteRatingForNotFoundProduct_thenReturnNotFound(){
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.empty());
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));

        StepVerifier.create(ratingService.deleteRatingForProduct(productId, rating1.getCustomerId()))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenDeleteRatingForNotFoundRating_thenReturnNotFound(){
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.deleteRatingForProduct(productId, rating1.getCustomerId()))
                .expectError(NotFoundException.class)
                .verify();
    }
}