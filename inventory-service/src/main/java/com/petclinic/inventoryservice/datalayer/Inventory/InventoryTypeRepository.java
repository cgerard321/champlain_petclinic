package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InventoryTypeRepository extends ReactiveMongoRepository<InventoryType, String> {


}
