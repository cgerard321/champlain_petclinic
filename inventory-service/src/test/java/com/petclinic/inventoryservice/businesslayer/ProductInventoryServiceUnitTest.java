package com.petclinic.inventoryservice.businesslayer;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
            .inventoryType(InventoryType.internal)
            .inventoryDescription("Medication for procedures")
            .build();

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


    //delete
    @Test
    void deleteAllProductInventory_ValidInventoryId_ShouldDeleteAllProducts() {
        // Arrange
        String inventoryId = "1";

        // Mock returning an inventory when searched by the inventoryId
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));

        // Mock the behavior of deleting all products by inventoryId
        Mockito.when(productRepository.deleteByInventoryId(inventoryId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = productInventoryService.deleteAllProductInventory(inventoryId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(productRepository, Mockito.times(1)).deleteByInventoryId(inventoryId);
    }


    @Test
    void deleteAllProductInventory_InvalidInventoryId_ShouldThrowException() {
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());

        // Act and Assert
        Mono<Void> result = productInventoryService.deleteAllProductInventory(inventoryId);
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void deleteAllInventory_ShouldDeleteAllInventories() {
        // Arrange
        Mockito.when(inventoryRepository.deleteAll()).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = productInventoryService.deleteAllInventory();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        Mockito.verify(inventoryRepository, Mockito.times(1)).deleteAll();
    }
}