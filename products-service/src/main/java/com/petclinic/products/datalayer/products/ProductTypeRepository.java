package com.petclinic.products.datalayer.products;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductTypeRepository extends ReactiveMongoRepository<ProductTypeDb, String> {

    Mono<ProductTypeDb> findByProductTypeId(String bundleId);

}