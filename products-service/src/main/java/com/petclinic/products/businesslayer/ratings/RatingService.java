package com.petclinic.products.businesslayer.ratings;

import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import com.petclinic.products.presentationlayer.ratings.ReviewLikeResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {
    Flux<RatingResponseModel> getAllRatingsForProductId(String productId);
    Flux<RatingResponseModel> getAllRatingsForProductId(String productId, String viewingCustomerId);
    Mono<RatingResponseModel> getRatingForProductIdWithCustomerId(String productId, String customerId);
    Mono<RatingResponseModel> addRatingForProduct(String productId, String customerId, Mono<RatingRequestModel> requestModel);
    Mono<RatingResponseModel> updateRatingForProduct(String productId, String customerId, Mono<RatingRequestModel> requestModel);
    Mono<RatingResponseModel> deleteRatingForProduct(String productId, String customerId);
    Mono<ReviewLikeResponseModel> likeReview(String productId, String reviewCustomerId, String likingCustomerId);
}
