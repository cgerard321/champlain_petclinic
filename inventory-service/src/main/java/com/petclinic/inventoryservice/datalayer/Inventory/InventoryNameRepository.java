package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InventoryNameRepository extends ReactiveMongoRepository<InventoryName, String> {


}