package com.petclinic.products.businesslayer.ratings;

import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.RatingAlreadyExists;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RatingServiceImpl implements RatingService{
    private RatingRepository ratingRepository;
    private ProductRepository productRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, ProductRepository productRepository){
        this.ratingRepository = ratingRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Flux<RatingResponseModel> getAllRatingsForProductId(String productId){
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .thenMany(ratingRepository.findRatingsByProductId(productId)
                        .map(EntityModelUtil::toRatingResponseModel)
                );
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
                .filter(req -> req.getReview() == null || req.getReview().length() < 2000)
                .switchIfEmpty(Mono.error(new InvalidInputException("Review must be less than 2000 characters")))
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
                .filter(req -> req.getReview() == null || req.getReview().length() < 2000)
                .switchIfEmpty(Mono.error(new InvalidInputException("Review must be less than 2000 characters")))
                .flatMap(req ->
                        productRepository.findProductByProductId(productId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                                .flatMap(prod -> ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                                        .switchIfEmpty(Mono.error(new NotFoundException("Rating could not be found for customer " + customerId + " for product " + productId)))
                                        .flatMap(found -> {
                                            Rating newRating = EntityModelUtil.toRatingEntity(req, productId, customerId);
                                            newRating.setId(found.getId());
                                            return ratingRepository.save(newRating);
                                        })
                                )
                                .map(EntityModelUtil::toRatingResponseModel)
                );
    }

    @Override
    public Mono<RatingResponseModel> deleteRatingForProduct(String productId, String customerId) {
        return productRepository.findProductByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                .then(ratingRepository.findRatingByCustomerIdAndProductId(customerId, productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Rating not found with associated customerId " + customerId + " and productId " + productId)))
                        .flatMap(rating -> ratingRepository.delete(rating)
                                .then(Mono.just(rating))
                        )
                        .map(EntityModelUtil::toRatingResponseModel)
                );
    }
}
