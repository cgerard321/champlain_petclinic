package com.petclinic.inventoryservice.datalayer.Product;

import com.petclinic.inventoryservice.datalayer.Supply.Status;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    Flux<Product> findAllProductsByInventoryIdAndProductDescription(String inventoryId, String productDescription);
    Flux<Product> findAllProductsByInventoryIdAndProductNameAndProductDescription(String inventoryId, String productName, String productDescription);

}
