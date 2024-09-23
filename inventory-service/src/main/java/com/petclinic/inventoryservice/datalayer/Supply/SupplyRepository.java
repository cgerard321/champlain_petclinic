package com.petclinic.inventoryservice.datalayer.Supply;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface SupplyRepository extends ReactiveMongoRepository<Supply, String> {
    Mono<Boolean> existsBySupplyId(String supplyId);

    Mono<Supply> findSupplyBySupplyId(String supplyId);
    Mono<Supply> findSupplyByInventoryIdAndSupplyId(String inventoryId, String supplyId);
    Mono<Void> deleteBySupplyId(String supplyId);
    Flux<Supply> findAllSuppliesByInventoryId(String inventoryId);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyNameAndSupplyPriceAndSupplyQuantityAndSupplySalePrice(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity,Double supplySalePrice);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyPriceAndSupplyQuantity(String inventoryId, Double supplyPrice, Integer supplyQuantity);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyPrice(String inventoryId, Double supplyPrice);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyQuantity(String inventoryId, Integer supplyQuantity);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyName(String inventoryId, String supplyName);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplySalePrice(String inventoryId, Double supplySalePrice);

    Mono<Boolean> deleteByInventoryId(String inventoryId);
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyNameAndSupplyPriceAndSupplyQuantity(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity);
    //Regex
    Flux<Supply> findAllSuppliesByInventoryIdAndSupplyNameRegex(String inventoryId, String regex);

}

