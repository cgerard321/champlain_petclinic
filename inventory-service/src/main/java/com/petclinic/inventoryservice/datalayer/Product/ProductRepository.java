package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Mono<Boolean> existsBySku(Integer sku);
}
