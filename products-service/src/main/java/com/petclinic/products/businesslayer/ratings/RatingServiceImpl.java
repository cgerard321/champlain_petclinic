package com.petclinic.products.businesslayer.ratings;

import com.petclinic.products.datalayer.ratings.ReviewSettingsRepository;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.datalayer.ratings.ReviewLikeRepository;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import com.petclinic.products.presentationlayer.ratings.ReviewLikeResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.RatingAlreadyExists;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final ReviewSettingsRepository reviewSettingsRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, ProductRepository productRepository,
                             ReviewLikeRepository reviewLikeRepository, ReviewSettingsRepository reviewSettingsRepository) {
        this.ratingRepository = ratingRepository;
        this.productRepository = productRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.reviewSettingsRepository = reviewSettingsRepository;
    }

    @Override
    public Flux<RatingResponseModel> getAllRatingsForProductId(String productId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .thenMany(ratingRepository.findRatingsByProductId(productId)
                        .flatMapSequential(rating -> reviewLikeRepository
                                .countByProductIdAndReviewCustomerId(productId, rating.getCustomerId())
                                .defaultIfEmpty(0L)
                                .map(likeCount -> {
                                    RatingResponseModel response = EntityModelUtil.toRatingResponseModel(rating);
                                    response.setLikeCount(likeCount);
                                    return response;
                                })));
    }

    @Override
    public Flux<RatingResponseModel> getAllRatingsForProductId(String productId, String viewingCustomerId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .thenMany(ratingRepository.findRatingsByProductId(productId)
                        .flatMapSequential(rating -> Mono.zip(
                                        reviewLikeRepository.countByProductIdAndReviewCustomerId(
                                                productId, rating.getCustomerId()).defaultIfEmpty(0L),
                                        reviewLikeRepository.existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
                                                productId, rating.getCustomerId(), viewingCustomerId).defaultIfEmpty(false))
                                .map(likeDetails -> {
                                    RatingResponseModel response = EntityModelUtil.toRatingResponseModel(rating);
                                    response.setLikeCount(likeDetails.getT1());
                                    response.setLikedByCurrentUser(likeDetails.getT2());
                                    return response;
                                })));
    }

    @Override
    public Mono<RatingResponseModel> getRatingForProductIdWithCustomerId(String productId, String customerId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .then(ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Rating not found with associated customerId " + customerId + " and productId " + productId)))
                        .map(EntityModelUtil::toRatingResponseModel)
                );
    }

    @Override
    public Mono<RatingResponseModel> addRatingForProduct(String productId, String customerId, Mono<RatingRequestModel> requestModel) {
        return requestModel
                .filter(req -> req.getRating() != null)
                .switchIfEmpty(Mono.error(new InvalidInputException("Rating must be provided")))
                .filter(req -> req.getRating() > 0 && req.getRating() <= 5)
                .switchIfEmpty(Mono.error(new InvalidInputException("Rating must be between 1 and 5")))
                .filter(req -> req.getReview() == null || req.getReview().length() <= 10000)
                .switchIfEmpty(Mono.error(new InvalidInputException("Review cannot exceed 10000 characters")))
                .flatMap(req ->
                        productRepository.findProductByProductId(productId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                                .flatMap(prod -> ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                                        .flatMap(existingRating ->
                                                Mono.<Rating>error(new RatingAlreadyExists("Rating already exists for customer " + customerId + " for product " + productId))
                                        )
                                        .switchIfEmpty(
                                                ratingRepository.save(
                                                        EntityModelUtil.toRatingEntity(req, productId, customerId)
                                                )
                                        )
                                        .map(EntityModelUtil::toRatingResponseModel)
                                )
                );
    }

    @Override
    public Mono<RatingResponseModel> updateRatingForProduct(String productId, String customerId, Mono<RatingRequestModel> requestModel) {
        return requestModel
                .filter(req -> req.getRating() != null)
                .switchIfEmpty(Mono.error(new InvalidInputException("Rating must be provided")))
                .filter(req -> req.getRating() > 0 && req.getRating() <= 5)
                .switchIfEmpty(Mono.error(new InvalidInputException("Rating must be between 1 and 5")))
                .filter(req -> req.getReview() == null || req.getReview().length() <= 10000)
                .switchIfEmpty(Mono.error(new InvalidInputException("Review cannot exceed 10000 characters")))
                .flatMap(req ->
                        productRepository.findProductByProductId(productId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                                .flatMap(prod ->
                                        ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                                )
                                        .switchIfEmpty(Mono.error(new NotFoundException("Rating could not be found for customer " + customerId + " for product " + productId)))
                                        .flatMap(existingRating ->
                                           reviewSettingsRepository.findById(1L)
                                                   .switchIfEmpty(Mono.error(new InvalidInputException("Review settings are unavailable")))
                                                   .flatMap(settings -> {
                                                       if (existingRating.getCreatedAt() == null) {
                                                           return Mono.error(new InvalidInputException("Review creation time is unavailable"));
                                                       }
                                                       if (settings.getEditPeriodHours() == null || settings.getEditPeriodHours() < 0) {
                                                           return Mono.error(new InvalidInputException("Edit period must be zero or greater"));
                                                       }
                                                       LocalDateTime deadline = existingRating.getCreatedAt()
                                                               .plusHours(settings.getEditPeriodHours());
                                                       if (LocalDateTime.now().isAfter(deadline)){
                                                           return Mono.error(new InvalidInputException("This review is no longer editable")
                                                           );
                                                       }
                                                       existingRating.setRating(req.getRating());
                                                       existingRating.setReview(req.getReview() == null ? "" : req.getReview()
                                                       );
                                                       return ratingRepository.save(existingRating);
                                                   })
                                        )
                                )
                                .map(EntityModelUtil::toRatingResponseModel);
    }

    @Override
    public Mono<RatingResponseModel> deleteRatingForProduct(String productId, String customerId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .then(ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Rating not found with associated customerId " + customerId + " and productId " + productId)))
                        .flatMap(rating ->
                                reviewLikeRepository.deleteByProductIdAndReviewCustomerId(productId, customerId)
                                        .then(ratingRepository.delete(rating))
                                        .then(Mono.just(rating))
                        )
                        .map(EntityModelUtil::toRatingResponseModel)
                );
    }

    @Override
    public Mono<ReviewLikeResponseModel> likeReview(String productId, String reviewCustomerId, String likingCustomerId) {
        if (reviewCustomerId.equals(likingCustomerId)) {
            return Mono.error(new InvalidInputException("Customers cannot like their own review"));
        }

        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .then(ratingRepository.findRatingByCustomerIdAndProductId(reviewCustomerId, productId)
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "Rating not found with associated customerId " + reviewCustomerId +
                                        " and productId " + productId))))
                .then(reviewLikeRepository.existsByProductIdAndReviewCustomerIdAndLikingCustomerId(
                        productId, reviewCustomerId, likingCustomerId))
                .flatMap(alreadyLiked -> {
                    Mono<Void> updateLike = alreadyLiked
                            ? reviewLikeRepository.deleteByProductIdAndReviewCustomerIdAndLikingCustomerId(
                                    productId, reviewCustomerId, likingCustomerId)
                            : reviewLikeRepository.addIfAbsent(productId, reviewCustomerId, likingCustomerId).then();

                    return updateLike
                            .then(reviewLikeRepository.countByProductIdAndReviewCustomerId(productId, reviewCustomerId))
                            .map(count -> ReviewLikeResponseModel.builder()
                                    .likeCount(count)
                                    .likedByCurrentUser(!alreadyLiked)
                                    .build());
                });
    }
}
