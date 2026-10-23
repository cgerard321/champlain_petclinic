package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
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
    private OrderReport.Reason reason;
    private List<AffectedItemResponse> affectedItems;
    private String description;
    private OrderReport.Status status;
    private LocalDateTime submittedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedItemResponse {
        private String productId;
        private String productName;
    }
}
