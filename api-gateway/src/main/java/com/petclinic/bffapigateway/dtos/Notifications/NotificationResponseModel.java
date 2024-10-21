package com.petclinic.bffapigateway.dtos.Notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseModel {
    String productId;
    String productName;
    Set<String> notificationType;
}
