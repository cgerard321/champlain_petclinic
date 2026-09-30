package com.petclinic.products.datalayer.products;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductRepository extends R2dbcRepository<Product, Long> {

    Mono<Product> findProductByProductId(String productId);

    Flux<Product> findByProductSalePriceBetween(Double minPrice, Double maxPrice);

    Flux<Product> findByProductSalePriceGreaterThanEqual(Double minPrice);

    Flux<Product> findByProductSalePriceLessThanEqual(Double maxPrice);

    Flux<Product> findProductsByProductType(String productType);

    List<Product> findByProductType(ProductType productType);

    Flux<Product> findAllByProductIdIn(List<String> productIds);
}
