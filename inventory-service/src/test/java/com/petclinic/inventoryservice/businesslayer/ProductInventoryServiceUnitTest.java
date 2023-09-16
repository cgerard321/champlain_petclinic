package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProductInventoryServiceUnitTest {
    @Autowired
    ProductInventoryService productInventoryService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryRepository inventoryRepository;


    ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
            .id("1")
            .inventoryId("1")
            .productId(UUID.randomUUID().toString())
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .build();

    Inventory inventory = Inventory.builder()
            .id("1")
            .inventoryId("1")
            .inventoryType(InventoryType.internal)
            .inventoryDescription("Medication for procedures")
            .build();


//    @Test
//    void createProduct_validProduct_ShouldSucceed(){
//        // Arrange
//        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
//                .productName("Benzodiazepines")
//                .productDescription("Sedative Medication")
//                .productPrice(100.00)
//                .productQuantity(10)
//                .build();
//        Product product = Product.builder()
//                .productName("Benzodiazepines")
//                .productDescription("Sedative Medication") // Fix the field name here
//                .productPrice(100.00)
//                .productQuantity(10)
//                .sku("123F567C9")
//                .inventoryId("1")
//                .id("1")
//                .build();
//        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));
//        // Verify that the save method was called with any Product instance
//        verify(productRepository).save(any(Product.class));
//
//        //act
//        Mono<ProductResponseDTO> productResponseDTOMono = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), "1");
//        //assert
//        StepVerifier
//                .create(productResponseDTOMono)
//                .consumeNextWith(productResponseDTO -> {
//                    assertNotNull(productResponseDTO);
//                    assertEquals("1", productResponseDTO.getId());
//                    assertEquals("1", productResponseDTO.getInventoryId());
//                    assertEquals("123F567C9", productResponseDTO.getSku());
//                    assertEquals("Benzodiazepines", productResponseDTO.getProductName());
//                    assertEquals("Sedative Medication", productResponseDTO.getProductDescription());
//                    assertEquals(100.00, productResponseDTO.getProductPrice());
//                    assertEquals(10, productResponseDTO.getProductQuantity());
//                })
//                .verifyComplete();
//    }



}