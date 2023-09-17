package com.petclinic.vet.servicelayer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {
    Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId);
    Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO);
    Mono<Void> deleteRatingByRatingId(String vetId, String ratingId);

}