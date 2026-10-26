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
                .willReturn(okForContentType(MediaType.TEXT_EVENT_STREAM.toString(), "data:{\"rating\": 5, \"review\": \"It's great\", \"qualityRating\": 5, \"valueRating\": 4, \"petSatisfactionRating\": 5, \"ingredientsRating\": 4}\n\ndata: {\"rating\": 1, \"review\": \"Horrible\", \"qualityRating\": 2, \"valueRating\": 1, \"petSatisfactionRating\": 1, \"ingredientsRating\": 2}\n\n"))
        );

        StepVerifier.create(ratingsServiceClient.getAllRatingsForProductId(productId))
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 5, ratingResponseModel.getRating());
                    assertEquals("It's great", ratingResponseModel.getReview());
                    assertEquals((byte) 5, ratingResponseModel.getQualityRating());
                    assertEquals((byte) 4, ratingResponseModel.getValueRating());
                    assertEquals((byte) 5, ratingResponseModel.getPetSatisfactionRating());
                    assertEquals((byte) 4, ratingResponseModel.getIngredientsRating());
                })
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 1, ratingResponseModel.getRating());
                    assertEquals("Horrible", ratingResponseModel.getReview());
                    assertEquals((byte) 2, ratingResponseModel.getQualityRating());
                    assertEquals((byte) 1, ratingResponseModel.getValueRating());
                    assertEquals((byte) 1, ratingResponseModel.getPetSatisfactionRating());
                    assertEquals((byte) 2, ratingResponseModel.getIngredientsRating());
                })
                .verifyComplete();
    }

    @Test
    void getRatingForProductIdAndCustomerId() {
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        stubFor(get(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\": 5, \"review\": \"It's great\", \"qualityRating\": 5, \"valueRating\": 4, \"petSatisfactionRating\": 5, \"ingredientsRating\": 4}"))
        );

        StepVerifier.create(ratingsServiceClient.getRatingForProductIdAndCustomerId(productId, customerId))
                .assertNext(ratingResponseModel -> {
                    assertEquals((byte) 5, ratingResponseModel.getRating());
                    assertEquals("It's great", ratingResponseModel.getReview());
                    assertEquals((byte) 5, ratingResponseModel.getQualityRating());
                    assertEquals((byte) 4, ratingResponseModel.getValueRating());
                    assertEquals((byte) 5, ratingResponseModel.getPetSatisfactionRating());
                    assertEquals((byte) 4, ratingResponseModel.getIngredientsRating());
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        stubFor(post(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(matchingJsonPath("$.rating", equalTo("3")))
                .withRequestBody(matchingJsonPath("$.qualityRating", equalTo("5")))
                .withRequestBody(matchingJsonPath("$.valueRating", equalTo("4")))
                .withRequestBody(matchingJsonPath("$.petSatisfactionRating", equalTo("5")))
                .withRequestBody(matchingJsonPath("$.ingredientsRating", equalTo("4")))
                .willReturn(created()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBody("{\"rating\":" + requestModel.getRating() + ", \"review\": \"" + requestModel.getReview() + "\", \"qualityRating\": 5, \"valueRating\": 4, \"petSatisfactionRating\": 5, \"ingredientsRating\": 4}")
                )
        );

        StepVerifier.create(ratingsServiceClient.addRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> {
                    assertEquals(requestModel.getRating(), ratingResponseModel.getRating());
                    assertEquals(requestModel.getReview(), ratingResponseModel.getReview());
                    assertEquals(requestModel.getQualityRating(), ratingResponseModel.getQualityRating());
                    assertEquals(requestModel.getValueRating(), ratingResponseModel.getValueRating());
                    assertEquals(requestModel.getPetSatisfactionRating(), ratingResponseModel.getPetSatisfactionRating());
                    assertEquals(requestModel.getIngredientsRating(), ratingResponseModel.getIngredientsRating());
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
                .qualityRating((byte) 5)
                .valueRating((byte) 4)
                .petSatisfactionRating((byte) 5)
                .ingredientsRating((byte) 4)
                .build();

        stubFor(put(urlEqualTo("/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(matchingJsonPath("$.rating", equalTo("3")))
                .withRequestBody(matchingJsonPath("$.qualityRating", equalTo("5")))
                .withRequestBody(matchingJsonPath("$.valueRating", equalTo("4")))
                .withRequestBody(matchingJsonPath("$.petSatisfactionRating", equalTo("5")))
                .withRequestBody(matchingJsonPath("$.ingredientsRating", equalTo("4")))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON.toString(), "{\"rating\":" + requestModel.getRating() + ", \"review\": \"" + requestModel.getReview() + "\", \"qualityRating\": 5, \"valueRating\": 4, \"petSatisfactionRating\": 5, \"ingredientsRating\": 4}"))
        );

        StepVerifier.create(ratingsServiceClient.updateRatingForProductIdAndCustomerId(productId, customerId, Mono.just(requestModel)))
                .assertNext(ratingResponseModel -> {
                    assertEquals(requestModel.getRating(), ratingResponseModel.getRating());
                    assertEquals(requestModel.getReview(), ratingResponseModel.getReview());
                    assertEquals(requestModel.getQualityRating(), ratingResponseModel.getQualityRating());
                    assertEquals(requestModel.getValueRating(), ratingResponseModel.getValueRating());
                    assertEquals(requestModel.getPetSatisfactionRating(), ratingResponseModel.getPetSatisfactionRating());
                    assertEquals(requestModel.getIngredientsRating(), ratingResponseModel.getIngredientsRating());
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
