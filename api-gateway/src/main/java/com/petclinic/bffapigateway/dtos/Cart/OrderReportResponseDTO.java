package com.petclinic.bffapigateway.dtos.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportResponseDTO {

    private String reportId;
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String reason;
    private List<AffectedItemDTO> affectedItems;
    private String description;
    private String status;
    private LocalDateTime submittedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedItemDTO {
        private String productId;
        private String productName;
    }
}
