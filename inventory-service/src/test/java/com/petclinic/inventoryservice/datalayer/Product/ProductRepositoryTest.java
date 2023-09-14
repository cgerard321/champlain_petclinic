package com.petclinic.inventoryservice.datalayer.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;


    private Product buildProduct(String inventoryId, String sku, String productName, String productDescription, Double productPrice, Integer productQuantity) {
        return Product.builder()
                .inventoryId(inventoryId)
                .sku(sku)
                .productName(productName)
                .productDescription(productDescription)
                .productPrice(productPrice)
                .productQuantity(productQuantity)
                .build();
    }


}