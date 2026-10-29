package com.petclinic.inventoryservice.datalayer.Audit;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SupplyAuditRepository extends ReactiveMongoRepository<SupplyAuditEntry, String> {
    Flux<SupplyAuditEntry> findTop20ByInventoryIdAndSupplyIdOrderByChangedAtDesc(
            String inventoryId,
            String supplyId
    );
}