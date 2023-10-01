package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    Mono<Inventory> findInventoryByInventoryId(String inventoryId);
    Mono<Boolean> existsByInventoryId(String inventoryId);

    //search
    Flux<Inventory> findAllByInventoryNameAndInventoryTypeAndInventoryDescription(String inventoryName, String inventoryType, String inventoryDescription);

    Flux<Inventory>  findAllByInventoryTypeAndInventoryDescription(String inventoryType, String inventoryDescription);

    Flux<Inventory>  findAllByInventoryName(String inventoryName);

    Flux<Inventory>  findAllByInventoryType(String inventoryType);

    Flux<Inventory>  findAllByInventoryDescription(String inventoryDescription);
}
