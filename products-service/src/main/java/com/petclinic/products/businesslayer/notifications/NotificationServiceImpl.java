package com.petclinic.products.businesslayer.notifications;

import com.petclinic.products.datalayer.notifications.NotificationRepository;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
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
        return null;
    }

    @Override
    public Mono<NotificationResponseModel> addNotificationSubscription(String productId, String customerId) {
        return null;
    }

    @Override
    public Mono<NotificationResponseModel> updateNotificationSubscription(String productId, String customerId) {
        return null;
    }

    @Override
    public Mono<NotificationResponseModel> deleteNotificationSubscription(String productId, String customerId) {
        return null;
    }
}
