package com.petclinic.inventoryservice.datalayer.Audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "supply_audit_entries")
public record SupplyAuditEntry(
        @Id String id,
        String inventoryId,
        String supplyId,
        String supplyName,
        int previousQuantity,
        int newQuantity,
        int quantityChange,
        SupplyAuditAction action,
        Instant changedAt,
        String changedBy
) {
}