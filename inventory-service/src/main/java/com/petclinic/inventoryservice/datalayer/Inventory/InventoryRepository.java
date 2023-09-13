package com.petclinic.inventoryservice.datalayer.Inventory;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    Inventory findInventoryByInventoryId(String inventoryId);
}
