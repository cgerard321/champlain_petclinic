package com.petclinic.products.businesslayer.notifications;

import com.petclinic.products.datalayer.notifications.Notification;
import com.petclinic.products.datalayer.notifications.NotificationRepository;
import com.petclinic.products.datalayer.notifications.NotificationType;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.presentationlayer.notifications.NotificationRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private String productId = UUID.randomUUID().toString();

    Notification notif1 = Notification.builder()
            .id("2")
            .customerId(UUID.randomUUID().toString())
            .productId(productId)
            .email("bro@email.com")
            .notificationType(EnumSet.of(NotificationType.QUANTITY))
            .previousQuantity(50)
            .previousPrice(123.5)
            .build();

    Notification notif2 = Notification.builder()
            .id("1")
            .customerId(UUID.randomUUID().toString())
            .productId(productId)
            .email("hello@world.com")
            .notificationType(EnumSet.of(NotificationType.QUANTITY))
            .previousQuantity(50)
            .previousPrice(123.5)
            .build();

    @Test
    void getAllNotificationSubscriptions() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(notificationRepository.findNotificationsByCustomerId(notif1.getCustomerId()))
                .thenReturn(Flux.just(notif1));

        StepVerifier.create(notificationService.getAllNotificationSubscriptions(notif1.getCustomerId()))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getNotificationSubscription() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .averageRating(0.0)
                .build();
        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, notif1.getCustomerId()))
                .thenReturn(Mono.just(notif1));

        StepVerifier.create(notificationService.getNotificationSubscription(productId, notif1.getCustomerId()))
                .expectNextMatches(n -> n.getEmail().equals(notif1.getEmail()))
                .verifyComplete();
    }

    @Test
    void addNotificationSubscription() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .productQuantity(50)
                .averageRating(0.0)
                .build();

        NotificationRequestModel req = NotificationRequestModel.builder()
                .email("bro@email.com")
                .notificationType(EnumSet.of(NotificationType.QUANTITY))
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, notif1.getCustomerId()))
                .thenReturn(Mono.empty());
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(notificationService.addNotificationSubscription(productId, notif1.getCustomerId(), Mono.just(req)))
                .expectNextMatches(n -> n.getEmail().equals(notif1.getEmail()))
                .verifyComplete();
    }

    @Test
    void updateNotificationSubscription() {
        Product prod = Product.builder()
                .productId(productId)
                .productName("Sample Name")
                .productDescription("Sample Description")
                .productSalePrice(100.0)
                .productQuantity(50)
                .averageRating(0.0)
                .build();

        NotificationRequestModel req = NotificationRequestModel.builder()
                .email("hello@bro.org")
                .notificationType(EnumSet.of(NotificationType.QUANTITY))
                .build();

        when(productRepository.findProductByProductId(productId))
                .thenReturn(Mono.just(prod));
        when(notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, notif1.getCustomerId()))
                .thenReturn(Mono.just(notif1));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(notificationService.updateNotificationSubscription(productId, notif1.getCustomerId(), Mono.just(req)))
                .expectNextMatches(n -> n.getEmail().equals(req.getEmail()))
                .verifyComplete();
    }

    @Test
    void deleteNotificationSubscription() {
        when(notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, notif1.getCustomerId()))
                .thenReturn(Mono.just(notif1));
        when(notificationRepository.delete(any(Notification.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(notificationService.deleteNotificationSubscription(productId, notif1.getCustomerId()))
                .verifyComplete();
    }
}