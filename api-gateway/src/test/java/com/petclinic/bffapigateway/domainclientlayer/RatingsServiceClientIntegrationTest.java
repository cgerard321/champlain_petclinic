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
    void getAllRatingsForProductId() {
        String productId = UUID.randomUUID().toString();
        stubFor(get(urlEqualTo("/ratings/%s".formatted(productId)))
                .willReturn(okForContentType(MediaType.TEXT_EVENT_STREAM.toString(), "data:{\"rating\": 5, \"review\": \"It's great\"}\n\ndata: {\"rating\": 1, \"review\": \"Horrible\"}\n\n"))
        );

        StepVerifier.create(ratingsServiceClient.getAllRatingsForProductId(productId))
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 5, ratingResponseModel.getRating());
                    assertEquals("It's great", ratingResponseModel.getReview());
                })
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 1, ratingResponseModel.getRating());
                    assertEquals("Horrible", ratingResponseModel.getReview());
                })
                .verifyComplete();
    }

    @Test
    void getRatingForProductIdAndCustomerId() {
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        stubFor(get(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\": 5, \"review\": \"It's great\"}"))
        );

        StepVerifier.create(ratingsServiceClient.getRatingForProductIdAndCustomerId(productId, customerId))
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 5, ratingResponseModel.getRating());
                    assertEquals("It's great", ratingResponseModel.getReview());
                })
                .verifyComplete();
    }

    @Test
    void addRatingForProductIdAndCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 3)
                .review("It's not bad neither good")
                .build();

        stubFor(post(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(matchingJsonSchema("{\"rating\": " + requestModel.getRating() + "}"))
                .willReturn(created()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("{\"rating\":" + requestModel.getRating() + ", \"review\": \"" + requestModel.getReview() + "\"}")
                )
        );

        StepVerifier.create(ratingsServiceClient.addRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> {
                    assertEquals(requestModel.getRating(), ratingResponseModel.getRating());
                    assertEquals(requestModel.getReview(), ratingResponseModel.getReview());
                })
                .verifyComplete();
    }

    @Test
    void updateRatingForProductIdAndCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        RatingRequestModel requestModel = RatingRequestModel.builder()
                .rating((byte) 3)
                .review("It's not bad neither good")
                .build();

        stubFor(put(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(matchingJsonSchema("{\"rating\": " + requestModel.getRating() + "}"))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\":" + requestModel.getRating() + ", \"review\": \"" + requestModel.getReview() + "\"}"))
        );

        StepVerifier.create(ratingsServiceClient.updateRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> {
                    assertEquals(requestModel.getRating(), ratingResponseModel.getRating());
                    assertEquals(requestModel.getReview(), ratingResponseModel.getReview());
                })
                .verifyComplete();
    }

    @Test
    void deleteRatingFromProductIdAssociatedToCustomerId(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        stubFor(delete(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\": 5, \"review\": \"It's great\"}"))
        );

        StepVerifier.create(ratingsServiceClient.deleteRatingFromProductIdAssociatedToCustomerId(productId, customerId))
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 5, ratingResponseModel.getRating());
                    assertEquals("It's great", ratingResponseModel.getReview());
                })
                .verifyComplete();
    }
}