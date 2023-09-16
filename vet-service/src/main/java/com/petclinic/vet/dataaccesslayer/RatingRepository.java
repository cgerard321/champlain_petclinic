package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RatingRepository extends ReactiveMongoRepository<Rating, String> {
    Flux<Rating> findAllByVetId(String vetId);
    Mono<Rating> findByRatingId(String ratingId);
    Mono<Rating> findByVetIdAndRatingId(String vetId, String ratingId);

}