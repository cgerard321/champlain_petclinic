package com.petclinic.bffapigateway.presentationlayer.v2;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RatingControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private final String jwtToken = "valid-test-token-for-valid-owner-id";

    @RegisterExtension
    static WireMockExtension ratingMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(7007))
            .build();

    @RegisterExtension
    static WireMockExtension authMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(7005))
            .build();

    @Test
    void whenGetRatingByProductId_thenReturnRating(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        ratingMock.stubFor(get(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType("application/json", "{\"rating\": 5}"))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"" + customerId + "\"" +
                        ",\"email\": \"some-email@example.com\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.get()
                .uri("/api/v2/gateway/ratings/{productId}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rating").isEqualTo(5);
    }

    @Test
    void whenAddRatingWithInvalidToken_thenReturnInvalidCredentials(){
        String productId = UUID.randomUUID().toString();

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(unauthorized())
        );

        webTestClient.post()
                .uri("/api/v2/gateway/ratings/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue("{\"rating\": 2}")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid token");
    }

    @Test
    void whenAddRating_thenReturnRating(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        ratingMock.stubFor(post(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(equalToJson("{\"rating\": 3}"))
                .willReturn(okForContentType("application/json", "{\"rating\": 3}"))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"" + customerId + "\"" +
                        ",\"email\": \"some-email@example.com\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.post()
                .uri("/api/v2/gateway/ratings/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue("{\"rating\": 3}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.rating").isEqualTo(3);
    }

    @Test
    void whenUpdateRating_thenReturnRating(){
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        ratingMock.stubFor(put(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .withRequestBody(equalToJson("{\"rating\": 4}"))
                .willReturn(okForContentType("application/json", "{\"rating\": 4}"))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"" + customerId + "\"" +
                        ",\"email\": \"some-email@example.com\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.put()
                .uri("/api/v2/gateway/ratings/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue("{\"rating\": 4}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rating").isEqualTo(4);

    }

    @Test
    void whenDeleteRating_thenReturnRating() {
        String productId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        ratingMock.stubFor(delete(urlEqualTo("/api/v1/ratings/%s/%s".formatted(productId, customerId)))
                .willReturn(okForContentType("application/json", "{\"rating\": 1}"))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"" + customerId + "\"" +
                        ",\"email\": \"some-email@example.com\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.delete()
                .uri("/api/v2/gateway/ratings/{productId}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rating").isEqualTo("1");

    }
}