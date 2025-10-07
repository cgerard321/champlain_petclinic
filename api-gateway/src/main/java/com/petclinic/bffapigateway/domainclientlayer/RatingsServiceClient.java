package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
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

    public RatingsServiceClient(
            WebClient.Builder wcb,
            @Value("${app.products-service.host}") String host,
            @Value("${app.products-service.port}") String port
    ){
        this.webClientBuilder = wcb;
        this.baseURL = "http://" + host + ":" + port + "/ratings";
    }

    public Flux<RatingResponseModel> getAllRatingsForProductId(final String productId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + productId)
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
}
