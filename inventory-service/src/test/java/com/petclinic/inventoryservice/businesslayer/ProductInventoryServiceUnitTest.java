package com.petclinic.inventoryservice.businesslayer;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.ImageUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

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
    @MockBean
    InventoryTypeRepository inventoryTypeRepository;

    //    ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
//            .inventoryId("1")
//            .productId(UUID.randomUUID().toString())
//            .productName("Benzodiazepines")
//            .productDescription("Sedative Medication")
//            .productPrice(100.00)
//            .productQuantity(10)
//            .productSalePrice(15.99)
//            .build();
    Product product = Product.builder()
            .id(UUID.randomUUID().toString())
            .productId("12345")
            .inventoryId("1")
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .productSalePrice(15.99)
            .status(Status.RE_ORDER)
            .build();
    Product product1 = Product.builder()
            .id(UUID.randomUUID().toString())
            .productId("123456")
            .inventoryId("1")
            .productName("Antibenzo")
            .productDescription("Sedative Medication")
            .productPrice(768.00)
            .productQuantity(100)
            .status(Status.AVAILABLE)
            .build();
    Product product2 = Product.builder()
            .id(UUID.randomUUID().toString())
            .productId("1234567")
            .inventoryId("1")
            .productName("ibuprofen")
            .productDescription("Sedative Medication")
            .productPrice(200.00)
            .productQuantity(50)
            .status(Status.AVAILABLE)
            .build();
    InventoryType inventoryType = InventoryType.builder()
            .id("1")
            .typeId("81445f86-5329-4df6-badc-8f230ee07e75")
            .type("Internal")
            .build();

    InputStream inputStream = getClass().getResourceAsStream("/images/DiagnosticKitImage.jpg");
    byte[] diagnosticKitImage = ImageUtil.readImage(inputStream);

    Inventory inventory = Inventory.builder()
            .id("1")
            .inventoryId("1")
            .inventoryCode("INV-0001")
            .inventoryType(inventoryType.getType())
            .inventoryDescription("Medication for procedures")
            .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
            .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
            .imageUploaded(diagnosticKitImage)
            .build();
    ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .productSalePrice(15.99)
            .build();

    Product lowStockProduct = Product.builder()
            .productId("12346")
            .inventoryId("1")
            .productName("Ibuprofen")
            .productDescription("Pain reliever")
            .productPrice(50.00)
            .productQuantity(3) // low stock
            .productSalePrice(8.99)
            .build();

    ProductInventoryServiceUnitTest() throws IOException {
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldPagination_ShouldSucceed(){
        Pageable pageable = PageRequest.of(0, 2);

        when(productRepository.findAllProductsByInventoryId(product.getInventoryId()))
                .thenReturn(Flux.just(product, product2, product1));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(product.getInventoryId(), null,null, null,
                        pageable);
        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithPriceAndQuantity_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductPriceAndProductQuantity(
                product.getInventoryId(), 10.00, 5))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), null, 10.00, 5, pageable);

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithPriceOnly_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductPrice(
                product.getInventoryId(), 10.00))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), null, 10.00, null, pageable);

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithQuantityOnly_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductQuantity(
                product.getInventoryId(), 5))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), null, null, 5, pageable);

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithNameOnly_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductName(
                product.getInventoryId(), "name"))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), "name", null, null, pageable);

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getAllProductsByInventoryId_andProductName_andProductPrice_andProductQuantity_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        String productName = "Benzodiazepines";
        Double productPrice = 100.00;
        Integer productQuantity = 10;
        Double productSalePrice = 200.00;



        when(productRepository
                .findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantityAndProductSalePrice(
                        inventoryId,
                        productName,
                        productPrice,
                        productQuantity,
                        productSalePrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        productName,
                        productPrice,
                        productQuantity,
                        productSalePrice);

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
                        productQuantity,
                        null
                );

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }
    /*
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
    */
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
                        null,
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
                        productQuantity,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductSalePrice_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Double productSalePrice = 20.00;

        when(productRepository
                .findAllProductsByInventoryIdAndProductSalePrice(
                        inventoryId,
                        productSalePrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        null,
                        null,
                        productSalePrice);

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
    void getProductsByInventoryId_andProductId_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productId = "12345";

        when(productRepository
                .findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.just(product));

        Mono<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductByProductIdInInventory(inventoryId, productId);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    void getProductsByInventoryId_andProductId_withInvalidFields_shouldReturnEmptyFlux() {
        String invalidInventoryId = "999"; // Invalid inventory ID
        String invalidProductId = "invalid123"; // Invalid product ID

        when(productRepository
                .findProductByInventoryIdAndProductId(invalidInventoryId, invalidProductId))
                .thenReturn(Mono.empty());

        Mono<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductByProductIdInInventory(invalidInventoryId, invalidProductId);


        StepVerifier.create(productResponseDTOMono)
                .expectError(NotFoundException.class)
                .verify();
    }


//    @Test
//    void getInventoryByInventoryId_ValidId_shouldSucceed(){
//        String inventoryId ="1";
//        //arrange
//        when(inventoryRepository
//                .findInventoryByInventoryId(
//                        inventoryId))
//                .thenReturn(Mono.just(inventory));
//
//        //act
//        Mono<InventoryResponseDTO> inventoryResponseDTOMono = productInventoryService
//                .getInventoryById(inventory.getInventoryId());
//
//        //assert
//        StepVerifier
//                .create(inventoryResponseDTOMono)
//                .consumeNextWith(foundInventory ->{
//                    assertNotNull(foundInventory);
//                    assertEquals(inventory.getInventoryId(), foundInventory.getInventoryId());
//                    assertEquals(inventory.getInventoryName(), foundInventory.getInventoryName());
//                    assertEquals(inventory.getInventoryType(), foundInventory.getInventoryType());
//                    assertEquals(inventory.getInventoryDescription(), foundInventory.getInventoryDescription());
//
//                })
//                .verifyComplete();
//    }




    @Test
    void GetInventoryByInvalid_InventoryId_throwNotFound() {

        String invalidInventoryId = "145";

        when(inventoryRepository.findInventoryByInventoryId(invalidInventoryId)).thenReturn(Mono.empty());


        Mono<InventoryResponseDTO> inventoryResponseDTOMono = productInventoryService.getInventoryById(invalidInventoryId);

        StepVerifier.create(inventoryResponseDTOMono)
                .expectError(NotFoundException.class)
                .verify();
    }




//    @Test
//    void addInventory_ValidInventory_shouldSucceed() {
//        // Arrange
//        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
//                .inventoryName("internal")
//                .inventoryType(inventoryType.getType())
//                .inventoryDescription("inventory_id1")
//                .build();
//
//        Inventory inventoryEntity = Inventory.builder()
//                .inventoryId("inventoryId_1")
//                .inventoryName("internal")
//                .inventoryType(inventoryType.getType())
//                .inventoryDescription("inventory_id1")
//                .build();
//
//
//
//        assertNotNull(inventoryEntity);
//
//
//        when(inventoryRepository.insert(any(Inventory.class)))
//                .thenReturn(Mono.just(inventoryEntity));
//
//
//        Mono<InventoryResponseDTO> inventoryResponseDTO = productInventoryService.addInventory(Mono.just(inventoryRequestDTO));
//
//
//        StepVerifier
//                .create(inventoryResponseDTO)
//                .expectNextMatches(foundInventory -> {
//                    assertNotNull(foundInventory);
//                    assertEquals(inventoryEntity.getInventoryId(), foundInventory.getInventoryId());
//                    assertEquals(inventoryEntity.getInventoryName(), foundInventory.getInventoryName());
//                    assertEquals(inventoryEntity.getInventoryType(), foundInventory.getInventoryType());
//                    assertEquals(inventoryEntity.getInventoryDescription(), foundInventory.getInventoryDescription());
//                    return true;
//                })
//                .verifyComplete();
//    }


    @Test
    void addInventory_InValidInventoryTypeInventory_ThrowInvalidInputException() {
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("invtone")
                .inventoryType(null)
                .inventoryDescription("inventory_id1")
                .build();

        when(inventoryRepository.count()).thenReturn(Mono.just(0L));
        assertThrows(InvalidInputException.class, () -> {
            productInventoryService.addInventory(Mono.just(inventoryRequestDTO)).block();
        });


    }




    @Test
    void updateInventoryId_InValidInventoryIdAndRequest_ShouldUpdateAndReturnInventory() {

        String invalidInventoryId = "145";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(inventoryType.getType())
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
                .productDescription("Description")
                .productSalePrice(15.99)
                .build();

        Inventory inventory = Inventory.builder()
                .id("1")
                .inventoryId("1")
                .inventoryType(inventoryType.getType())
                .inventoryDescription("Medication for procedures")
                .build();

        Product existingProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Original Product Name")
                .productPrice(50.0)
                .productQuantity(10)
                .productSalePrice(10.10)
                .build();

        Product updatedProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Updated Product Name")
                .productPrice(99.99)
                .productQuantity(20)
                .productDescription("Description")
                .productSalePrice(10.10)
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
                    assertEquals("Description",responseDTO.getProductDescription());
                    assertEquals(10.10, responseDTO.getProductSalePrice());
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
                .productPrice(-99.99)
                .productQuantity(20)
                .productDescription("Description")
                .productSalePrice(10.10)
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
                .productDescription("Description")
                .productSalePrice(10.10)
                .build();

        Inventory inventory = Inventory.builder()
                .id("1")
                .inventoryId("1")
                .inventoryType(inventoryType.getType())
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

    @Test
    void updateProductInInventory_InvalidInputNull_ShouldThrowInvalidInputException() {
        // Arrange
        String inventoryId = "1";
        String productId = "2";

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName(null)
                .productPrice(null)
                .productQuantity(null)
                .productDescription(null)
                .productSalePrice(null)
                .build();

        // Mock the behavior of the inventoryRepository to return a dummy Inventory object
        when(inventoryRepository.findInventoryByInventoryId(eq(inventoryId)))
                .thenReturn(Mono.just(new Inventory()));

        // Act and Assert
        StepVerifier.create(productInventoryService.updateProductInInventory(Mono.just(productRequestDTO), inventoryId, productId))
                .expectError(InvalidInputException.class)
                .verify();

        // You can also assert the exception message here if needed
    }

    //    public void deleteProduct_InvalidInventoryId_ShouldNotFound(){
//        //arrange
//        String invalidInventoryId = "invalid";
//        when(inventoryRepository.existsByInventoryId(invalidInventoryId)).thenReturn(Mono.just(false));
//        when(productRepository.existsByProductId(product.getProductId())).thenReturn(Mono.just(true));
//
//        //act
//        Mono<Void> setup = productInventoryService.deleteProductInInventory(invalidInventoryId, product.getProductId());
//        //assert
//        StepVerifier
//                .create(setup)
//                .expectError(NotFoundException.class) // Expect a NotFoundException
//                .verify();
//    }
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
        Mono<Void> result = productInventoryService.deleteAllProductsForAnInventory(inventoryId);

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
        Mono<Void> result = productInventoryService.deleteAllProductsForAnInventory(inventoryId);
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
    void addSupplyToInventory_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        Mockito.when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(product));
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .assertNext(productResponseDTO -> {
                    assertNotNull(productResponseDTO);
                    assertEquals(productResponseDTO.getInventoryId(), inventoryId);
                    assertEquals(productResponseDTO.getProductName(), productRequestDTO.getProductName());
                    assertEquals(productResponseDTO.getProductPrice(), productRequestDTO.getProductPrice());
                    assertEquals(productResponseDTO.getProductQuantity(), productRequestDTO.getProductQuantity());
                    assertEquals(productResponseDTO.getProductDescription(),productRequestDTO.getProductDescription());
                    assertEquals(productResponseDTO.getProductSalePrice(), productRequestDTO.getProductSalePrice());
                })
                .verifyComplete();
        Mockito.verify(productRepository, Mockito.times(1)).save(any(Product.class));
    }
    @Test
    void addSupplyToInventory_WithInvalidInventoryId_ShouldThrowNotFoundException(){
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    assertEquals("Inventory not found with id: " + inventoryId, throwable.getMessage());
                    return throwable instanceof NotFoundException;
                })
                .verify();
    }

    @Test
    void addSupplyToInventory_WithInvalidProductRequest_ShouldThrowInvalidInputException(){
        // Arrange
        String inventoryId = "1";
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(-100.00)
                .productQuantity(10)
                .productDescription("Description")
                .productSalePrice(10.10)
                .build();
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(Mono.just(productRequestDTO), inventoryId);
        //Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    assertEquals("Product price and quantity must be greater than 0.", throwable.getMessage());
                    return throwable instanceof InvalidInputException;
                })
                .verify();
    }

//    @Test
//    void addProductToInventory_ShouldSucceed(){
//        // Arrange
//        String inventoryId = "1";
//        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
//                .thenReturn(Mono.just(inventory));
//        Mockito.when(productRepository.save(any(Product.class)))
//                .thenReturn(Mono.just(product));
//        //Act
//        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
//        //Assert
//        StepVerifier.create(result)
//                .assertNext(productResponseDTO -> {
//                    assertNotNull(productResponseDTO);
//                    assertEquals(productResponseDTO.getInventoryId(), inventoryId);
//                    assertEquals(productResponseDTO.getProductName(), productRequestDTO.getProductName());
//                    assertEquals(productResponseDTO.getProductPrice(), productRequestDTO.getProductPrice());
//                    assertEquals(productResponseDTO.getProductQuantity(), productRequestDTO.getProductQuantity());
//                    assertEquals(productResponseDTO.getProductDescription(),productRequestDTO.getProductDescription());
//                    assertEquals(productResponseDTO.getProductSalePrice(), productRequestDTO.getProductSalePrice());
//                })
//                .verifyComplete();
//        Mockito.verify(productRepository, Mockito.times(1)).save(any(Product.class));
//    }

//    @Test
//    void addProductToInventory_WithInvalidInventoryId_ShouldThrowNotFoundException(){
//        // Arrange
//        String inventoryId = "1";
//        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
//                .thenReturn(Mono.empty());
//        //Act
//        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
//        //Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> {
//                    assertEquals("Inventory not found with id: " + inventoryId, throwable.getMessage());
//                    return throwable instanceof NotFoundException;
//                })
//                .verify();
//    }

//    @Test
//    void addProductToInventory_WithInvalidProductRequest_ShouldThrowInvalidInputException(){
//        // Arrange
//        String inventoryId = "1";
//        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
//                .productName("Benzodiazepines")
//                .productDescription("Sedative Medication")
//                .productPrice(-100.00)
//                .productQuantity(10)
//                .productDescription("Description")
//                .productSalePrice(10.10)
//                .build();
//        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
//                .thenReturn(Mono.just(inventory));
//        //Act
//        Mono<ProductResponseDTO> result = productInventoryService.addProductToInventory(Mono.just(productRequestDTO), inventoryId);
//        //Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> {
//                    assertEquals("Product price and quantity must be greater than 0.", throwable.getMessage());
//                    return throwable instanceof InvalidInputException;
//                })
//                .verify();
//    }
    //for search
    //SearchInventory
//    @Test
//    void searchInventories_WithNameTypeAndDescription_shouldSucceed() {
//        String inventoryName = "SampleName";
//        String inventoryType = "SampleType";
//        String inventoryDescription = "SampleDescription";
//        Optional<Integer> page = Optional.of(0);
//        Optional<Integer> size = Optional.of(10);
//
//        when(inventoryRepository
//                .findAllByInventoryNameAndInventoryTypeAndInventoryDescription(inventoryName, inventoryType, inventoryDescription))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> responseFlux = productInventoryService.searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)),inventoryName, inventoryType, inventoryDescription);
//
//        StepVerifier
//                .create(responseFlux)
//                .expectNextCount(1)
//                .verifyComplete();
//    }

//    @Test
//    void searchInventories_WithTypeAndDescription_shouldSucceed() {
//        String inventoryType = "SampleType";
//        String inventoryDescription = "SampleDescription";
//        Optional<Integer> page = Optional.of(0);
//        Optional<Integer> size = Optional.of(10);
//
//        when(inventoryRepository
//                .findAllByInventoryTypeAndInventoryDescription(inventoryType, inventoryDescription))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> responseFlux = productInventoryService
//                .searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)), null, inventoryType, inventoryDescription);
//
//        StepVerifier
//                .create(responseFlux)
//                .expectNextCount(1)
//                .verifyComplete();
//    }
/*
    @Test
    void searchInventories_WithName_shouldSucceed() {
        String inventoryName = "SampleName";
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(10);

        when(inventoryRepository.findAllByInventoryName(inventoryName))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)), inventoryName, null, null);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

 */

//    @Test
//    void searchInventories_WithType_shouldSucceed() {
//        String inventoryType = "SampleType";
//        Optional<Integer> page = Optional.of(0);
//        Optional<Integer> size = Optional.of(10);
//
//        when(inventoryRepository.findAllByInventoryType(inventoryType))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> responseFlux = productInventoryService
//                .searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)),null, inventoryType, null);
//
//        StepVerifier
//                .create(responseFlux)
//                .expectNextCount(1)
//                .verifyComplete();
//    }
/*
    @Test
    void searchInventories_WithDescription_shouldSucceed() {
        String inventoryDescription = "SampleDescription";
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(10);

        when(inventoryRepository.findAllByInventoryDescription(inventoryDescription))
                .thenReturn(Flux.just(inventory));

        Flux<InventoryResponseDTO> responseFlux = productInventoryService
                .searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)),null, null, inventoryDescription);

        StepVerifier
                .create(responseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }
*/
//    @Test
//    void searchInventories_WithNoFilters_shouldFetchAll() {
//        when(inventoryRepository.findAll())
//                .thenReturn(Flux.just(inventory));
//        Optional<Integer> page = Optional.of(0);
//        Optional<Integer> size = Optional.of(10);
//
//        Flux<InventoryResponseDTO> responseFlux = productInventoryService
//                .searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)),null, null, null);
//
//        StepVerifier
//                .create(responseFlux)
//                .expectNextCount(1)
//                .verifyComplete();
//    }



    @Test
    public void deleteInventoryByInventoryId_validInventory_ShouldSucceed() {
        // Arrange
        String validInventoryId = "1";

        when(inventoryRepository.findInventoryByInventoryId(validInventoryId)).thenReturn(Mono.just(inventory));

        when(inventoryRepository.delete(inventory)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(productInventoryService.deleteInventoryByInventoryId(validInventoryId))
                .verifyComplete();

        verify(inventoryRepository).findInventoryByInventoryId(validInventoryId);
        verify(inventoryRepository).delete(inventory);
    }

    @Test
    public void deleteInventoryByInventoryId_invalidInventory_ShouldThrowNotFoundException() {
        // Arrange
        String invalidInventoryId = "nonexistentId";

        when(inventoryRepository.findInventoryByInventoryId(invalidInventoryId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(productInventoryService.deleteInventoryByInventoryId(invalidInventoryId))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException && throwable.getMessage().equals("The Inventory with the inventoryId: " + invalidInventoryId + " is invalid. Please enter a valid inventory id."))
                .verify();

        verify(inventoryRepository).findInventoryByInventoryId(invalidInventoryId);
        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    public void addInventoryType_shouldSucceed(){
        // Arrange
        InventoryTypeRequestDTO inventoryTypeRequestDTO = InventoryTypeRequestDTO.builder()
                .type("Internal")
                .build();

        assertNotNull(inventoryType);


        when(inventoryTypeRepository.insert(any(InventoryType.class)))
                .thenReturn(Mono.just(inventoryType));


        Mono<InventoryTypeResponseDTO> inventoryTypeResponseDTO = productInventoryService.addInventoryType(Mono.just(inventoryTypeRequestDTO));


        StepVerifier
                .create(inventoryTypeResponseDTO)
                .expectNextMatches(foundInventoryType -> {
                    assertNotNull(foundInventoryType);
                    assertEquals(inventoryType.getType(), foundInventoryType.getType());
                    return true;
                })
                .verifyComplete();
    }




    @Test
    public void getAllInventoryTypes_ShouldSucceed(){
        when(inventoryTypeRepository.findAll())
                .thenReturn(Flux.just(inventoryType));
        Flux<InventoryTypeResponseDTO> inventoryTypeResponseDTOFlux = productInventoryService.getAllInventoryTypes();

        StepVerifier
                .create(inventoryTypeResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();

    }

//search
    /*
    @Test
    void testSearchInventories_TypeAndDescriptionGiven() {
        // Given
        String testType = "Internal";
        String testDescription = "Medication for procedures";
=======
>>>>>>> d1e75733 (fixing a bit of code because of the quodana requirement)



*/

    //search inventories

//    @Test
//    void searchInventories_withAllParams_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);  // Example pageable
//        String name = "Benzodiazepines";
//        String type = "Internal";
//        String description = "Medication for procedures";
//
//        when(inventoryRepository.findAllByInventoryNameAndInventoryTypeAndInventoryDescription(name, type, description))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(page, name, type, description);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }

//    @Test
//    void searchInventories_withTypeAndDescription_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);
//        String type = "Internal";
//        String description = "Medication for procedures";
//
//        when(inventoryRepository.findAllByInventoryTypeAndInventoryDescription(type, description))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result =  productInventoryService.searchInventories(page, null, type, description);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }

    // Similarly, create other test methods for different combinations of parameters and scenarios...

//    @Test
//    void searchInventories_withOnlyName_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);
//        String name = "Benzodiazepines";
//
//        when(inventoryRepository.findByInventoryNameRegex(anyString()))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result =  productInventoryService.searchInventories(page, name, null, null);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }
//    @Test
//    void searchInventories_withNameLengthOne_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);
//        String name = "B";  // Assuming 'B' is the starting letter for some inventory names
//
//        when(inventoryRepository.findByInventoryNameRegex(anyString()))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(page, name, null, null);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }

//    @Test
//    void searchInventories_withDescriptionLengthOne_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);
//        String description = "M";  // Assuming 'M' is the starting letter for some inventory descriptions
//
//        when(inventoryRepository.findByInventoryDescriptionRegex(anyString()))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(page, null, null, description);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }

//    @Test
//    void searchInventories_withDescriptionLongerThanOne_shouldReturnResults() {
//        Pageable page = PageRequest.of(0, 5);
//        String description = "Medication";  // Longer than one character
//
//        when(inventoryRepository.findByInventoryDescriptionRegex(anyString()))
//                .thenReturn(Flux.just(inventory));
//
//        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(page, null, null, description);
//
//        StepVerifier.create(result)
//                .expectNextCount(1)
//                .verifyComplete();
//    }


    @Test
    void searchInventories_withDescriptionNotExisting_shouldThrowError() {
        Pageable page = PageRequest.of(0, 5);
        String description = "NonExistingDescription";

        when(inventoryRepository.findByInventoryDescriptionRegex(anyString()))
                .thenReturn(Flux.empty());  // No inventory found

        Flux<InventoryResponseDTO> result =productInventoryService.searchInventories(page, null,null, null, description, false);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }
    @Test
    void getProductsByInventoryIdAndProductName_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        String productName = "B";
        String regexPattern = "(?i)^" + Pattern.quote(productName) + ".*";

        when(productRepository
                .findAllProductsByInventoryIdAndProductNameRegex(
                        inventoryId,
                        regexPattern))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        productName,
                        null,
                        null,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_withValidFields_shouldSucceed(){
        String inventoryId = "1";

        when(productRepository
                .findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        null,
                        null,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchInventories_WithInventoryCode_ShouldReturnInventory() {
        Pageable pageable = PageRequest.of(0, 10);
        String inventoryCode = "INV-0001";

        when(inventoryRepository.findInventoryByInventoryCode(inventoryCode))
                .thenReturn(Mono.just(inventory));

        // ✅ add this line
        when(productRepository.countByInventoryIdAndLastUpdatedAtAfter(eq("1"), any(LocalDateTime.class)))
                .thenReturn(Mono.just(0L));

        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(
                pageable, inventoryCode, null, null, null, null);

        StepVerifier.create(result)
                .expectNextMatches(inv -> inv.getInventoryCode().equals(inventoryCode))
                .verifyComplete();
    }

    @Test
    void searchInventories_WithInvalidInventoryCode_ShouldThrowNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        String invalidCode = "INV-9999";

        when(inventoryRepository.findInventoryByInventoryCode(invalidCode))
                .thenReturn(Mono.empty());

        Flux<InventoryResponseDTO> result = productInventoryService.searchInventories(
                pageable, invalidCode, null, null, null, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }


//    @Test
//    void getProductsInInventoryByInventoryIdAndProductFieldPagination_ShouldSucceed(){
//        Pageable pageable = PageRequest.of(0, 2);
//
//        when(productRepository.findAllProductsByInventoryId(product.getInventoryId()))
//                .thenReturn(Flux.just(product, product2, product1));
//
//        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
//                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(product.getInventoryId(), null,null, null,
//                        pageable);
//        StepVerifier.create(productResponseDTOMono)
//                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
//                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
//                .expectComplete()
//                .verify();
//    }

    @Test
    void getLowStockProducts_WithLowStock_ShouldReturnResults() {
        // Arrange
        String inventoryId = "1";
        int stockThreshold = 5;

        when(productRepository.findAllByInventoryIdAndProductQuantityLessThan(inventoryId, stockThreshold))
                .thenReturn(Flux.just(lowStockProduct));

        // Act
        Flux<ProductResponseDTO> result = productInventoryService.getLowStockProducts(inventoryId, stockThreshold);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(product -> product.getProductQuantity() < stockThreshold)
                .verifyComplete();
    }

    @Test
    void getLowStockProducts_NoLowStock_ShouldReturnEmpty() {
        // Arrange
        String inventoryId = "1";
        int stockThreshold = 2;  // All products have stock >= 2

        when(productRepository.findAllByInventoryIdAndProductQuantityLessThan(inventoryId, stockThreshold))
                .thenReturn(Flux.empty());

        // Act
        Flux<ProductResponseDTO> result = productInventoryService.getLowStockProducts(inventoryId, stockThreshold);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getLowStockProducts_WithNoMatchingInventory_ShouldThrowNotFound() {
        // Arrange
        String invalidInventoryId = "999";
        int stockThreshold = 5;

        when(productRepository.findAllByInventoryIdAndProductQuantityLessThan(invalidInventoryId, stockThreshold))
                .thenReturn(Flux.empty());

        // Act
        Flux<ProductResponseDTO> result = productInventoryService.getLowStockProducts(invalidInventoryId, stockThreshold);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getLowStockProducts_MultipleProductsBelowThreshold_ShouldReturnAll() {
        // Arrange
        String inventoryId = "1";
        int stockThreshold = 10;

        when(productRepository.findAllByInventoryIdAndProductQuantityLessThan(inventoryId, stockThreshold))
                .thenReturn(Flux.just(lowStockProduct, product));

        // Act
        Flux<ProductResponseDTO> result = productInventoryService.getLowStockProducts(inventoryId, stockThreshold);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void searchProducts_withAllParams_shouldReturnResults() {
        String inventoryId = "1";
        String name = "Benzodiazepines";
        String description = "Sedative Medication";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(inventoryId, name, description, status))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, status);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withAllParams_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        String name = "InvalidName";
        String description = "InvalidDescription";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(inventoryId, name, description, status))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withAllParams_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String name = "InvalidName";
        String description = "InvalidDescription";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus(inventoryId, name, description, status))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withNameAndDescription_shouldReturnResults() {
        String inventoryId = "1";
        String name = "Benzodiazepines";
        String description = "Sedative Medication";

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescription(inventoryId, name, description))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withNameAndDescription_withInvalidInventoryId_shouldThrowNotFoundException() {
        String inventoryId = "InvalidInventoryId";
        String name = "InvalidName";
        String description = "InvalidDescription";

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescription(inventoryId, name, description))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withNameAndDescription_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String name = "InvalidName";
        String description = "InvalidDescription";

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndProductDescription(inventoryId, name, description))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, description, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withNameAndStatus_shouldReturnResults() {
        String inventoryId = "1";
        String name = "Benzodiazepines";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndStatus(inventoryId, name, status))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, status);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withNameAndStatus_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        String name = "InvalidName";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndStatus(inventoryId, name, status))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withNameAndStatus_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String name = "InvalidName";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductNameAndStatus(inventoryId, name, status))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withName_shouldReturnResults() {
        String inventoryId = "1";
        String name = "Benzodiazepines";

        when(productRepository.findAllProductsByInventoryIdAndProductName(inventoryId, name))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withName_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        String name = "InvalidName";

        when(productRepository.findAllProductsByInventoryIdAndProductName(inventoryId, name))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withName_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String name = "InvalidName";

        when(productRepository.findAllProductsByInventoryIdAndProductName(inventoryId, name))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, name, null, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withDescriptionAndStatus_shouldReturnResults() {
        String inventoryId = "1";
        String description = "Sedative Medication";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductDescriptionAndStatus(inventoryId, description, status))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, status);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withDescriptionAndStatus_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        String description = "InvalidDescription";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductDescriptionAndStatus(inventoryId, description, status))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withDescriptionAndStatus_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String description = "InvalidDescription";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndProductDescriptionAndStatus(inventoryId, description, status))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withDescription_shouldReturnResults() {
        String inventoryId = "1";
        String description = "Sedative Medication";

        when(productRepository.findAllProductsByInventoryIdAndProductDescription(inventoryId, description))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withDescription_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        String description = "InvalidDescription";

        when(productRepository.findAllProductsByInventoryIdAndProductDescription(inventoryId, description))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withDescription_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        String description = "InvalidDescription";

        when(productRepository.findAllProductsByInventoryIdAndProductDescription(inventoryId, description))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, description, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withStatus_shouldReturnResults() {
        String inventoryId = "1";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndStatus(inventoryId, status))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, null, status);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withStatus_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "InvalidInventoryId";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndStatus(inventoryId, status))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, null, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withStatus_noResults_shouldThrowNotFound() {
        String inventoryId = "1";
        Status status = Status.RE_ORDER;

        when(productRepository.findAllProductsByInventoryIdAndStatus(inventoryId, status))
                .thenReturn(Flux.empty());

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, null, status);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void searchProducts_withNoParams_shouldReturnResults() {
        String inventoryId = "1";

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, null, null);

        StepVerifier
                .create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProducts_withNoParams_withInvalidInventoryId_shouldThrowNotFound() {
        String inventoryId = "NonExistingInventoryId";

        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.empty());

        Flux<ProductResponseDTO> result = productInventoryService.searchProducts(inventoryId, null, null, null);

        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getQuantityOfProductsInInventory_InventoryFound_ShouldReturnQuantity() {
        // Arrange
        String inventoryId = "1";
        Inventory inventory = Inventory.builder()
                .inventoryId(inventoryId)
                .build();

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        when(productRepository.countByInventoryId(inventoryId))
                .thenReturn(Mono.just(5));

        // Act
        Mono<Integer> result = productInventoryService.getQuantityOfProductsInInventory(inventoryId);

        // Assert
        StepVerifier.create(result)
                .expectNext(5)
                .verifyComplete();

        verify(inventoryRepository, times(1)).findInventoryByInventoryId(inventoryId);
        verify(productRepository, times(1)).countByInventoryId(inventoryId);
    }

    @Test
    void getQuantityOfProductsInInventory_InventoryNotFound_ShouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "invalid";

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Integer> result = productInventoryService.getQuantityOfProductsInInventory(inventoryId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Inventory not found with id: " + inventoryId))
                .verify();

        verify(inventoryRepository, times(1)).findInventoryByInventoryId(inventoryId);
        verify(productRepository, never()).countByInventoryId(anyString());
    }


    @Test
    void consumeProduct_WithValidFields_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();

        Product product = Product.builder()
                .id("1")
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Product Name")
                .productPrice(50.0)
                .productQuantity(10)
                .productSalePrice(10.10)
                .build();

        Product updatedProduct = Product.builder()
                .id("1")
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Product Name")
                .productPrice(50.0)
                .productQuantity(9)
                .productSalePrice(10.10)
                .build();

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.just(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(updatedProduct));

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(productId, responseDTO.getProductId());
                    assertEquals("Product Name", responseDTO.getProductName());
                    assertEquals(50.0, responseDTO.getProductPrice());
                    assertEquals(9, responseDTO.getProductQuantity());
                    assertEquals(10.10, responseDTO.getProductSalePrice());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void consumeProduct_WithInvalidInventoryId_ShouldThrowNotFound() {
        // Arrange
        String inventoryId = "NonExistingInventoryId";
        String productId = UUID.randomUUID().toString();

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.empty());

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void consumeProduct_WithInvalidProductId_ShouldThrowNotFound() {
        // Arrange
        String inventoryId = "1";
        String productId = "NonExistingProductId";

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.empty());

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void createSupplyPdf_InvalidInventoryId_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidInventoryId = "";

        // Act
        Mono<byte[]> result = productInventoryService.createSupplyPdf(invalidInventoryId);

        // Assert
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void createSupplyPdf_NoProductsFound_ShouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "valid-id";
        Mockito.when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.empty());

        // Act
        Mono<byte[]> result = productInventoryService.createSupplyPdf(inventoryId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("No products found for inventory ID"))
                .verify();
    }

    @Test
    public void CreateSupplyPdfThrowsDocumentException() throws DocumentException {
        String inventoryId = "valid-id";

        // Create mock Product objects with correct parameters for Product constructor
        Product product1 = new Product("1", "P001", inventoryId, "Product1", "Description1", 10, 5.99, 7.99, product.getProductProfit(), Status.AVAILABLE, LocalDateTime.now());
        Product product2 = new Product("2", "P002", inventoryId, "Product2", "Description2", 20, 10.99, 12.99, product.getProductProfit(), Status.RE_ORDER, LocalDateTime.now());

        // Mock the repository to return a Flux of products
        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product1, product2));

        // Mock PdfWriter.getInstance to throw DocumentException
        mockStatic(PdfWriter.class);
        when(PdfWriter.getInstance(any(Document.class), any(ByteArrayOutputStream.class)))
                .thenThrow(new DocumentException("PDF generation error"));

        // Call the method and expect an error
        Mono<byte[]> result = productInventoryService.createSupplyPdf(inventoryId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error generating PDF"))
                .verify();

        // Verify the interaction with the repository
        verify(productRepository, times(1)).findAllProductsByInventoryId(inventoryId);
    }


    @Test
    void createSupplyPdf_SuccessfullyGeneratesPdf() {
        // Arrange
        String inventoryId = "valid-id";

        Product product1 = new Product();
        product1.setProductName("Product A");
        product1.setProductQuantity(10);
        product1.setProductDescription("Description A");
        product1.setProductPrice(100.0);
        product1.setProductSalePrice(120.00);

        Product product2 = new Product();
        product2.setProductName("Product B");
        product2.setProductQuantity(20);
        product2.setProductDescription("Description B");
        product2.setProductPrice(200.0);
        product2.setProductSalePrice(250.00);

        List<Product> productList = Arrays.asList(product1, product2);
        Mockito.when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.fromIterable(productList));

        // Act
        Mono<byte[]> result = productInventoryService.createSupplyPdf(inventoryId);

        // Assert
        StepVerifier.create(result)
                .assertNext(pdfBytes -> {
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                })
                .verifyComplete();
    }

    @Test
    void updateProductInventoryId_withValidProduct_shouldUpdateAndReturnProduct() {
        // Arrange
        String currentInventoryId = "1";
        String productId = "101";
        String newInventoryId = "2";

        Product product = Product.builder()
                .id("1")
                .inventoryId(currentInventoryId)
                .productId(productId)
                .productName("Product Name")
                .productPrice(50.0)
                .productQuantity(10)
                .productSalePrice(10.10)
                .build();

        // Mocking behavior
        when(productRepository.findProductByInventoryIdAndProductId(currentInventoryId, productId))
                .thenReturn(Mono.just(product));

        // Update the inventory ID on the product before saving
        product.setInventoryId(newInventoryId);

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(product));

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.updateProductInventoryId(currentInventoryId, productId, newInventoryId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(updatedProduct -> {
                    assertNotNull(updatedProduct);
                    assertEquals(newInventoryId, updatedProduct.getInventoryId());
                    return true; // This indicates that the assertion has passed
                })
                .verifyComplete();

        // Verify that the methods were called
        verify(productRepository, times(1)).findProductByInventoryIdAndProductId(currentInventoryId, productId);
        verify(productRepository, times(1)).save(any());
    }


    @Test
    void updateProductInventoryId_withInvalidProductId_shouldThrowNotFoundException() {
        // Arrange
        String currentInventoryId = "1";
        String invalidProductId = "999";
        String newInventoryId = "2";

        // Mocking behavior
        when(productRepository.findProductByInventoryIdAndProductId(currentInventoryId, invalidProductId))
                .thenReturn(Mono.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productInventoryService.updateProductInventoryId(currentInventoryId, invalidProductId, newInventoryId).block());

        assertEquals("Product not found with id: " + invalidProductId, exception.getMessage());
        verify(productRepository, times(1)).findProductByInventoryIdAndProductId(currentInventoryId, invalidProductId);
        verify(productRepository, times(0)).save(any());
    }

    @Test
    void getAllInventories_shouldReturnAllInventories() {
        // Arrange
        Inventory inventory = new Inventory();
        inventory.setInventoryId("1");
        inventory.setInventoryName("Sample Inventory");
        inventory.setInventoryType("Type A");
        inventory.setInventoryDescription("This is a sample inventory.");

        InventoryResponseDTO inventoryResponseDTO = EntityDTOUtil.toInventoryResponseDTO(inventory); // Convert to DTO

        // Mock repository behavior
        when(inventoryRepository.findAll()).thenReturn(Flux.just(inventory));

        // Act
        Flux<InventoryResponseDTO> result = productInventoryService.getAllInventories();

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseDTO -> responseDTO.getInventoryId().equals(inventoryResponseDTO.getInventoryId()))
                .verifyComplete();
    }

    @Test
    void restockLowStockProduct_WithValidInputs_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        String productId = "12345";
        int restockQuantity = 5;

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .productId(productId)
                .inventoryId(inventoryId)
                .productName("Test Product")
                .productDescription("Test Description")
                .productPrice(50.00)
                .productQuantity(10)
                .productSalePrice(20.00)
                .build();

        Product updatedProduct = Product.builder()
                .id(product.getId())
                .productId(productId)
                .inventoryId(inventoryId)
                .productName("Test Product")
                .productDescription("Test Description")
                .productPrice(50.00)
                .productQuantity(15) // Updated quantity after restock
                .productSalePrice(20.00)
                .build();

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(updatedProduct));

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(inventoryId, productId, restockQuantity);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(productId, responseDTO.getProductId());
                    assertEquals(15, responseDTO.getProductQuantity()); // Check the updated quantity
                    return true;
                })
                .verifyComplete();

        verify(productRepository).findProductByInventoryIdAndProductId(inventoryId, productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void restockLowStockProduct_WithNonExistentProduct_ShouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "1";
        String productId = "nonExistentProduct";
        int restockQuantity = 5;

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.empty());

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(inventoryId, productId, restockQuantity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Product with id: " + productId + "not found in inventory " + inventoryId))
                .verify();

        verify(productRepository).findProductByInventoryIdAndProductId(inventoryId, productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void restockLowStockProduct_WithInvalidQuantity_ShouldThrowInvalidInputException() {
        // Arrange
        String inventoryId = "1";
        String productId = "12345";
        int restockQuantity = -5; // Invalid quantity

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .productId(productId)
                .inventoryId(inventoryId)
                .productName("Test Product")
                .productDescription("Test Description")
                .productPrice(50.00)
                .productQuantity(10)
                .productSalePrice(20.00)
                .build();

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.just(product));

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(inventoryId, productId, restockQuantity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().equals("The restock quantity must be greater than 0."))
                .verify();

        verify(productRepository).findProductByInventoryIdAndProductId(inventoryId, productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateImportantStatus_shouldSucceed() {
        // Arrange
        String inventoryId = "inventoryId_1";
        Boolean important = true;

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(Mono.just(inventory));

        // Act
        Mono<Void> result = productInventoryService.updateImportantStatus(inventoryId, important);

        // Assert
        StepVerifier
                .create(result)
                .verifyComplete();

        verify(inventoryRepository).findInventoryByInventoryId(inventoryId);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void addInventory_shouldAutoGenerateInventoryCode() {
        // Arrange
        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .inventoryName("New Inventory")
                .inventoryType("Internal")
                .inventoryDescription("Test Description")
                .build();

        Inventory savedInventory = Inventory.builder()
                .inventoryId("generated_id")
                .inventoryCode("INV-0001")
                .inventoryName("New Inventory")
                .inventoryType("Internal")
                .inventoryDescription("Test Description")
                .build();

        when(inventoryRepository.count()).thenReturn(Mono.just(0L));
        when(inventoryRepository.insert(any(Inventory.class))).thenReturn(Mono.just(savedInventory));

        // Act
        Mono<InventoryResponseDTO> result = productInventoryService.addInventory(Mono.just(requestDTO));

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(response -> {
                    assertNotNull(response.getInventoryCode());
                    assertEquals("INV-0001", response.getInventoryCode());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getRecentUpdateMessage_WithRecentUpdates_ShouldReturnCount() {
        String inventoryId = "1";
        LocalDateTime now = LocalDateTime.now();

        when(productRepository.countByInventoryIdAndLastUpdatedAtAfter(eq(inventoryId), any(LocalDateTime.class)))
                .thenReturn(Mono.just(3L));

        Mono<String> result = productInventoryService.getRecentUpdateMessage(inventoryId);

        StepVerifier.create(result)
                .expectNext("3 supplies updated in the last 15 min.")
                .verifyComplete();
    }

    @Test
    void getRecentUpdateMessage_WithNoRecentUpdates_ShouldReturnNoUpdates() {
        String inventoryId = "1";

        when(productRepository.countByInventoryIdAndLastUpdatedAtAfter(eq(inventoryId), any(LocalDateTime.class)))
                .thenReturn(Mono.just(0L));

        Mono<String> result = productInventoryService.getRecentUpdateMessage(inventoryId);

        StepVerifier.create(result)
                .expectNext("No recent updates.")
                .verifyComplete();
    }


}







