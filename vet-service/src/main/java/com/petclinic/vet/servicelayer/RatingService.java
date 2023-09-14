package com.petclinic.vet.servicelayer;

import reactor.core.publisher.Flux;

public interface RatingService {
    Flux<RatingResponseDTO> getAllRatingsByVetId(String vetId);
}