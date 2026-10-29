package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    Mono<Boolean> existsByProductId(String productId);

    Mono<Product> findProductByProductId(String productId);
    Mono<Product> findProductByInventoryIdAndProductId(String inventoryId, String productId);
    Mono<Void> deleteByProductId(String productId);
    Flux<Product> findAllProductsByInventoryId(String inventoryId);
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantityAndProductSalePrice(String inventoryId, String productName, Double productPrice, Integer productQuantity,Double productSalePrice);
    Flux<Product> findAllProductsByInventoryIdAndProductPriceAndProductQuantity(String inventoryId, Double productPrice, Integer productQuantity);
    Flux<Product> findAllProductsByInventoryIdAndProductPrice(String inventoryId, Double productPrice);
    Flux<Product> findAllProductsByInventoryIdAndProductQuantity(String inventoryId, Integer productQuantity);
    @Query("{ 'inventoryId': ?0, '$or': [ { 'productName': ?1 }, { 'name.en': ?1 }, { 'name.fr': ?1 } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductName(String inventoryId, String productName);
    Flux<Product> findAllProductsByInventoryIdAndProductSalePrice(String inventoryId, Double productSalePrice);

    Mono<Boolean> deleteByInventoryId(String inventoryId);
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(String inventoryId, String productName, Double productPrice, Integer productQuantity);
    //Regex
    @Query("{ 'inventoryId': ?0, '$or': [ { 'productName': { '$regex': ?1 } }, { 'name.en': { '$regex': ?1 } }, { 'name.fr': { '$regex': ?1 } } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductNameRegex(String inventoryId, String regex);

    Flux<Product> findAllByInventoryIdAndProductQuantityLessThan(String inventoryId, int productQuantity);

    @Query("{ 'inventoryId': ?0, '$and': [ { '$or': [ { 'productName': ?1 }, { 'name.en': ?1 }, { 'name.fr': ?1 } ] }, { '$or': [ { 'productDescription': ?2 }, { 'description.en': ?2 }, { 'description.fr': ?2 } ] } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductDescription(String inventoryId, String productName, String productDescription);

    @Query("{ 'inventoryId': ?0, '$or': [ { 'productDescription': ?1 }, { 'description.en': ?1 }, { 'description.fr': ?1 } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductDescription(String inventoryId, String productDescription);

    Mono<Integer> countByInventoryId(String inventoryId);

    Flux<Product> findAllProductsByInventoryIdAndStatus(String inventoryId, Status status);

    @Query("{ 'inventoryId': ?0, 'status': ?2, '$or': [ { 'productName': ?1 }, { 'name.en': ?1 }, { 'name.fr': ?1 } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndStatus(String inventoryId, String productName, Status status);

    @Query("{ 'inventoryId': ?0, 'status': ?2, '$or': [ { 'productDescription': ?1 }, { 'description.en': ?1 }, { 'description.fr': ?1 } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductDescriptionAndStatus(String inventoryId, String productDescription, Status status);

    @Query("{ 'inventoryId': ?0, 'status': ?3, '$and': [ { '$or': [ { 'productName': ?1 }, { 'name.en': ?1 }, { 'name.fr': ?1 } ] }, { '$or': [ { 'productDescription': ?2 }, { 'description.en': ?2 }, { 'description.fr': ?2 } ] } ] }")
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(String inventoryId, String productName, String productDescription, Status status);
    Mono<Long> countByInventoryIdAndLastUpdatedAtAfter(String inventoryId, LocalDateTime since);
    Mono<Boolean> existsByInventoryIdAndProductNameIgnoreCase(String inventoryId, String productName);
    Mono<Boolean> existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
            String inventoryId, String productName, String productId
    );

    Flux<Product> findAllProductsByInventoryIdAndProductPriceBetween(String inventoryId, Double minPrice, Double maxPrice);
    Flux<Product> findAllProductsByInventoryIdAndProductSalePriceBetween(String inventoryId, Double minSalePrice, Double maxSalePrice);
}
