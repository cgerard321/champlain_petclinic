package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.RatingsServiceClient;
import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import com.petclinic.bffapigateway.exceptions.InvalidCredentialsException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/ratings")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class RatingController {
    private final RatingsServiceClient ratingsServiceClient;
    private final AuthServiceClient authServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RatingResponseModel>> getRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @CookieValue("Bearer") String jwt
    ){
        return authServiceClient.verifyUser(jwt)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Could not verify user")))
                .flatMap(user -> ratingsServiceClient.getRatingForProductIdAndCustomerId(productId, user.getBody().getUserId()))
                .map(ResponseEntity::ok);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PostMapping(
            value = "/{productId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> addRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @RequestBody Mono<RatingRequestModel> requestModel,
            @CookieValue("Bearer") String jwt
    ){
        return authServiceClient.verifyUser(jwt)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Could not verify user")))
                .flatMap(user -> ratingsServiceClient.addRatingForProductIdAndCustomerId(productId, user.getBody().getUserId(), requestModel))
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PutMapping(
            value = "/{productId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<RatingResponseModel>> updateRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @RequestBody Mono<RatingRequestModel> requestModel,
            @CookieValue("Bearer") String jwt
    ){
        return authServiceClient.verifyUser(jwt)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Could not verify user")))
                .flatMap(user -> ratingsServiceClient.updateRatingForProductIdAndCustomerId(productId, user.getBody().getUserId(), requestModel))
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RatingResponseModel>> deleteRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @CookieValue("Bearer") String jwt
    ){
        return authServiceClient.verifyUser(jwt)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Could not verify user")))
                .flatMap(user -> ratingsServiceClient.deleteRatingFromProductIdAssociatedToCustomerId(productId, user.getBody().getUserId()))
                .map(ResponseEntity::ok);
    }
}
