package com.petclinic.products.datalayer.notifications;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findNotificationsByCustomerId(String customerId);
    Mono<Notification> findNotificationSubscriptionByProductIdAndCustomerId(String productId, String customerId);
}
