package com.petclinic.products.datalayer.products;

import com.petclinic.products.datalayer.products.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findProductByProductId(String productId);

    Flux<Product> findByProductSalePriceBetween(Double minPrice, Double maxPrice);

    Flux<Product> findByProductSalePriceGreaterThanEqual(Double minPrice);

    Flux<Product> findByProductSalePriceLessThanEqual(Double maxPrice);
}
