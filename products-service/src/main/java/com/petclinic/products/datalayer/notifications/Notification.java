package com.petclinic.products.datalayer.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.EnumSet;

@Document(collection = "product_notification_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;
    private String productId;
    private String customerId;
    private Double previousPrice;
    private Integer previousQuantity;
    private EnumSet<NotificationType> notificationType;
}
