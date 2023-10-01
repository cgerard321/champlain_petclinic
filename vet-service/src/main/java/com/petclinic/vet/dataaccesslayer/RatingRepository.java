package com.petclinic.vet.dataaccesslayer;

import com.petclinic.vet.servicelayer.VetAverageRatingDTO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RatingRepository extends ReactiveMongoRepository<Rating, String> {
    Flux<Rating> findAllByVetId(String vetId);
    Mono<Long> countAllByVetId(String vetId);
    Mono<Rating> findByVetIdAndRatingId(String vetId, String ratingId);
    Mono<Rating> findByRatingId(String ratingId);
   // Mono<Integer> countAllByVetIdAndPredefinedDescription(String vetId, PredefinedDescription predefinedDescription);
}