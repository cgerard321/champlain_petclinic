package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
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
    Flux<Product> findAllProductsByInventoryIdAndProductName(String inventoryId, String productName);
    Flux<Product> findAllProductsByInventoryIdAndProductSalePrice(String inventoryId, Double productSalePrice);

    Mono<Boolean> deleteByInventoryId(String inventoryId);
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(String inventoryId, String productName, Double productPrice, Integer productQuantity);
    //Regex
    Flux<Product> findAllProductsByInventoryIdAndProductNameRegex(String inventoryId, String regex);

    Flux<Product> findAllByInventoryIdAndProductQuantityLessThan(String inventoryId, int productQuantity);

    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductDescription(String inventoryId, String productName, String productDescription);

    Flux<Product> findAllProductsByInventoryIdAndProductDescription(String inventoryId, String productDescription);

    Mono<Integer> countByInventoryId(String inventoryId);

    Flux<Product> findAllProductsByInventoryIdAndStatus(String inventoryId, Status status);

    Flux<Product> findAllProductsByInventoryIdAndProductNameAndStatus(String inventoryId, String productName, Status status);

    Flux<Product> findAllProductsByInventoryIdAndProductDescriptionAndStatus(String inventoryId, String productDescription, Status status);

    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(String inventoryId, String productName, String productDescription, Status status);
    Mono<Long> countByInventoryIdAndLastUpdatedAtAfter(String inventoryId, LocalDateTime since);
    Mono<Boolean> existsByInventoryIdAndProductNameIgnoreCase(String inventoryId, String productName);
}
