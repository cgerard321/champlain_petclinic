package com.petclinic.bffapigateway.domainclientlayer;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationRequestModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@WireMockTest(httpPort = 7007)
class NotificationsServiceClientIntegrationTest {
    @Autowired
    private NotificationsServiceClient notificationsServiceClient;

    @Test
    void getAllNotifsForCustomer(){
        String customerId = "1";
        stubFor(get(urlEqualTo("/api/v1/notifications/%s".formatted(customerId)))
                .willReturn(okForContentType(MediaType.TEXT_EVENT_STREAM_VALUE.toString(), "data:{\"productId\": \"1\", \"productName\": \"Box\", \"email\": \"hello@admin.com\", \"notificationType\": [\"PRICE\"]}"))
        );

        StepVerifier.create(notificationsServiceClient.getNotificationsByCustomerId(customerId))
                .assertNext(notificationResponseModel -> {
                    assertEquals("1", notificationResponseModel.getProductId());
                    assertEquals("Box", notificationResponseModel.getProductName());
                    assertEquals("hello@admin.com", notificationResponseModel.getEmail());
                    assertTrue(notificationResponseModel.getNotificationType().contains("PRICE"));
                })
                .verifyComplete();
    }

    @Test
    void getNotifForCustomerAndProduct() {
        String customerId = "1";
        String productId = "1";
        stubFor(get(urlEqualTo("/api/v1/notifications/%s/%s".formatted(customerId, productId)))
                .willReturn(okForContentType(MediaType.APPLICATION_JSON_VALUE.toString(), "{\"productId\": \"1\", \"productName\": \"Box\", \"email\": \"hello@admin.com\", \"notificationType\": [\"PRICE\"]}"))
        );

        StepVerifier.create(notificationsServiceClient.getNotificationByCustomerIdAndProductId(customerId, productId))
                .assertNext(notificationResponseModel -> {
                    assertEquals("1", notificationResponseModel.getProductId());
                    assertEquals("Box", notificationResponseModel.getProductName());
                    assertEquals("hello@admin.com", notificationResponseModel.getEmail());
                    assertTrue(notificationResponseModel.getNotificationType().contains("PRICE"));
                })
                .verifyComplete();
    }

    @Test
    void addNotificationForCustomerAndProduct() {
        String customerId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        NotificationRequestModel requestModel = NotificationRequestModel.builder()
                .email("hello@admin.com")
                .notificationType(Set.of("PRICE"))
                .build();

        stubFor(post(urlEqualTo("/api/v1/notifications/%s/%s".formatted(customerId, productId)))
                .withRequestBody(matchingJsonSchema("{\"email\": \"" + requestModel.getEmail() + "\",\"notificationType\": [\"PRICE\"]}"))
                .willReturn(created()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE.toString())
                        .withBody("{\"productId\": \"" + productId + "\", \"productName\": \"Box\", \"email\": \"" + requestModel.getEmail() + "\", \"notificationType\": [\"PRICE\"]}")
                )
        );

        StepVerifier.create(notificationsServiceClient.addNotificationSubscription(customerId, productId, Mono.just(requestModel)))
                .assertNext(notificationResponseModel -> {
                    assertEquals(productId, notificationResponseModel.getProductId());
                    assertEquals("Box", notificationResponseModel.getProductName());
                    assertEquals(requestModel.getEmail(), notificationResponseModel.getEmail());
                    assertTrue(notificationResponseModel.getNotificationType().contains("PRICE"));
                })
                .verifyComplete();
    }

    @Test
    void updateNotificationForCustomerAndProduct() {
        String customerId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        NotificationRequestModel requestModel = NotificationRequestModel.builder()
                .email("hi@admin.com")
                .notificationType(Set.of("QUANTITY"))
                .build();

        stubFor(put(urlEqualTo("/api/v1/notifications/%s/%s".formatted(customerId, productId)))
                .withRequestBody(matchingJsonSchema("{\"email\": \"" + requestModel.getEmail() + "\",\"notificationType\": [\"QUANTITY\"]}"))
                .willReturn(created()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE.toString())
                        .withBody("{\"productId\": \"" + productId + "\", \"productName\": \"Box\", \"email\": \"" + requestModel.getEmail() + "\", \"notificationType\": [\"QUANTITY\"]}")
                )
        );

        StepVerifier.create(notificationsServiceClient.updateNotificationSubscription(customerId, productId, Mono.just(requestModel)))
                .assertNext(notificationResponseModel -> {
                    assertEquals(productId, notificationResponseModel.getProductId());
                    assertEquals("Box", notificationResponseModel.getProductName());
                    assertEquals(requestModel.getEmail(), notificationResponseModel.getEmail());
                    assertTrue(notificationResponseModel.getNotificationType().contains("QUANTITY"));
                })
                .verifyComplete();
    }

    @Test
    void deleteNotificationForCustomerAndProduct() {
        String customerId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        stubFor(delete(urlEqualTo("/api/v1/notifications/%s/%s".formatted(customerId, productId)))
                .willReturn(ok())
        );

        StepVerifier.create(notificationsServiceClient.deleteNotificationSubscription(customerId, productId))
                .verifyComplete();
    }
}