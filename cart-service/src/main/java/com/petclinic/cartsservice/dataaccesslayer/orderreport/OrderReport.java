package com.petclinic.cartsservice.dataaccesslayer.orderreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "order_reports")
@CompoundIndexes({
        @CompoundIndex(
                name = "order_status_idx",
                def = "{'orderId': 1, 'status': 1}"
        ),
        @CompoundIndex(
                name = "customer_submitted_idx",
                def = "{'customerId': 1, 'submittedAt': -1}"
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReport {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reportId;

    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private OrderReportReason reason;
    private List<AffectedItem> affectedItems;
    private String description;
    private OrderReportStatus status;
    private LocalDateTime submittedAt;
    private String resolutionNote;
    private LocalDateTime resolvedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedItem {
        private String productId;
        private String productName;
    }
}
