package com.petclinic.products.presentationlayer.notifications;

import com.petclinic.products.datalayer.notifications.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseModel {
    String productId;
    String productName;
    EnumSet<NotificationType> notificationType;
}