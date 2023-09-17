package com.petclinic.inventoryservice.datalayer.Product;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Mono<Boolean> existsByProductId(String productId);

    Mono<Product> findProductByProductId(String productId);

}
