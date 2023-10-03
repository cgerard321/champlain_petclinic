package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Mono<Boolean> existsByProductId(String productId);

    Mono<Product> findProductByProductId(String productId);
    Mono<Product> findProductByInventoryIdAndProductId(String inventoryId, String productId);
    Mono<Void> deleteByProductId(String productId);
    Flux<Product> findAllProductsByInventoryId(String inventoryId);
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(String inventoryId, String productName, Double productPrice, Integer productQuantity);
    Flux<Product> findAllProductsByInventoryIdAndProductPriceAndProductQuantity(String inventoryId, Double productPrice, Integer productQuantity);
    Flux<Product> findAllProductsByInventoryIdAndProductPrice(String inventoryId, Double productPrice);
    Flux<Product> findAllProductsByInventoryIdAndProductQuantity(String inventoryId, Integer productQuantity);
    Flux<Product> findAllProductsByInventoryIdAndProductName(String inventoryId, String productName);

    Mono<Boolean> deleteByInventoryId(String inventoryId);

}
