package com.petclinic.inventoryservice.datalayer.Product;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;

    @Test
    public void ShouldSaveSingleProduct(){
        //arrange
        Product newProduct = buildProduct("inventoryId_1", "sku_1", "product_1", "product_1", 10.0, 10);
        Publisher<Product> setup = productRepository.save(newProduct);
        //Act and Assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    private Product buildProduct(String inventoryId, String productId, String productName, String productDescription, Double productPrice, Integer productQuantity) {
        return Product.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName(productName)
                .productDescription(productDescription)
                .productPrice(productPrice)
                .productQuantity(productQuantity)
                .build();
    }


}