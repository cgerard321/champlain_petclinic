package com.petclinic.bffapigateway.domainclientlayer;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.petclinic.bffapigateway.dtos.Ratings.RatingRequestModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@WireMockTest(httpPort = 7007)
class RatingsServiceClientIntegrationTest {
    @Autowired
    private RatingsServiceClient ratingsServiceClient;

    @Test
    void getRatingForProductIdAndCustomerId() {
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        stubFor(get(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\": 5}"))
        );

        StepVerifier.create(ratingsServiceClient.getRatingForProductIdAndCustomerId(productId, customerId))
                .assertNext(ratingResponseModel -> assertEquals((byte) 5, ratingResponseModel.getRating()))
                .verifyComplete();
    }

    @Test
    void addRatingForProductIdAndCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 3)
                .build();

        stubFor(post(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(equalToJson("{\"rating\": " + requestModel.getRating() + "}"))
                .willReturn(created()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("{\"rating\":" + requestModel.getRating() + "}")
                )
        );

        StepVerifier.create(ratingsServiceClient.addRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> assertEquals(requestModel.getRating(), ratingResponseModel.getRating()))
                .verifyComplete();
    }

    @Test
    void updateRatingForProductIdAndCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 3)
                .build();

        stubFor(put(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(equalToJson("{\"rating\": " + requestModel.getRating() + "}"))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\":" + requestModel.getRating() + "}"))
        );

        StepVerifier.create(ratingsServiceClient.updateRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> assertEquals(requestModel.getRating(), ratingResponseModel.getRating()))
                .verifyComplete();
    }

    @Test
    void deleteRatingFromProductIdAssociatedToCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        stubFor(delete(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\": 5}"))
        );

        StepVerifier.create(ratingsServiceClient.deleteRatingFromProductIdAssociatedToCustomerId(productId, customerId))
                .assertNext(ratingResponseModel -> assertEquals((byte) 5, ratingResponseModel.getRating()))
                .verifyComplete();
    }
}