package com.petclinic.inventoryservice.datalayer.Inventory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveMongoRepository<Inventory, String> {
    Mono<Inventory> findInventoryByInventoryId(String inventoryId);
    Mono<Boolean> existsByInventoryId(String inventoryId);

    Mono<Inventory> findInventoryByInventoryCode(String inventoryCode);

    //search
    @Query("{ '$and': [ { '$or': [ { 'inventoryName': ?0 }, { 'name.en': ?0 }, { 'name.fr': ?0 } ] }, { 'inventoryType': ?1 }, { '$or': [ { 'inventoryDescription': ?2 }, { 'description.en': ?2 }, { 'description.fr': ?2 } ] } ] }")
    Flux<Inventory> findAllByInventoryNameAndInventoryTypeAndInventoryDescription(String inventoryName, String inventoryType, String inventoryDescription);

    @Query("{ 'inventoryType': ?0, '$or': [ { 'inventoryDescription': ?1 }, { 'description.en': ?1 }, { 'description.fr': ?1 } ] }")
    Flux<Inventory>  findAllByInventoryTypeAndInventoryDescription(String inventoryType, String inventoryDescription);

    Flux<Inventory>  findAllByInventoryName(String inventoryName);

    Flux<Inventory>  findAllByInventoryType(String inventoryType);

    Flux<Inventory>  findAllByInventoryDescription(String inventoryDescription);

    //using regex
    @Query("{ '$or': [ { 'inventoryName': { '$regex': ?0 } }, { 'name.en': { '$regex': ?0 } }, { 'name.fr': { '$regex': ?0 } } ] }")
    Flux<Inventory> findByInventoryNameRegex(String regex);

    @Query("{ '$or': [ { 'inventoryDescription': { '$regex': ?0 } }, { 'description.en': { '$regex': ?0 } }, { 'description.fr': { '$regex': ?0 } } ] }")
    Flux<Inventory> findByInventoryDescriptionRegex(String regex);

    Flux<Inventory> findByImportant(Boolean important);

    Mono<Boolean> existsByInventoryName(String inventoryName);
    Mono<Boolean> existsByInventoryNameAndInventoryIdNot(String inventoryName, String inventoryId);

    Mono<Inventory> findByInventoryType(String inventoryType);
    Mono<Inventory> findByInventoryName(String inventoryName);
}
