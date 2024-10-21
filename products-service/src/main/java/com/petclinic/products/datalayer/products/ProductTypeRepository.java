package com.petclinic.products.datalayer.products;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductTypeRepository extends ReactiveMongoRepository<ProductType, String> {
}
