package com.petclinic.products.datalayer.ratings;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingRepository extends R2dbcRepository<Rating, Long> {
    Flux<Rating> findRatingsByProductId(String productId);

    Mono<Rating> findRatingByCustomerIdAndProductId(String customerId, String productId);

    @Query("DELETE FROM product_ratings WHERE product_id = :productId RETURNING *")
    Flux<Rating> deleteRatingsByProductId(String productId);
}
