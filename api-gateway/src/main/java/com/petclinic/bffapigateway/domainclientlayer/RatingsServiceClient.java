package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RatingsServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String baseURL;

    public RatingsServiceClient(
            WebClient.Builder wcb,
            @Value("${app.products-service.host}") String host,
            @Value("${app.products-service.port}") String port
    ){
        this.webClientBuilder = wcb;
        this.baseURL = "http://" + host + ":" + port + "/api/v1/ratings";
    }

    public Mono<RatingResponseModel> getRatingForProductIdAndCustomerId(final String productId, final String customerId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + productId + "/" + customerId)
                .retrieve()
                .bodyToMono(RatingResponseModel.class);
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
}
