package com.petclinic.products.datalayer.products;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductBundleRepository extends ReactiveMongoRepository<ProductBundle, String> {

    Mono<ProductBundle> findByBundleId(String bundleId);

}
