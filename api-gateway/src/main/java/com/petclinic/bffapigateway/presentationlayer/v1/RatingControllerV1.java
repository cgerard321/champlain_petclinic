package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.RatingsServiceClient;
import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import com.petclinic.bffapigateway.exceptions.InvalidCredentialsException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/gateway/ratings")
@Validated
@Slf4j
public class RatingControllerV1 {
    private final RatingsServiceClient ratingsServiceClient;
    private final AuthServiceClient authServiceClient;

    private class UserCache{
        private String userId;
        private long lastUsed;

        public UserCache(String userId){
            this.userId = userId;
            this.lastUsed = System.currentTimeMillis();
        }

        public String getUserId() {
            this.lastUsed = System.currentTimeMillis();
            return userId;
        }

        public long getLastUsed() {
            return lastUsed;
        }
    }

    // Ghetto User Cache
    private static HashMap<String, UserCache> jwtUserCache = new HashMap<>();

    // Caches JWT to a UserID so we don't have to call Auth all the time
    private Mono<String> getFromJWTUserId(String jwt){
        if(jwtUserCache.containsKey(jwt)){
            return Mono.just(jwtUserCache.get(jwt).getUserId());
        }else{
            return authServiceClient.validateToken(jwt)
                    .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
                    .doOnNext(user -> jwtUserCache.put(jwt, new UserCache(user.getBody().getUserId())))
                    .flatMap(u -> Mono.just(u.getBody().getUserId()));
        }
    }

    // Goes through HashMap and cleans if was not used within 5 minutes.
    private void cleanCache(){
        jwtUserCache.entrySet().iterator().forEachRemaining(entry -> {
            if(System.currentTimeMillis() - entry.getValue().getLastUsed() > 300000){
                jwtUserCache.remove(entry.getKey());
            }
        });
    }

    public static void clearCache(){
        jwtUserCache.clear();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/product/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<RatingResponseModel> getAllRatingsForProductId(@PathVariable String productId){
        return ratingsServiceClient.getAllRatingsForProductId(productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RatingResponseModel>> getRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @CookieValue("Bearer") String jwt
    ){
        return getFromJWTUserId(jwt)
                .doOnNext(e -> cleanCache())
                .flatMap(user -> ratingsServiceClient.getRatingForProductIdAndCustomerId(productId, user))
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
        return getFromJWTUserId(jwt)
                .doOnNext(e -> cleanCache())
                .flatMap(user -> ratingsServiceClient.addRatingForProductIdAndCustomerId(productId, user, requestModel))
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
        return getFromJWTUserId(jwt)
                .doOnNext(e -> cleanCache())
                .flatMap(user -> ratingsServiceClient.updateRatingForProductIdAndCustomerId(productId, user, requestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<RatingResponseModel>> deleteRatingForProductIdAndCustomerId(
            @PathVariable String productId,
            @CookieValue("Bearer") String jwt
    ){
        return getFromJWTUserId(jwt)
                .doOnNext(e -> cleanCache())
                .flatMap(user -> ratingsServiceClient.deleteRatingFromProductIdAssociatedToCustomerId(productId, user))
                .map(ResponseEntity::ok);
    }
}
