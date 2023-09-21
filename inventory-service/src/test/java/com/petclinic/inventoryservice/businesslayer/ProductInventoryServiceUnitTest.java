package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
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
    @MockBean
    ProductRepository productRepository;
    @MockBean
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

    Product product = Product.builder()
            .productId("12345")
            .inventoryId("1")
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .build();

    Inventory inventory = Inventory.builder()
            .id("1")
            .inventoryId("1")
            .inventoryType("Medication")
            .inventoryDescription("Medication for procedures")
            .build();

    @Test
    void getAllProductsByInventoryId_withValidFields_shouldSucceed(){
        String inventoryId = "1";

        when(productRepository
                .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(
                        inventoryId,
                        null,
                        null,
                        null))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOFlux = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        null,
                        null);

        StepVerifier
                .create(productResponseDTOFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductName_andProductPrice_andProductQuantity_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        String productName = "Benzodiazepines";
        Double productPrice = 100.00;
        Integer productQuantity = 10;

        when(productRepository
                .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantity(
                        inventoryId,
                        productName,
                        productPrice,
                        productQuantity))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        productName,
                        productPrice,
                        productQuantity);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductPrice_andProductQuantity_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Double productPrice = 100.00;
        Integer productQuantity = 10;

        when(productRepository
                .findAllProductsByInventoryIdAndProductPriceAndProductQuantity(
                        inventoryId,
                        productPrice,
                        productQuantity))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        productPrice,
                        productQuantity);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductName_withValidFields_shouldSucceed(){
        String inventoryType = "1";
        String productName = "Benzodiazepines";

        when(productRepository
                .findAllProductsByInventoryIdAndProductName(
                        inventoryType,
                        productName))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryType,
                        productName,
                        null,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductPrice_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Double productPrice = 100.00;

        when(productRepository
                .findAllProductsByInventoryIdAndProductPrice(
                        inventoryId,
                        productPrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        productPrice,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductQuantity_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Integer productQuantity = 10;

        when(productRepository
                .findAllProductsByInventoryIdAndProductQuantity(
                        inventoryId,
                        productQuantity))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        null,
                        productQuantity);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

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