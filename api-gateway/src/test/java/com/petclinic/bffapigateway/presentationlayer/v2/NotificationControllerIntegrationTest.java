package com.petclinic.bffapigateway.presentationlayer.v2;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationRequestModel;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationResponseModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class NotificationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private final String jwtToken = "valid-test-token-for-valid-owner-id";

    @RegisterExtension
    static WireMockExtension notificationMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(7007))
            .build();

    @RegisterExtension
    static WireMockExtension authMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.options().port(7005))
            .build();

    @BeforeEach
    public void resetJWTCache() {
        NotificationController.clearCache();
    }

    @Test
    void whenGetNotifications_thenReturnNotifications() {
        String customerId = UUID.randomUUID().toString();
        String productName = "Test Product";
        String email = "some-email@example.com";
        String productId = UUID.randomUUID().toString();

        notificationMock.stubFor(get(urlEqualTo("/api/v1/notifications/%s".formatted(customerId)))
                .willReturn(okForContentType("application/json", "[{\"productId\": \"%s\", \"productName\": \"%s\", \"email\": \"%s\", \"notificationType\": [\"PRICE\", \"QUANTITY\"]}]"
                        .formatted(productId, productName, email)))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"" + customerId + "\"" +
                        ",\"email\": \"" + email + "\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.get()
                .uri("/api/v2/gateway/notifications/for-customer")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationResponseModel.class)
                .value(notificationResponseModels -> {
                    assertNotNull(notificationResponseModels);
                })
                .hasSize(1);
    }

    @Test
    void whenAddNotification_thenReturnNotification() {
        String productId = UUID.randomUUID().toString();
        String email = "some-email@example.com";

        NotificationRequestModel request = new NotificationRequestModel();
        request.setNotificationType(Set.of("PRICE")); // or Set.of("QUANTITY")
        request.setEmail(email);

        notificationMock.stubFor(post(urlEqualTo("/api/v1/notifications/%s/%s".formatted("someCustomerId", productId)))
                .withRequestBody(matchingJsonSchema("{\"notificationType\": [\"PRICE\"], \"email\": \"" + email + "\"}"))
                .willReturn(okForContentType("application/json", "{\"productId\": \"%s\", \"productName\": \"Test Product\", \"email\": \"%s\", \"notificationType\": [\"PRICE\"]}"
                        .formatted(productId, email)))
        );

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(okForContentType("application/json", "{" +
                        "\"token\": \"" + jwtToken + "\"" +
                        ",\"userId\": \"someCustomerId\"" +
                        ",\"email\": \"" + email + "\"" +
                        ",\"roles\": [\"ALL\"]" +
                        "}"))
        );

        webTestClient.post()
                .uri("/api/v2/gateway/notifications/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.notificationType").isEqualTo("PRICE");
    }

    @Test
    void whenAddNotificationWithInvalidToken_thenReturnInvalidCredentials() {
        String productId = UUID.randomUUID().toString();

        authMock.stubFor(post(urlEqualTo("/users/validate-token"))
                .withCookie("Bearer", equalTo(jwtToken))
                .willReturn(unauthorized())
        );

        NotificationRequestModel request = new NotificationRequestModel();
        request.setNotificationType(Set.of("PRICE")); // or Set.of("QUANTITY")
        request.setEmail("some-email@example.com");

        webTestClient.post()
                .uri("/api/v2/gateway/notifications/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid token");
    }
}
