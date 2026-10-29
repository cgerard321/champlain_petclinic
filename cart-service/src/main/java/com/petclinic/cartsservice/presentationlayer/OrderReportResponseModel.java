package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportReason;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
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
public class OrderReportResponseModel {

    private String reportId;
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private OrderReportReason reason;
    private List<AffectedItemResponse> affectedItems;
    private String description;
    private OrderReportStatus status;
    private LocalDateTime submittedAt;
    private String resolutionNote;
    private LocalDateTime resolvedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedItemResponse {
        private String productId;
        private String productName;
    }
}
