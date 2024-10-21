package com.petclinic.products.presentationlayer.notifications;

import com.petclinic.products.datalayer.notifications.Notification;
import com.petclinic.products.datalayer.notifications.NotificationRepository;
import com.petclinic.products.datalayer.notifications.NotificationType;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.data.mongodb.port=0")
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebTestClient
class NotificationControllerIntegrationTest {
    @Autowired
    WebTestClient webClient;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ProductRepository productRepository;

    private final String INVALID_PRODUCT_ID = "e39286b6-d3cab6222f58";
    private final String UNFOUND_PRODUCT_ID = "828537fe-907d-4ece-8455-c7e483327a68";

    private final String INVALID_USER_ID = "186a7ca6-5fb756923d9d";

    private final String CUSTOMER_1 = "186a7ca6-5fb7-4f58-8b6f-30f1f1b9a2f6";
    private final String CUSTOMER_2 = "f4efac30-3f59-4443-8379-3d8c73cdd3a6";

    Product product1 = Product.builder()
            .productId("e39286b6-d3ca-4f58-8b6f-30f1f1b9a2f6")
            .productName("product1")
            .productName("product1 description")
            .productSalePrice(100.0)
            .build();

    Product product2 = Product.builder()
            .productId("6866ec50-898d-435c-bfec-8ee513c5a6c1")
            .productName("product2")
            .productName("product2 description")
            .productSalePrice(200.0)
            .build();

    Notification notif1prod1 = Notification.builder()
            .productId(product1.getProductId())
            .customerId(CUSTOMER_1)
            .notificationType(EnumSet.of(NotificationType.PRICE))
            .previousPrice(product1.getProductSalePrice())
            .previousQuantity(product1.getProductQuantity())
            .email("admin1@somerandomwebsiteemail.com")
            .build();

    Notification notif2prod1 = Notification.builder()
            .productId(product1.getProductId())
            .customerId(CUSTOMER_2)
            .notificationType(EnumSet.of(NotificationType.QUANTITY))
            .previousPrice(product1.getProductSalePrice())
            .previousQuantity(product1.getProductQuantity())
            .email("admin2@somerandomwebsiteemail.com")
            .build();

    Notification notif1prod2 = Notification.builder()
            .productId(product2.getProductId())
            .customerId(CUSTOMER_1)
            .notificationType(EnumSet.of(NotificationType.PRICE))
            .previousPrice(product2.getProductSalePrice())
            .previousQuantity(product2.getProductQuantity())
            .email("admin1@somerandomwebsiteemail.com")
            .build();

    Notification notif2prod2 = Notification.builder()
            .productId(product2.getProductId())
            .customerId(CUSTOMER_2)
            .notificationType(EnumSet.of(NotificationType.QUANTITY))
            .previousPrice(product2.getProductSalePrice())
            .previousQuantity(product2.getProductQuantity())
            .email("admin2@somerandomwebsiteemail.com")
            .build();

    @BeforeEach
    public void setupDB(){
        Publisher<Product> productSetup = productRepository.deleteAll()
                .thenMany(Flux.just(product1, product2)
                        .flatMap(productRepository::save));
        StepVerifier.create(productSetup)
                .expectNextCount(2)
                .verifyComplete();

        Publisher<Notification> notifSetup = notificationRepository.deleteAll()
                .thenMany(Flux.just(notif1prod1, notif2prod1, notif1prod2, notif2prod2)
                        .flatMap(notificationRepository::save));
        StepVerifier.create(notifSetup)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void getSubscriptionTypes() {
        webClient.get().uri("/api/v1/notifications/types")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationType.class)
                .hasSize(NotificationType.values().length);
    }

    @Test
    void whenGetNotificationsByCustomerId_thenReturnNotifications() {
        webClient.get().uri("/api/v1/notifications/" + CUSTOMER_1)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationResponseModel.class)
                .hasSize(2);
    }

    @Test
    void whenGetNotificationsByNotFoundCustomerId_thenReturnEmptyList(){
        webClient.get().uri("/api/v1/notifications/2724c875-9b8b-4968-88e9-66cd452bbb80")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationResponseModel.class)
                .hasSize(0);
    }

    @Test
    void whenGetNotificationsByInvalidCustomerId_thenReturnBadRequest(){
        webClient.get().uri("/api/v1/notifications/" + INVALID_USER_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void getNotificationByCustomerIdAndProductId() {
        webClient.get().uri("/api/v1/notifications/" + CUSTOMER_1 + "/" + product1.getProductId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationResponseModel.class)
                .isEqualTo(NotificationResponseModel.builder()
                        .productId(product1.getProductId())
                        .productName(product1.getProductName())
                        .email(notif1prod1.getEmail())
                        .notificationType(notif1prod1.getNotificationType())
                        .build());
    }

    @Test
    void addNotificationSubscription() {
        NotificationRequestModel requestModel = NotificationRequestModel.builder()
                .notificationType(EnumSet.of(NotificationType.PRICE))
                .email("newuser@somerandomwebsiteemail.com")
                .build();

        webClient.post().uri("/api/v1/notifications/" + "d9788738-92df-488b-a3a4-a29a59014447" + "/" + product1.getProductId())
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(NotificationResponseModel.class)
                .value(response -> {
                    assertEquals(product1.getProductId(), response.getProductId());
                    assertEquals(product1.getProductName(), response.getProductName());
                    assertEquals(requestModel.getEmail(), response.getEmail());
                    assertEquals(requestModel.getNotificationType(), response.getNotificationType());
                });

        StepVerifier.create(notificationRepository.findAll())
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    void updateNotificationSubscription() {
        NotificationRequestModel requestModel = NotificationRequestModel.builder()
                .notificationType(EnumSet.of(NotificationType.QUANTITY))
                .email("newemail@admin.com")
                .build();

        webClient.put()
                .uri("/api/v1/notifications/" + CUSTOMER_1 + "/" + product1.getProductId())
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationResponseModel.class)
                .value(response -> {
                    assertEquals(product1.getProductId(), response.getProductId());
                    assertEquals(product1.getProductName(), response.getProductName());
                    assertEquals(requestModel.getEmail(), response.getEmail());
                    assertEquals(requestModel.getNotificationType(), response.getNotificationType());
                });

        StepVerifier.create(notificationRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void deleteNotificationSubscription() {
        webClient.delete()
                .uri("/api/v1/notifications/" + CUSTOMER_1 + "/" + product1.getProductId())
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(product1.getProductId(), CUSTOMER_1))
                .expectNextCount(0)
                .verifyComplete();
    }
}