//package com.petclinic.bffapigateway.presentationlayer.v2.Products;
//
//import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
//import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
//import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
//import com.petclinic.bffapigateway.presentationlayer.v2.RatingController;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.RegisterExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import java.util.UUID;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@AutoConfigureWebTestClient
//class RatingControllerIntegrationTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    private final String jwtToken = "valid-test-token-for-valid-owner-id";
//
//    @RegisterExtension
//    static WireMockExtension ratingMock = WireMockExtension.newInstance()
//            .options(WireMockConfiguration.options().port(7007))
//            .build();
//
//    @RegisterExtension
//    static WireMockExtension authMock = WireMockExtension.newInstance()
//            .options(WireMockConfiguration.options().port(7005))
//            .build();
//
//    @BeforeEach
//    public void resetJWTCache(){
//        RatingController.clearCache();
//    }
//
//    @Test
//    void whenGetRatingsForProductId_thenReturnRatings(){
//        String productId = UUID.randomUUID().toString();
//        String customerId = UUID.randomUUID().toString();
//
//        ratingMock.stubFor(get(urlEqualTo("/api/ratings/%s".formatted(productId)))
//                .willReturn(okForContentType("text/event-stream;charset=UTF-8", "data:{\"rating\": 5, \"review\": \"It's great\"}\n\ndata: {\"rating\": 1, \"review\": \"Horrible\"}\n\n"))
//        );
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(okForContentType("application/json", "{" +
//                        "\"token\": \"" + jwtToken + "\"" +
//                        ",\"userId\": \"" + customerId + "\"" +
//                        ",\"email\": \"some-email@example.com\"" +
//                        ",\"roles\": [\"ALL\"]" +
//                        "}"))
//        );
//
//        webTestClient.get()
//                .uri("/api/gateway/ratings/product/{productId}", productId)
//                .accept(MediaType.TEXT_EVENT_STREAM)
//                .cookie("Bearer", jwtToken)
//                .exchange()
//                .expectHeader().contentType("text/event-stream;charset=UTF-8")
//                .expectStatus().isOk()
//                .expectBodyList(RatingResponseModel.class)
//                .value(ratingResponseModels -> {
//                    assertNotNull(ratingResponseModels);
//                })
//                .hasSize(2);
//
//    }
//
//    @Test
//    void whenGetRatingByProductId_thenReturnRating(){
//        String productId = UUID.randomUUID().toString();
//        String customerId = UUID.randomUUID().toString();
//
//        ratingMock.stubFor(get(urlEqualTo("/api/ratings/%s/%s".formatted(productId, customerId)))
//                .willReturn(okForContentType("application/json", "{\"rating\": 5, \"review\": \"It's great\"}"))
//        );
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(okForContentType("application/json", "{" +
//                        "\"token\": \"" + jwtToken + "\"" +
//                        ",\"userId\": \"" + customerId + "\"" +
//                        ",\"email\": \"some-email@example.com\"" +
//                        ",\"roles\": [\"ALL\"]" +
//                        "}"))
//        );
//
//        webTestClient.get()
//                .uri("/api/gateway/ratings/{productId}", productId)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie("Bearer", jwtToken)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.rating").isEqualTo(5)
//                .jsonPath("$.review").isEqualTo("It's great");
//    }
//
//    @Test
//    void whenAddRatingWithInvalidToken_thenReturnInvalidCredentials(){
//        String productId = UUID.randomUUID().toString();
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(unauthorized())
//        );
//
//        webTestClient.post()
//                .uri("/api/gateway/ratings/{productId}", productId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie("Bearer", jwtToken)
//                .bodyValue("{\"rating\": 2}")
//                .exchange()
//                .expectStatus().isUnauthorized()
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Invalid token");
//    }
//
//    @Test
//    void whenAddRating_thenReturnRating(){
//        String productId = UUID.randomUUID().toString();
//        String customerId = UUID.randomUUID().toString();
//
//        ratingMock.stubFor(post(urlEqualTo("/api/ratings/%s/%s".formatted(productId, customerId)))
//                .withRequestBody(matchingJsonSchema("{\"rating\": 3}"))
//                .willReturn(okForContentType("application/json", "{\"rating\": 3, \"review\": \"It's not bad neither good\"}"))
//        );
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(okForContentType("application/json", "{" +
//                        "\"token\": \"" + jwtToken + "\"" +
//                        ",\"userId\": \"" + customerId + "\"" +
//                        ",\"email\": \"some-email@example.com\"" +
//                        ",\"roles\": [\"ALL\"]" +
//                        "}"))
//        );
//
//        webTestClient.post()
//                .uri("/api/gateway/ratings/{productId}", productId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie("Bearer", jwtToken)
//                .bodyValue("{\"rating\": 3}")
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody()
//                .jsonPath("$.rating").isEqualTo(3)
//                .jsonPath("$.review").isEqualTo("It's not bad neither good");
//    }
//
//    @Test
//    void whenUpdateRating_thenReturnRating(){
//        String productId = UUID.randomUUID().toString();
//        String customerId = UUID.randomUUID().toString();
//
//        ratingMock.stubFor(put(urlEqualTo("/api/ratings/%s/%s".formatted(productId, customerId)))
//                .withRequestBody(matchingJsonSchema("{\"rating\": 4}"))
//                .willReturn(okForContentType("application/json", "{\"rating\": 4, \"review\": \"It's alright\"}"))
//        );
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(okForContentType("application/json", "{" +
//                        "\"token\": \"" + jwtToken + "\"" +
//                        ",\"userId\": \"" + customerId + "\"" +
//                        ",\"email\": \"some-email@example.com\"" +
//                        ",\"roles\": [\"ALL\"]" +
//                        "}"))
//        );
//
//        webTestClient.put()
//                .uri("/api/v2/gateway/ratings/{productId}", productId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie("Bearer", jwtToken)
//                .bodyValue("{\"rating\": 4}")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.rating").isEqualTo(4)
//                .jsonPath("$.review").isEqualTo("It's alright");
//
//    }
//
//    @Test
//    void whenDeleteRating_thenReturnRating() {
//        String productId = UUID.randomUUID().toString();
//        String customerId = UUID.randomUUID().toString();
//
//        ratingMock.stubFor(delete(urlEqualTo("/api/ratings/%s/%s".formatted(productId, customerId)))
//                .willReturn(okForContentType("application/json", "{\"rating\": 1, \"review\": \"Horrible\"}"))
//        );
//
//        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
//                .withCookie("Bearer", equalTo(jwtToken))
//                .willReturn(okForContentType("application/json", "{" +
//                        "\"token\": \"" + jwtToken + "\"" +
//                        ",\"userId\": \"" + customerId + "\"" +
//                        ",\"email\": \"some-email@example.com\"" +
//                        ",\"roles\": [\"ALL\"]" +
//                        "}"))
//        );
//
//        webTestClient.delete()
//                .uri("/api/gateway/ratings/{productId}", productId)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie("Bearer", jwtToken)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.rating").isEqualTo("1")
//                .jsonPath("$.review").isEqualTo("Horrible");
//
//    }
//}