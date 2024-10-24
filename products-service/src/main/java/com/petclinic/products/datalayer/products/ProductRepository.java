package com.petclinic.products.datalayer.products;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findProductByProductId(String productId);




    @Query("{ 'productSalePrice' : { $gte: ?0, $lte: ?1 } }")
    Flux<Product> findByProductSalePriceBetween(Double minPrice, Double maxPrice);

    @Query("{ 'productSalePrice' : { $gte: ?0 } }")
    Flux<Product> findByProductSalePriceGreaterThanEqual(Double minPrice);

    @Query("{ 'productSalePrice' : { $lte: ?0 } }")
    Flux<Product> findByProductSalePriceLessThanEqual(Double maxPrice);
    Flux<Product> findProductsByProductType(String productType);
    List<Product> findByProductType(ProductType productType);


}
