package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    Mono<Inventory> findInventoryByInventoryId(String inventoryId);
    Mono<Boolean> existsByInventoryId(String inventoryId);
}
