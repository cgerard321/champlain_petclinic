package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.Rating;
import com.petclinic.vet.dataaccesslayer.RatingRepository;
import com.petclinic.vet.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class RatingServiceImplTest {

    @Autowired
    RatingService ratingService;

    @MockBean
    RatingRepository ratingRepository;

    @MockBean
    ObjectMapper objectMapper;

    String VET_ID = "vetId";
    Rating rating = buildRating();
    RatingRequestDTO ratingRequestDTO = buildRatingRequestDTO();
    @Test
    void getAllRatingsByVetId() {
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));

        Flux<RatingResponseDTO> ratingResponseDTO = ratingService.getAllRatingsByVetId("vetId");

        StepVerifier
                .create(ratingResponseDTO)
                .consumeNextWith(foundRating -> {
                    assertEquals(rating.getRatingId(), foundRating.getRatingId());
                    assertEquals(rating.getVetId(), foundRating.getVetId());
                    assertEquals(rating.getRateScore(), foundRating.getRateScore());
                })

                .verifyComplete();
    }

    @Test
    void deleteRatingByRatingId() {
        when(ratingRepository.findByVetIdAndRatingId(anyString(), anyString())).thenReturn(Mono.just(rating));
        when(ratingRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedRating = ratingService.deleteRatingByRatingId(rating.getVetId(), rating.getRatingId());

        StepVerifier
                .create(deletedRating)
                .verifyComplete();
    }

    @Test
    void addRatingToVet() {
        ratingService.addRatingToVet(rating.getVetId(), Mono.just(ratingRequestDTO))
                .map(ratingResponseDTO -> {
                    assertEquals(ratingResponseDTO.getVetId(), ratingRequestDTO.getVetId());
                    assertEquals(ratingResponseDTO.getRateScore(), ratingRequestDTO.getRateScore());
                    assertNotNull(ratingResponseDTO.getRatingId());
                    return ratingResponseDTO;
                });
    }

    @Test
    void getNumberOfRatingsByVetId() {
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(1L));

        Mono<Integer> numberOfRatings = ratingService.getNumberOfRatingsByVetId(rating.getVetId());

        StepVerifier
                .create(numberOfRatings)
                .consumeNextWith(foundRating -> {
                    assertEquals(1, foundRating);
                })

                .verifyComplete();
    }

    @Test
    void getAverageRatingByVetId() {
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(1L));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));

        Mono<Double> averageRating = ratingService.getAverageRatingByVetId(rating.getVetId());

        StepVerifier
                .create(averageRating)
                .consumeNextWith(foundRating -> {
                    assertEquals(5.0, foundRating);
                })

                .verifyComplete();
    }

    @Test
    void getRatingPercentagesByVetId() throws JsonProcessingException {
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}");
        Mono<String> ratingPercent = ratingService.getRatingPercentagesByVetId(rating.getVetId());

        StepVerifier
                .create(ratingPercent)
                .consumeNextWith(foundRating -> {
                    assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}", foundRating);
                })

                .verifyComplete();
    }

    // get rating percentage error handling test
    @Test
    void getRatingPercentagesByVetIdError() throws JsonProcessingException {
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.empty());
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        Mono<String> ratingPercent = ratingService.getRatingPercentagesByVetId(rating.getVetId());

        StepVerifier
                .create(ratingPercent)
                .expectError(RuntimeException.class)
                .verify();
    }

    private Rating buildRating() {
        Rating rating = new Rating();
        rating.setRatingId("ratingId");
        rating.setVetId("vetId");
        rating.setRateScore(5.0);
        return rating;
    }

    private RatingRequestDTO buildRatingRequestDTO() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("vetId")
                .rateScore(5.0)
                .build();
        return ratingRequestDTO;
    }
}