package com.petclinic.products.datalayer.ratings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class RatingRepositoryIntegrationTest {
    @Autowired
    private RatingRepository ratingRepository;

    @BeforeEach
    public void setupDB(){
        StepVerifier.create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenRatingsFoundByProductId_thenReturnRatings(){
        String productId = UUID.randomUUID().toString();
        String customerId1 = UUID.randomUUID().toString();
        String customerId2 = UUID.randomUUID().toString();

        Rating rating1 = Rating.builder()
                .productId(productId)
                .customerId(customerId1)
                .rating((byte) 2)
                .build();

        Rating rating2 = Rating.builder()
                .productId(productId)
                .customerId(customerId2)
                .rating((byte) 5)
                .build();

        StepVerifier.create(ratingRepository.save(rating1))
                .consumeNextWith(insRating -> {
                    assertNotNull(insRating);
                    assertEquals(insRating.getId(), rating1.getId());
                    assertEquals(insRating.getProductId(), rating1.getProductId());
                    assertEquals(insRating.getCustomerId(), rating1.getCustomerId());
                })
                .verifyComplete();
        StepVerifier.create(ratingRepository.save(rating2))
                .consumeNextWith(insRating -> {
                    assertNotNull(insRating);
                    assertEquals(insRating.getId(), rating2.getId());
                    assertEquals(insRating.getProductId(), rating2.getProductId());
                    assertEquals(insRating.getCustomerId(), rating2.getCustomerId());
                })
                .verifyComplete();
        StepVerifier.create(ratingRepository.findRatingsByProductId(productId))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenRatingFoundByCustomerAndProduct_thenReturnRating(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        Rating rating = Rating.builder()
                .productId(productId)
                .customerId(customerId)
                .rating((byte) 5)
                .build();

        StepVerifier.create(ratingRepository.save(rating))
                .consumeNextWith(insRating -> {
                    assertNotNull(insRating);
                    assertEquals(insRating.getId(), rating.getId());
                    assertEquals(insRating.getCustomerId(), rating.getCustomerId());
                    assertEquals(insRating.getProductId(), rating.getProductId());
                    assertEquals(insRating.getRating(), rating.getRating());
                })
                .verifyComplete();
        StepVerifier.create(ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId))
                .consumeNextWith(foundRating -> {
                    assertNotNull(foundRating);
                    assertEquals(foundRating.getProductId(), rating.getProductId());
                    assertEquals(foundRating.getCustomerId(), rating.getCustomerId());
                    assertEquals(foundRating.getRating(), rating.getRating());
                })
                .verifyComplete();
    }

    @Test
    public void whenProductDeleted_deleteAllRatings(){
        String productId = UUID.randomUUID().toString();
        String customerId1 = UUID.randomUUID().toString();
        String customerId2 = UUID.randomUUID().toString();

        Rating rating1 = Rating.builder()
                .productId(productId)
                .customerId(customerId1)
                .rating((byte) 2)
                .build();

        Rating rating2 = Rating.builder()
                .productId(productId)
                .customerId(customerId2)
                .rating((byte) 5)
                .build();

        StepVerifier.create(ratingRepository.save(rating1))
                .consumeNextWith(insRating -> {
                    assertNotNull(insRating);
                    assertEquals(insRating.getId(), rating1.getId());
                    assertEquals(insRating.getProductId(), rating1.getProductId());
                    assertEquals(insRating.getCustomerId(), rating1.getCustomerId());
                    assertEquals(insRating.getRating(), rating1.getRating());
                })
                .verifyComplete();
        StepVerifier.create(ratingRepository.save(rating2))
                .consumeNextWith(insRating -> {
                    assertNotNull(insRating);
                    assertEquals(insRating.getId(), rating2.getId());
                    assertEquals(insRating.getProductId(), rating2.getProductId());
                    assertEquals(insRating.getCustomerId(), rating2.getCustomerId());
                    assertEquals(insRating.getRating(), rating2.getRating());
                })
                .verifyComplete();
        StepVerifier.create(ratingRepository.findRatingsByProductId(productId))
                .expectNextCount(2)
                .verifyComplete();
        // Check if deletion returns the ratings deleted
        StepVerifier.create(ratingRepository.deleteRatingsByProductId(productId))
                .expectNextCount(2)
                .verifyComplete();
        // Check if deletion happened on DB
        StepVerifier.create(ratingRepository.findRatingsByProductId(productId))
                .expectNextCount(0)
                .verifyComplete();
    }
}