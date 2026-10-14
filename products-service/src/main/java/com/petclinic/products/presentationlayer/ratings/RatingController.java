package com.petclinic.products.presentationlayer.ratings;

import com.petclinic.products.businesslayer.ratings.RatingService;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ratings")
@Slf4j
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    private void validateId(String id, String name){
        if(id.length() != 36) {
            throw new InvalidInputException("Provided " + name + " id is invalid: " + id);
        }
    }

    @GetMapping(
            value = "/{productId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<RatingResponseModel> getAllRatingsForProductId(@PathVariable String productId){
        validateId(productId, "product");
        return Mono.just(productId)
                .thenMany(ratingService.getAllRatingsForProductId(productId));
    }

    @GetMapping(
            value = "/{productId}/for-user/{customerId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<RatingResponseModel> getAllRatingsForProductIdAndCustomer(
            @PathVariable String productId,
            @PathVariable String customerId
    ) {
        validateId(productId, "product");
        validateId(customerId, "customer");
        return ratingService.getAllRatingsForProductId(productId, customerId);
    }

    @GetMapping(
            value="/{productId}/{customerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> getRatingForProductByCustomer(
            @PathVariable String productId,
            @PathVariable String customerId
    ) {
        validateId(productId, "product");
        validateId(customerId, "customer");

        return ratingService.getRatingForProductIdWithCustomerId(productId, customerId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(
            value = "/{productId}/{customerId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> addRatingForProductByCustomer(
            @PathVariable String productId,
            @PathVariable String customerId,
            @RequestBody Mono<RatingRequestModel> requestModel
    ) {
        validateId(productId, "product");
        validateId(customerId, "customer");

        return ratingService.addRatingForProduct(productId, customerId, requestModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(
            value = "/{productId}/{customerId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> updateRatingForProductByCustomer(
            @PathVariable String productId,
            @PathVariable String customerId,
            @RequestBody Mono<RatingRequestModel> requestModel
    ){
        validateId(productId, "product");
        validateId(customerId, "customer");

        return ratingService.updateRatingForProduct(productId, customerId, requestModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(
            value = "/{productId}/{customerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> deleteRatingForProductByCustomer(
            @PathVariable String productId,
            @PathVariable String customerId
    ){
        validateId(productId, "product");
        validateId(customerId, "customer");

        return ratingService.deleteRatingForProduct(productId, customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping(
            value = "/{productId}/{reviewCustomerId}/likes/{likingCustomerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ReviewLikeResponseModel>> likeReview(
            @PathVariable String productId,
            @PathVariable String reviewCustomerId,
            @PathVariable String likingCustomerId
    ) {
        validateId(productId, "product");
        validateId(reviewCustomerId, "review customer");
        validateId(likingCustomerId, "liking customer");

        return ratingService.likeReview(productId, reviewCustomerId, likingCustomerId)
                .map(ResponseEntity::ok);
    }
}
