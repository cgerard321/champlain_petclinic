package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import com.petclinic.bffapigateway.dtos.Ratings.ReviewLikeResponseModel;
import com.petclinic.bffapigateway.dtos.Ratings.ReviewSettingsModel;
import com.petclinic.bffapigateway.dtos.Ratings.ReviewLikeResponseModel;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RatingsServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String baseURL;
    private final String settingsURL;

    public RatingsServiceClient(
            WebClient.Builder wcb,
            @Value("${app.products-service.host}") String host,
            @Value("${app.products-service.port}") String port
    ){
        this.webClientBuilder = wcb;
        this.baseURL = "http://" + host + ":" + port + "/ratings";
        this.settingsURL = "http://" + host + ":" + port + "/review-settings";
    }

    public Flux<RatingResponseModel> getAllRatingsForProductId(final String productId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + productId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(RatingResponseModel.class);
    }

    public Flux<RatingResponseModel> getAllRatingsForProductIdAndCustomerId(
            final String productId, final String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + productId + "/for-user/" + customerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(RatingResponseModel.class);
    }

    public Mono<RatingResponseModel> getRatingForProductIdAndCustomerId(final String productId, final String customerId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + productId + "/" + customerId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError, clientResponse -> {
                        if(clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.empty();
                        }
                        return Mono.error(new GenericHttpException("Client error while fetching ratings", HttpStatus.BAD_REQUEST));
                    }
                )
                .bodyToMono(RatingResponseModel.class)
                .defaultIfEmpty(RatingResponseModel.builder().rating((byte) 0).build());
    }

    public Mono<RatingResponseModel> addRatingForProductIdAndCustomerId(final String productId, final String customerId, final Mono<RatingRequestModel> requestModel){
        return webClientBuilder.build()
                .post()
                .uri(baseURL + "/" + productId + "/" + customerId)
                .body(requestModel, RatingRequestModel.class)
                .retrieve()
                .bodyToMono(RatingResponseModel.class);
    }

    public Mono<RatingResponseModel> updateRatingForProductIdAndCustomerId(final String productId, final String customerId, final Mono<RatingRequestModel> requestModel){
        return webClientBuilder.build()
                .put()
                .uri(baseURL + "/" + productId + "/" + customerId)
                .body(requestModel, RatingResponseModel.class)
                .retrieve()
                .bodyToMono(RatingResponseModel.class);
    }

    public Mono<RatingResponseModel> deleteRatingFromProductIdAssociatedToCustomerId(final String productId, final String customerId){
        return webClientBuilder.build()
                .delete()
                .uri(baseURL + "/" + productId + "/" + customerId)
                .retrieve()
                .bodyToMono(RatingResponseModel.class);
    }

    public Mono<ReviewLikeResponseModel> likeReview(final String productId,
                                                     final String reviewCustomerId,
                                                     final String likingCustomerId) {
        return webClientBuilder.build()
                .post()
                .uri(baseURL + "/" + productId + "/" + reviewCustomerId + "/likes/" + likingCustomerId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, clientResponse -> {
                            HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
                            String message = status == HttpStatus.NOT_FOUND
                                    ? "Review not found"
                                    : "Could not like review";
                            return Mono.error(new GenericHttpException(message, status));
                        }
                )
                .bodyToMono(ReviewLikeResponseModel.class);
    }

    public Mono<ReviewSettingsModel> getReviewSettings(){
        return webClientBuilder.build()
                .get()
                .uri(settingsURL)
                .retrieve()
                .bodyToMono(ReviewSettingsModel.class);
    }

    public Mono<ReviewSettingsModel> updateReviewSettings(ReviewSettingsModel settings){
        return webClientBuilder.build()
                .put()
                .uri(settingsURL)
                .bodyValue(settings)
                .retrieve()
                .bodyToMono(ReviewSettingsModel.class);
    }

    public Mono<ReviewLikeResponseModel> likeReview(final String productId,
                                                     final String reviewCustomerId,
                                                     final String likingCustomerId) {
        return webClientBuilder.build()
                .post()
                .uri(baseURL + "/" + productId + "/" + reviewCustomerId + "/likes/" + likingCustomerId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, clientResponse -> {
                            HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
                            String message = status == HttpStatus.NOT_FOUND
                                    ? "Review not found"
                                    : "Could not like review";
                            return Mono.error(new GenericHttpException(message, status));
                        }
                )
                .bodyToMono(ReviewLikeResponseModel.class);
    }
}
