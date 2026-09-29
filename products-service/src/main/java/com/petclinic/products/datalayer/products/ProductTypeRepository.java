package com.petclinic.products.datalayer.products;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ProductTypeRepository extends R2dbcRepository<ProductTypeDb, Long> {

    Mono<ProductTypeDb> findByProductTypeId(String bundleId);

}