package com.petclinic.vet.dataaccesslayer.ratings;

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

    Mono<String> deleteByVetId(String vetId);
   // Mono<Integer> countAllByVetIdAndPredefinedDescription(String vetId, PredefinedDescription predefinedDescription);
}