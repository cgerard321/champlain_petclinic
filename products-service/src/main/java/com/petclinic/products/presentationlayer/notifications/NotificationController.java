package com.petclinic.products.presentationlayer.notifications;

import com.petclinic.products.businesslayer.notifications.NotificationService;
import com.petclinic.products.datalayer.notifications.NotificationType;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

    @GetMapping(value = "/subscriptionTypes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResponseEntity<NotificationType>> getSubscriptionTypes(){
        return Flux.fromArray(NotificationType.values()).map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{customerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationResponseModel> getNotificationsByCustomerId(@PathVariable String customerId){
        validateId(customerId, "customer");
        return notificationService.getAllNotificationSubscriptions(customerId);
    }
}
