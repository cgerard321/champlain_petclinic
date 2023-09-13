package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<String, Product> {
}
