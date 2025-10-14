package com.petclinic.products.presentationlayer.ratings;

import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.businesslayer.ratings.RatingService;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.print.attribute.standard.Media;

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
}
