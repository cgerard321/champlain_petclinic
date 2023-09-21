package com.petclinic.vet.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class RatingRepositoryTest {
    @Autowired
    RatingRepository ratingRepository;

    Rating rating1 = Rating.builder()
            .ratingId("1")
            .vetId("1")
            .rateScore(5.0)
            .build();

    Rating rating2 = Rating.builder()
            .ratingId("2")
            .vetId("2")
            .rateScore(4.0)
            .build();
    @BeforeEach
    void setUp() {
        Publisher<Rating> setUp = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setUp)
                .expectNextCount(1)
                .verifyComplete();

        Publisher<Rating> setUp2 = ratingRepository.save(rating2);

        StepVerifier
                .create(setUp2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void getAllRatingsOfAVet_ShouldSucceed(){
        Publisher<Rating> find = ratingRepository.findAllByVetId("1");

        StepVerifier
                .create(find)
                .consumeNextWith(foundRating -> {
                    assertEquals(rating1.getRatingId(), foundRating.getRatingId());
                    assertEquals(rating1.getVetId(), foundRating.getVetId());
                    assertEquals(rating1.getRateScore(), foundRating.getRateScore());
                })
                .verifyComplete();
    }
    @Test
    public void deleteRatingOfVet_ShouldSucceed () {
        StepVerifier
                .create(ratingRepository.delete(rating1))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void addRatingToAVet_ShouldSucced(){
        Rating rating = Rating.builder()
                .ratingId("3")
                .vetId("1")
                .rateScore(1.0)
                .rateDescription("My dog wouldn't stop crying after his appointment")
                .rateDate("13/09/2023")
                .build();

        StepVerifier.create(ratingRepository.save(rating))
                .consumeNextWith(createdRating -> {
                    assertEquals(rating.getRatingId(), createdRating.getRatingId());
                    assertEquals(rating.getVetId(), createdRating.getVetId());
                    assertEquals(rating.getRateScore(), createdRating.getRateScore());
                    assertEquals(rating.getRateDescription(), createdRating.getRateDescription());
                    assertEquals(rating.getRateDate(), createdRating.getRateDate());
                })
                .verifyComplete();
    }

    @Test
    public void updateRatingOfVet_ShouldSucceed(){
        Rating rating = Rating.builder()
                .ratingId("2")
                .vetId("2")
                .rateScore(1.0)
                .rateDescription("My dog wouldn't stop crying after his appointment")
                .rateDate("13/09/2023")
                .build();
    }

    @Test
    public void getNumberOfRatingOfAVet_ShouldSucceed(){
        Publisher<Long> find = ratingRepository.countAllByVetId("1");

        StepVerifier
                .create(find)
                .consumeNextWith(foundRating -> {
                    assertEquals(1, foundRating);
                })
                .verifyComplete();
    }

    @Test
    public void getAverageRatingOfAVet_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1))
                .thenMany(ratingRepository.save(rating2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Rating rating = Rating.builder()
                .ratingId("1")
                .vetId("1")
                .rateScore(5.0)
                .build();

        Mono<Long> findRatings = ratingRepository.countAllByVetId(rating.getVetId());
        Publisher<Long> composite = Mono
                .from(setup)
                .then(findRatings);

        StepVerifier
                .create(composite)
                .consumeNextWith(foundRating -> {
                    assertEquals(1, foundRating);
                })
                .verifyComplete();
    }



}