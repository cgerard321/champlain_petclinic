package com.petclinic.products.datalayer.products;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductBundleRepository extends R2dbcRepository<ProductBundle, Long> {
    Mono<ProductBundle> findByBundleId(String bundleId);

    @Query("SELECT * FROM product_bundles WHERE :productId = ANY(product_ids)")
    Flux<ProductBundle> findAllByProductIdsContaining(String productId);
}
