package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface InventoryTypeRepository extends ReactiveMongoRepository<InventoryType, String> {

    Mono<Boolean> existsByTypeIgnoreCase(String type);

}
