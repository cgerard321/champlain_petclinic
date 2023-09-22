package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    Inventory inventory = Inventory.builder()
            .id("1")
            .inventoryId("1")
            .inventoryType(InventoryType.internal)
            .inventoryDescription("Medication for procedures")
            .build();



    @Test
    void addInventory_ValidInventory_shouldSucceed() {
        // Arrange
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(InventoryType.internal)
                .inventoryDescription("inventory_id1")
                .build();

        Inventory inventoryEntity = Inventory.builder()
                .inventoryId("inventoryId_1")
                .inventoryName("internal")
                .inventoryType(InventoryType.internal)
                .inventoryDescription("inventory_id1")
                .build();



        assertNotNull(inventoryEntity);


        when(inventoryRepository.insert(any(Inventory.class)))
                .thenReturn(Mono.just(inventoryEntity));


        Mono<InventoryResponseDTO> inventoryResponseDTO = productInventoryService.addInventory(Mono.just(inventoryRequestDTO));


        StepVerifier
                .create(inventoryResponseDTO)
                .expectNextMatches(foundInventory -> {
                    assertNotNull(foundInventory);
                    assertEquals(inventoryEntity.getInventoryId(), foundInventory.getInventoryId());
                    assertEquals(inventoryEntity.getInventoryName(), foundInventory.getInventoryName());
                    assertEquals(inventoryEntity.getInventoryType(), foundInventory.getInventoryType());
                    assertEquals(inventoryEntity.getInventoryDescription(), foundInventory.getInventoryDescription());
                    return true;
                })
                .verifyComplete();
    }


    @Test
    void addInventory_InValidInventoryTypeInventory_ThrowInvalidInputException() {
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("invtone")
                .inventoryType(null)
                .inventoryDescription("inventory_id1")
                .build();


        assertThrows(InvalidInputException.class, () -> {
            productInventoryService.addInventory(Mono.just(inventoryRequestDTO)).block();
        });


    }


    @Test
    void updateInventoryId_ValidInventoryIdAndRequest_ShouldUpdateAndReturnInventory() {
        String validInventoryId = "1";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .build();

        Inventory existingInventory = Inventory.builder()
                .inventoryId(validInventoryId)
                .id("123")
                .build();

        Inventory updatedInventoryEntity = Inventory.builder()

                .build();

        when(inventoryRepository.findInventoryByInventoryId(validInventoryId)).thenReturn(Mono.just(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(Mono.just(updatedInventoryEntity));

        StepVerifier
                .create(productInventoryService.updateInventory(Mono.just(inventoryRequestDTO), validInventoryId))
                .expectNextMatches(updatedCourse -> {

                    return true;
                })
                .verifyComplete();
    }


    @Test
    void updateInventoryId_InValidInventoryIdAndRequest_ShouldUpdateAndReturnInventory() {

        String invalidInventoryId = "145";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(InventoryType.internal)
                .inventoryDescription("Updated description")
                .build();


        when(inventoryRepository.findInventoryByInventoryId(invalidInventoryId)).thenReturn(Mono.empty());


        StepVerifier
                .create(productInventoryService.updateInventory(Mono.just(inventoryRequestDTO), invalidInventoryId))
                .expectError(NotFoundException.class)
                .verify();
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