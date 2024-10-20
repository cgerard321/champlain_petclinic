package com.petclinic.products.businesslayer.notifications;

import com.petclinic.products.datalayer.notifications.Notification;
import com.petclinic.products.datalayer.notifications.NotificationRepository;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.presentationlayer.notifications.NotificationRequestModel;
import com.petclinic.products.presentationlayer.notifications.NotificationResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationServiceImpl implements NotificationService{
    private ProductRepository productRepository;
    private NotificationRepository notificationRepository;

    public NotificationServiceImpl(ProductRepository productRepository, NotificationRepository notificationRepository) {
        this.productRepository = productRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Flux<NotificationResponseModel> getAllNotificationSubscriptions(String customerId) {
        return notificationRepository.findNotificationsByCustomerId(customerId)
                .flatMap(n -> productRepository.findProductByProductId(n.getProductId())
                        .switchIfEmpty(Mono.defer(() -> {
                            // If reference to product isn't found, we delete the subscription and skip.
                            notificationRepository.delete(n);
                            return Mono.empty();
                        }))
                        .map(p -> EntityModelUtil.toNotificationResponseModel(n, p))
                );
    }

    @Override
    public Mono<NotificationResponseModel> getNotificationSubscription(String productId, String customerId) {
        return notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, customerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Notification subscription not found for product id " + productId + " and customer id " + customerId)))
                .flatMap(n -> productRepository.findProductByProductId(n.getProductId())
                        .switchIfEmpty(Mono.defer(() -> {
                            // If reference to product isn't found, we delete the subscription and skip.
                            notificationRepository.delete(n);
                            return Mono.error(new NotFoundException("Product not found for product id " + n.getProductId()));
                        }))
                        .map(p -> EntityModelUtil.toNotificationResponseModel(n, p))
                );
    }

    @Override
    public Mono<NotificationResponseModel> addNotificationSubscription(String productId, String customerId, Mono<NotificationRequestModel> requestModel) {
        return requestModel
                .filter(req -> req.getNotificationType() != null && !req.getNotificationType().isEmpty())
                .switchIfEmpty(Mono.error(new NotFoundException("Notification type must be provided")))
                .flatMap(req -> productRepository.findProductByProductId(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                        .flatMap(p -> notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, customerId)
                                .doOnNext(n -> {
                                    throw new NotFoundException("Notification subscription already exists for product id " + productId + " and customer id " + customerId);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    Notification notification = EntityModelUtil.toNotificationEntity(req, p, customerId);
                                    return notificationRepository.save(notification);
                                }))
                                .map(n -> EntityModelUtil.toNotificationResponseModel(n, p))
                        )
                );
    }

    @Override
    public Mono<NotificationResponseModel> updateNotificationSubscription(String productId, String customerId, Mono<NotificationRequestModel> requestModel) {
        return requestModel
                .filter(req -> req.getNotificationType() != null && !req.getNotificationType().isEmpty())
                .switchIfEmpty(Mono.error(new NotFoundException("Notification type must be provided")))
                .flatMap(req -> productRepository.findProductByProductId(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Product id not found: " + productId)))
                        .flatMap(p -> notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, customerId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Notification subscription not found for product id " + productId + " and customer id " + customerId)))
                                .flatMap(n -> {
                                    Notification notification = EntityModelUtil.toNotificationEntity(req, p, customerId);
                                    notification.setId(n.getId());
                                    return notificationRepository.save(notification);
                                })
                                .map(updatedNotification -> EntityModelUtil.toNotificationResponseModel(updatedNotification, p))
                        )
                );
    }
    // Void because Product might be deleted in some situations
    // It would be confusing to get an error saying product couldn't be found but subscription still get deleted.
    @Override
    public Mono<Void> deleteNotificationSubscription(String productId, String customerId) {
        return notificationRepository.findNotificationSubscriptionByProductIdAndCustomerId(productId, customerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Notification subscription not found for product id " + productId + " and customer id " + customerId)))
                .flatMap(notificationRepository::delete);
    }
}
