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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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
    ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
              .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
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
    public void deleteProduct_validProductAndInventory_ShouldSucceed() {
        //arrange
        String productId = "123F567C9";
        when(productRepository.deleteByProductId(productId)).thenReturn(Mono.empty());
        when(inventoryRepository.existsByInventoryId(product.getInventoryId())).thenReturn(Mono.just(true));
        when(productRepository.existsByProductId(productId)).thenReturn(Mono.just(true));
        Mono<Void> deletedProduct = productInventoryService.deleteProductInInventory(product.getInventoryId(), productId);
        //act and assert
        StepVerifier
                .create(deletedProduct)
                .expectNextCount(0)
                .verifyComplete();

        verify(productRepository).deleteByProductId(productId);
    }



    @Test
    void getInventoryByInventoryId_ValidId_shouldSucceed(){
        String inventoryId ="1";
        //arrange
        when(inventoryRepository
                .findInventoryByInventoryId(
                        inventoryId))
                .thenReturn(Mono.just(inventory));

        //act
        Mono<InventoryResponseDTO> inventoryResponseDTOMono = productInventoryService
                .getInventoryById(inventory.getInventoryId());

        //assert
        StepVerifier
                .create(inventoryResponseDTOMono)
                .consumeNextWith(foundInventory ->{
                    assertNotNull(foundInventory);
                    assertEquals(inventory.getInventoryId(), foundInventory.getInventoryId());
                    assertEquals(inventory.getInventoryName(), foundInventory.getInventoryName());
                    assertEquals(inventory.getInventoryType(), foundInventory.getInventoryType());
                    assertEquals(inventory.getInventoryDescription(), foundInventory.getInventoryDescription());

                })
                .verifyComplete();
    }



    @Test
    void GetInventoryByInvalid_InventoryId_throwNotFound() {

        String invalidInventoryId = "145";

        when(inventoryRepository.findInventoryByInventoryId(invalidInventoryId)).thenReturn(Mono.empty());


        Mono<InventoryResponseDTO> inventoryResponseDTOMono = productInventoryService.getInventoryById(invalidInventoryId);

        StepVerifier.create(inventoryResponseDTOMono)
                .expectError(NotFoundException.class)
                .verify();
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
                .expectNextMatches(updatedInventory -> {

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

    @Test
    void updateProductInInventory_ValidRequest_ShouldUpdateAndReturnProduct() {
        // Arrange
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();



        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productPrice(99.99)
                .productQuantity(20)
                .build();

        Inventory inventory = Inventory.builder()
                .id("1")
                .inventoryId("1")
                .inventoryType(InventoryType.internal)
                .inventoryDescription("Medication for procedures")
                .build();

        Product existingProduct = Product.builder()
                .id("1")
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Original Product Name")
                .productPrice(50.0)
                .productQuantity(10)
                .build();

        Product updatedProduct = Product.builder()
                .id("1")
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Updated Product Name")
                .productPrice(99.99)
                .productQuantity(20)
                .build();

        when(inventoryRepository.findInventoryByInventoryId(anyString())).thenReturn(Mono.just(inventory));

        when(productRepository.findProductByProductId(anyString())).thenReturn(Mono.just(existingProduct));

        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        // Act and Assert
        StepVerifier
                .create(productInventoryService.updateProductInInventory(Mono.just(productRequestDTO), inventoryId, productId))
                .expectNextMatches(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(productId, responseDTO.getProductId());
                    assertEquals("Updated Product Name", responseDTO.getProductName());
                    assertEquals(99.99, responseDTO.getProductPrice());
                    assertEquals(20, responseDTO.getProductQuantity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void updateProductInInventory_ProductNotFound_ShouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productPrice(99.99)
                .productQuantity(20)
                .build();

        when(inventoryRepository.findInventoryByInventoryId(anyString())).thenReturn(Mono.empty());

        // Act and Assert
        StepVerifier
                .create(productInventoryService.updateProductInInventory(Mono.just(productRequestDTO), inventoryId, productId))
                .expectErrorMatches(throwable -> {
                    assertEquals("Inventory not found with id: " + inventoryId, throwable.getMessage());
                    return throwable instanceof NotFoundException;
                })
                .verify();
    }

    @Test
    void updateProductInInventory_InvalidInput_ShouldThrowInvalidInputException() {
        // Arrange
        String inventoryId = "1";
        String productId = "2";

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productPrice(-99.99)
                .productQuantity(20)
                .build();

        Inventory inventory = Inventory.builder()
                .id("1")
                .inventoryId("1")
                .inventoryType(InventoryType.internal)
                .inventoryDescription("Medication for procedures")
                .build();

        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));

        // Act and Assert
        Throwable exception = assertThrows(InvalidInputException.class, () -> {
            productInventoryService.updateProductInInventory(Mono.just(productRequestDTO), inventoryId, productId).block();
        });

        // Assert
        assertTrue(exception.getMessage().contains("Product price and quantity must be greater than 0."));
    }
    public void deleteProduct_InvalidInventoryId_ShouldNotFound(){
        //arrange
        String invalidInventoryId = "invalid";
        when(inventoryRepository.existsByInventoryId(invalidInventoryId)).thenReturn(Mono.just(false));
        when(productRepository.existsByProductId(product.getProductId())).thenReturn(Mono.just(true));

        //act
        Mono<Void> setup = productInventoryService.deleteProductInInventory(invalidInventoryId, product.getProductId());
        //assert
        StepVerifier
                .create(setup)
                .expectError(NotFoundException.class) // Expect a NotFoundException
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

    @Test
    void addProductToInventory_ShouldSucceed(){
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        Mockito.when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(product));
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .assertNext(productResponseDTO -> {
                    assertNotNull(productResponseDTO);
                    assertEquals(productResponseDTO.getInventoryId(), inventoryId);
                    assertEquals(productResponseDTO.getProductName(), productRequestDTO.getProductName());
                    assertEquals(productResponseDTO.getProductPrice(), productRequestDTO.getProductPrice());
                    assertEquals(productResponseDTO.getProductQuantity(), productRequestDTO.getProductQuantity());
                })
                .verifyComplete();
        Mockito.verify(productRepository, Mockito.times(1)).save(any(Product.class));
    }

    @Test
    void addProductToInventory_WithInvalidInventoryId_ShouldThrowNotFoundException(){
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    assertEquals("Inventory not found with id: " + inventoryId, throwable.getMessage());
                    return throwable instanceof NotFoundException;
                })
                .verify();
    }

    @Test
    void addProductToInventory_WithInvalidProductRequest_ShouldThrowInvalidInputException(){
        // Arrange
        String inventoryId = "1";
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(-100.00)
                .productQuantity(10)
                .build();
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    assertEquals("Product price and quantity must be greater than 0.", throwable.getMessage());
                    return throwable instanceof InvalidInputException;
                })
                .verify();
    }
    //for search
    //SearchInventory
    @Test
    void searchInventories_WithNameTypeAndDescription_shouldSucceed() {
        String inventoryName = "SampleName";
        String inventoryType = "SampleType";
        String inventoryDescription = "SampleDescription";

        when(inventoryRepository
                .findAllByInventoryNameAndInventoryTypeAndInventoryDescription(inventoryName, inventoryType, inventoryDescription))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService.searchInventories(inventoryName, inventoryType, inventoryDescription);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithTypeAndDescription_shouldSucceed() {
        String inventoryType = "SampleType";
        String inventoryDescription = "SampleDescription";

        when(inventoryRepository
                .findAllByInventoryTypeAndInventoryDescription(inventoryType, inventoryDescription))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(null, inventoryType, inventoryDescription);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithName_shouldSucceed() {
        String inventoryName = "SampleName";

        when(inventoryRepository.findAllByInventoryName(inventoryName))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(inventoryName, null, null);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithType_shouldSucceed() {
        String inventoryType = "SampleType";

        when(inventoryRepository.findAllByInventoryType(inventoryType))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(null, inventoryType, null);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithDescription_shouldSucceed() {
        String inventoryDescription = "SampleDescription";

        when(inventoryRepository.findAllByInventoryDescription(inventoryDescription))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(null, null, inventoryDescription);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithNoFilters_shouldFetchAll() {
        when(inventoryRepository.findAll())
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(null, null, null);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }


}
