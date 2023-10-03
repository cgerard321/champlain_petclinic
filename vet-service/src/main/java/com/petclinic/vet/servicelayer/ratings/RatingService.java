package com.petclinic.vet.servicelayer.ratings;

import com.petclinic.vet.servicelayer.VetAverageRatingDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {
    Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId);
    Mono<Integer> getNumberOfRatingsByVetId(String vetId);
    Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO);
    Mono<RatingResponseDTO> updateRatingByVetIdAndRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono);
    Mono<Void> deleteRatingByRatingId(String vetId, String ratingId);
    Mono<Double> getAverageRatingByVetId(String vetId);
    Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating();
    Mono<String> getRatingPercentagesByVetId(String vetId);
}