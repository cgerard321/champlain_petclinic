package com.petclinic.visits.visitsservicenew.DataLayer.Review;

import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReviewRepository extends ReactiveMongoRepository<Review, String> {

    Mono<Review> findReviewByReviewId(String reviewId);
}
