package com.petclinic.products.datalayer.ratings;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReviewLikeRepository extends ReactiveCrudRepository<ReviewLike, Long> {

    @Query("""
            INSERT INTO review_likes (product_id, review_customer_id, liking_customer_id)
            VALUES (:productId, :reviewCustomerId, :likingCustomerId)
            ON CONFLICT (product_id, review_customer_id, liking_customer_id) DO NOTHING
            RETURNING *
            """)
    Mono<ReviewLike> addIfAbsent(String productId, String reviewCustomerId, String likingCustomerId);

    Mono<Long> countByProductIdAndReviewCustomerId(String productId, String reviewCustomerId);

    Mono<Boolean> existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
            String productId, String reviewCustomerId, String likingCustomerId);

    Mono<Void> deleteByProductIdAndReviewCustomerIdAndLikingCustomerId(
            String productId, String reviewCustomerId, String likingCustomerId);

    Mono<Void> deleteByProductIdAndReviewCustomerId(String productId, String reviewCustomerId);

    Mono<Void> deleteByProductId(String productId);
}
