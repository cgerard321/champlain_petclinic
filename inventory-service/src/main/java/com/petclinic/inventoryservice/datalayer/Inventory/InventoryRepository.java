package com.petclinic.inventoryservice.datalayer.Inventory;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    Mono<Inventory> findInventoryByInventoryId(String inventoryId);
}
