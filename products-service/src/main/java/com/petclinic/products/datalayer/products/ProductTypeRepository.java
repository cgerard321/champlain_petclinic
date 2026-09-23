package com.petclinic.products.datalayer.products;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ProductTypeRepository extends R2dbcRepository<ProductTypeDb, String> {

    Mono<ProductTypeDb> findByProductTypeId(String bundleId);

}