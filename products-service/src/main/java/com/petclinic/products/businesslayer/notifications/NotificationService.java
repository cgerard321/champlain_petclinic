package com.petclinic.products.businesslayer.notifications;

import com.petclinic.products.datalayer.notifications.Notification;
import com.petclinic.products.presentationlayer.notifications.NotificationResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Flux<NotificationResponseModel> getAllNotificationSubscriptions(String customerId);
    Mono<NotificationResponseModel> getNotificationSubscription(String productId, String customerId);
    Mono<NotificationResponseModel> addNotificationSubscription(String productId, String customerId);
    Mono<NotificationResponseModel> updateNotificationSubscription(String productId, String customerId);
    Mono<NotificationResponseModel> deleteNotificationSubscription(String productId, String customerId);
}
