package com.petclinic.products.datalayer.products;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;

public interface ProductRepository extends R2dbcRepository<Product, Long> {

    @Modifying
    @Query("UPDATE products SET product_name_fr = :translation WHERE product_id = :productId AND product_name = :original AND product_name_fr IS NULL")
    Mono<Integer> backfillFrenchName(String productId, String original, String translation);

    @Modifying
    @Query("UPDATE products SET product_description_fr = :translation WHERE product_id = :productId AND product_description = :original AND product_description_fr IS NULL")
    Mono<Integer> backfillFrenchDescription(String productId, String original, String translation);

    Mono<Product> findProductByProductId(String productId);

    Flux<Product> findByProductNameContainingIgnoreCase(String productName);

    Flux<Product> findByProductSalePriceBetween(Double minPrice, Double maxPrice);

    Flux<Product> findByProductSalePriceGreaterThanEqual(Double minPrice);

    Flux<Product> findByProductSalePriceLessThanEqual(Double maxPrice);

    Flux<Product> findProductsByProductTypeId(String productTypeId);

    Mono<Boolean> existsByImageIdAndProductIdNot(
            String imageId, String productId);

    Flux<Product> findAllByProductIdIn(List<String> productIds);
}
