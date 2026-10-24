package com.petclinic.products.businesslayer.ratings;

import java.time.LocalDateTime;
import java.util.UUID;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.datalayer.ratings.ReviewLikeRepository;
import com.petclinic.products.datalayer.ratings.ReviewLike;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.datalayer.ratings.ReviewSettings;
import com.petclinic.products.datalayer.ratings.ReviewSettingsRepository;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.RatingAlreadyExists;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceUnitTest {
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewLikeRepository reviewLikeRepository;
    @Mock
    private ReviewSettingsRepository reviewSettingsRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;


    private String productId = UUID.randomUUID().toString();

    Rating rating1 = Rating.builder()
            .productId(productId)
            .customerId(UUID.randomUUID().toString())
            .rating((byte) 5)
            .qualityRating((byte) 5)
            .valueRating((byte) 4)
            .petSatisfactionRating((byte) 5)
            .ingredientsRating((byte) 4)
            .createdAt(LocalDateTime.now().minusHours(1))
            .review("Good stuff!")
            .build();

    Rating rating2 = Rating.builder()
            .productId(productId)
            .customerId(UUID.randomUUID().toString())
            .rating((byte) 3)
            .qualityRating((byte) 5)
            .valueRating((byte) 4)
            .petSatisfactionRating((byte) 5)
            .ingredientsRating((byte) 4)
            .createdAt(LocalDateTime.now().minusHours(1))
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
        when(reviewLikeRepository.countByProductIdAndReviewCustomerId(productId, rating1.getCustomerId()))
                .thenReturn(Mono.just(0L));
        when(reviewLikeRepository.countByProductIdAndReviewCustomerId(productId, rating2.getCustomerId()))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(ratingService.getAllRatingsForProductId(productId))
                .expectNextMatches(response -> {
                    assertEquals(rating1.getCustomerId(), response.getCustomerId());
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    return true;
                })
                .expectNextMatches(response -> {
                    assertEquals(rating2.getCustomerId(), response.getCustomerId());
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                    assertEquals(requestModel.getQualityRating(), response.getQualityRating());
                    assertEquals(requestModel.getValueRating(), response.getValueRating());
                    assertEquals(requestModel.getPetSatisfactionRating(), response.getPetSatisfactionRating());
                    assertEquals(requestModel.getIngredientsRating(), response.getIngredientsRating());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenAddRatingForNotFoundProduct_thenReturnNotFound(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                .review("a".repeat(10001))
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
    void whenAddRatingWithMissingCategoryRating_thenReturnInvalidInput() {
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .review("Great product")
                .qualityRating(null)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        StepVerifier.create(ratingService.addRatingForProduct(
                        productId,
                        rating1.getCustomerId(),
                        Mono.just(requestModel)))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals("All category ratings must be provided"))
                .verify();

        verifyNoInteractions(productRepository);
    }

    @Test
    void whenAddRatingWithInvalidCategoryRating_thenReturnInvalidInput() {
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 5)
                .review("Great product")
                .qualityRating((byte) 6)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        StepVerifier.create(ratingService.addRatingForProduct(
                        productId,
                        rating1.getCustomerId(),
                        Mono.just(requestModel)))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals("Category ratings must be between 1 and 5"))
                .verify();

        verifyNoInteractions(productRepository);
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(reviewSettingsRepository.findById(1L))
                .thenReturn(Mono.just(new ReviewSettings(1L, 48)));
        when(ratingRepository.save(any(Rating.class))
        ).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ratingService.updateRatingForProduct(productId, rating1.getCustomerId(), Mono.just(requestModel)))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    assertNotNull(response.getReview());
                    assertEquals(requestModel.getRating(), response.getRating());
                    assertEquals(requestModel.getQualityRating(), response.getQualityRating());
                    assertEquals(requestModel.getValueRating(), response.getValueRating());
                    assertEquals(requestModel.getPetSatisfactionRating(), response.getPetSatisfactionRating());
                    assertEquals(requestModel.getIngredientsRating(), response.getIngredientsRating());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenUpdateRatingForNotFoundProduct_thenReturnNotFound(){
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating(rating1.getRating())
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(reviewSettingsRepository.findById(1L))
                .thenReturn(Mono.just(new ReviewSettings(1L, 48)));
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
                .review("a".repeat(10001))
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
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
    void whenUpdateRatingWithMissingCategoryRating_thenReturnInvalidInput() {
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 4)
                .review("Updated review")
                .qualityRating((byte) 4)
                .valueRating(null)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        StepVerifier.create(ratingService.updateRatingForProduct(
                        productId,
                        rating1.getCustomerId(),
                        Mono.just(requestModel)))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals("All category ratings must be provided"))
                .verify();

        verifyNoInteractions(productRepository);
    }

    @Test
    void whenUpdateRatingWithInvalidCategoryRating_thenReturnInvalidInput() {
        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 4)
                .review("Updated review")
                .qualityRating((byte) 4)
                .valueRating((byte) 3)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 0)
                .build();

        StepVerifier.create(ratingService.updateRatingForProduct(
                        productId,
                        rating1.getCustomerId(),
                        Mono.just(requestModel)))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals("Category ratings must be between 1 and 5"))
                .verify();

        verifyNoInteractions(productRepository);
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
        when(reviewLikeRepository.deleteByProductIdAndReviewCustomerId(productId, rating1.getCustomerId()))
                .thenReturn(Mono.empty());

        StepVerifier.create(ratingService.deleteRatingForProduct(productId, rating1.getCustomerId()))
                .expectNextMatches(response -> {
                    assertNotNull(response.getRating());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenLikeReviewTwice_thenSecondClickRemovesLike() {
        String likingCustomerId = UUID.randomUUID().toString();
        Product product = Product.builder().productId(productId).build();
        ReviewLike savedLike = ReviewLike.builder()
                .productId(productId)
                .reviewCustomerId(rating1.getCustomerId())
                .likingCustomerId(likingCustomerId)
                .build();

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(product));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(reviewLikeRepository.existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
                productId, rating1.getCustomerId(), likingCustomerId))
                .thenReturn(Mono.just(false), Mono.just(true));
        when(reviewLikeRepository.addIfAbsent(productId, rating1.getCustomerId(), likingCustomerId))
                .thenReturn(Mono.just(savedLike));
        when(reviewLikeRepository.deleteByProductIdAndReviewCustomerIdAndLikingCustomerId(
                productId, rating1.getCustomerId(), likingCustomerId))
                .thenReturn(Mono.empty());
        when(reviewLikeRepository.countByProductIdAndReviewCustomerId(productId, rating1.getCustomerId()))
                .thenReturn(Mono.just(1L), Mono.just(0L));

        StepVerifier.create(ratingService.likeReview(productId, rating1.getCustomerId(), likingCustomerId))
                .expectNextMatches(response -> response.getLikeCount() == 1 && response.isLikedByCurrentUser())
                .verifyComplete();

        StepVerifier.create(ratingService.likeReview(productId, rating1.getCustomerId(), likingCustomerId))
                .expectNextMatches(response -> response.getLikeCount() == 0 && !response.isLikedByCurrentUser())
                .verifyComplete();
    }

    @Test
    void whenCustomerLikesOwnReview_thenReturnInvalidInput() {
        StepVerifier.create(ratingService.likeReview(productId, rating1.getCustomerId(), rating1.getCustomerId()))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(productRepository, ratingRepository, reviewLikeRepository);
    }

    @Test
    void whenLikeReviewForNotFoundProduct_thenReturnNotFound() {
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.empty());
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.just(rating1));
        when(reviewLikeRepository.existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
                productId, rating1.getCustomerId(), "liking-customer"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(ratingService.likeReview(productId, rating1.getCustomerId(), "liking-customer"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void whenLikeReviewThatDoesNotExist_thenReturnNotFound() {
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(Product.builder().productId(productId).build()));
        when(ratingRepository.findRatingByCustomerIdAndProductId(rating1.getCustomerId(), productId))
                .thenReturn(Mono.empty());
        when(reviewLikeRepository.existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
                productId, rating1.getCustomerId(), "liking-customer"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(ratingService.likeReview(productId, rating1.getCustomerId(), "liking-customer"))
                .expectError(NotFoundException.class)
                .verify();
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

    @Test
    void whenUpdateRatingWithinEditPeriod_thenUpdateRating() {
        when(reviewSettingsRepository.findById(1L))
                .thenReturn(Mono.just(new ReviewSettings(1L, 48)));

        Rating existingRating = Rating.builder()
                .id(rating1.getId())
                .productId(rating1.getProductId())
                .customerId(rating1.getCustomerId())
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        when(productRepository.findProductByProductId(existingRating.getProductId()))
                .thenReturn(Mono.just(
                        Product.builder()
                                .productId(existingRating.getProductId())
                                .build()
                ));

        when(ratingRepository.findRatingByCustomerIdAndProductId(
                existingRating.getCustomerId(),
                existingRating.getProductId()))
                .thenReturn(Mono.just(existingRating));

        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        RatingRequestModel request = RatingRequestModel.builder()
                .rating((byte) 4)
                .review("Updated review")
                .qualityRating((byte) 4)
                .valueRating((byte) 3)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 2)
                .build();

        StepVerifier.create(
                        ratingService.updateRatingForProduct(
                                existingRating.getProductId(),
                                existingRating.getCustomerId(),
                                Mono.just(request)))
                .expectNextMatches(response ->
                        response.getRating() == 4 &&
                                response.getReview().equals("Updated review") &&
                                response.getQualityRating() == 4 &&
                                response.getValueRating() == 3 &&
                                response.getPetSatisfactionRating() == 5 &&
                                response.getIngredientsRating() == 2)
                .verifyComplete();
    }

    @Test
    void whenUpdateRatingAfterEditPeriod_thenRejectUpdate() {
        when(reviewSettingsRepository.findById(1L))
                .thenReturn(Mono.just(new ReviewSettings(1L, 48)));

        Rating existingRating = Rating.builder()
                .id(rating1.getId())
                .productId(rating1.getProductId())
                .customerId(rating1.getCustomerId())
                .rating(rating1.getRating())
                .review(rating1.getReview())
                .createdAt(LocalDateTime.now().minusHours(49))
                .build();

        when(productRepository.findProductByProductId(existingRating.getProductId()))
                .thenReturn(Mono.just(
                        Product.builder()
                                .productId(existingRating.getProductId())
                                .build()
                ));

        when(ratingRepository.findRatingByCustomerIdAndProductId(
                existingRating.getCustomerId(),
                existingRating.getProductId()))
                .thenReturn(Mono.just(existingRating));

        RatingRequestModel request = RatingRequestModel.builder()
                .rating((byte) 4)
                .review("Updated review")
                .qualityRating((byte) 4)
                .valueRating((byte) 3)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 2)
                .build();

        StepVerifier.create(
                        ratingService.updateRatingForProduct(
                                existingRating.getProductId(),
                                existingRating.getCustomerId(),
                                Mono.just(request)))
                .expectErrorMatches(error ->
                        error.getMessage().equals("This review is no longer editable"))
                .verify();

        verify(ratingRepository, never()).save(any(Rating.class));
    }
}
