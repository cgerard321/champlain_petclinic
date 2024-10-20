package com.petclinic.products.presentationlayer.notifications;

import com.petclinic.products.businesslayer.notifications.NotificationService;
import com.petclinic.products.datalayer.notifications.NotificationType;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void validateId(String id, String name){
        if(id.length() != 36) {
            throw new InvalidInputException("Provided " + name + " id is invalid: " + id);
        }
    }

    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NotificationType> getSubscriptionTypes(){
        return Flux.fromArray(NotificationType.values());
    }

    @GetMapping(value = "/{customerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationResponseModel> getNotificationsByCustomerId(@PathVariable String customerId){
        validateId(customerId, "customer");
        return notificationService.getAllNotificationSubscriptions(customerId);
    }

    @GetMapping("/{customerId}/{productId}")
    public Mono<ResponseEntity<NotificationResponseModel>> getNotificationByCustomerIdAndProductId(@PathVariable String customerId, @PathVariable String productId){
        validateId(customerId, "customer");
        validateId(productId, "product");

        return notificationService.getNotificationSubscription(productId, customerId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{customerId}/{productId}")
    public Mono<ResponseEntity<NotificationResponseModel>> addNotificationSubscription(@PathVariable String customerId, @PathVariable String productId, @RequestBody Mono<NotificationRequestModel> requestModel){

        validateId(customerId, "customer");
        validateId(productId, "product");

        return notificationService.addNotificationSubscription(productId, customerId, requestModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{customerId}/{productId}")
    public Mono<ResponseEntity<NotificationResponseModel>> updateNotificationSubscription(@PathVariable String customerId, @PathVariable String productId, @RequestBody Mono<NotificationRequestModel> requestModel){
        validateId(customerId, "customer");
        validateId(productId, "product");

        return notificationService.updateNotificationSubscription(productId, customerId, requestModel)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{customerId}/{productId}")
    public Mono<ResponseEntity<Void>> deleteNotificationSubscription(@PathVariable String customerId, @PathVariable String productId){
        validateId(customerId, "customer");
        validateId(productId, "product");

        return notificationService.deleteNotificationSubscription(productId, customerId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
