package com.petclinic.products.datalayer.ratings;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingRepository extends ReactiveMongoRepository<Rating, String>{
    Flux<Rating> findRatingsByProductId(String productId);
    Mono<Rating> findRatingByCustomerIdAndProductId(String customerId, String productId);
    Flux<Rating> deleteRatingsByProductId(String productId);
}
