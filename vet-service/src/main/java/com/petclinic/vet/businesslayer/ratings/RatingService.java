package com.petclinic.vet.businesslayer.ratings;

import com.petclinic.vet.presentationlayer.vets.VetAverageRatingDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface RatingService {
    Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId);
    Mono<Integer> getNumberOfRatingsByVetId(String vetId);
    Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO);
    Mono<RatingResponseDTO> updateRatingByVetIdAndRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono);
    Mono<Void> deleteRatingByRatingId(String vetId, String ratingId);
    Mono<Void> deleteRatingByVetIdAndCustomerName(String vetId, String customerName);
    Mono<Double> getAverageRatingByVetId(String vetId);
    Flux<RatingResponseDTO> getRatingsOfAVetBasedOnDate(String vetId, Map<String,String> queryParams);
    Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating();
    Mono<String> getRatingPercentagesByVetId(String vetId);
}