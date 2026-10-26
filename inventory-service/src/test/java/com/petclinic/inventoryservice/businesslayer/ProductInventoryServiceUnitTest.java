package com.petclinic.inventoryservice.businesslayer;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import org.mockito.MockedStatic;
import java.io.OutputStream;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditAction;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditEntry;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.ImageUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import com.petclinic.inventoryservice.utils.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatcher;
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
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


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
    @MockBean
    SupplyAuditRepository supplyAuditRepository;

    @BeforeEach
    void configureAuditRepository() {
        when(supplyAuditRepository.insert(any(SupplyAuditEntry.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

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
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(product.getInventoryId(), null, null, null, null, null, null, pageable);
        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectNextMatches(prod -> prod.getProductId().equals(product2.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithPriceAndQuantity_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductPriceBetween(
                product.getInventoryId(), 10.00, 200.00))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), null, 10.00, 200.00, 10, null, null, pageable);

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(prod -> prod.getProductId().equals(product.getProductId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldsPagination_WithPriceOnly_ShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 2);
        when(productRepository.findAllProductsByInventoryIdAndProductPriceBetween(
                product.getInventoryId(), 10.00, 200.00))
                .thenReturn(Flux.just(product, product2));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                        product.getInventoryId(), null, 10.00, 200.00, null, null, null, pageable);

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
                        product.getInventoryId(), null, null, null, 5, null, null, pageable);

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
                        product.getInventoryId(), "name", null, null, null, null, null, pageable);

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
        Double minPrice = 100.00;
        Double maxPrice = 100.00;
        Integer productQuantity = 10;
        Double minSalePrice = 15.99;
        Double maxSalePrice = 15.99;

        when(productRepository
                .findAllProductsByInventoryIdAndProductPriceBetween(
                        inventoryId,
                        minPrice,
                        maxPrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        productName,
                        minPrice,
                        maxPrice,
                        productQuantity,
                        minSalePrice,
                        maxSalePrice);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductPrice_andProductQuantity_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Double minPrice = 100.00;
        Double maxPrice = 100.00;
        Integer productQuantity = 10;

        when(productRepository
                .findAllProductsByInventoryIdAndProductPriceBetween(
                        inventoryId,
                        minPrice,
                        maxPrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        minPrice,
                        maxPrice,
                        productQuantity,
                        null,
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
        Double minPrice = 100.00;
        Double maxPrice = 100.00;

        when(productRepository
                .findAllProductsByInventoryIdAndProductPriceBetween(
                        inventoryId,
                        minPrice,
                        maxPrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        minPrice,
                        maxPrice,
                        null,
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
                        null,
                        productQuantity,
                        null,
                        null);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andProductSalePrice_withValidFields_shouldSucceed(){
        String inventoryId = "1";
        Double minSalePrice = 15.99;
        Double maxSalePrice = 15.99;

        when(productRepository
                .findAllProductsByInventoryIdAndProductSalePriceBetween(
                        inventoryId,
                        minSalePrice,
                        maxSalePrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> productResponseDTOMono = productInventoryService
                .getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        null,
                        null,
                        null,
                        null,
                        minSalePrice,
                        maxSalePrice);

        StepVerifier
                .create(productResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andOnlyMinPrice_shouldSucceed() {
        String inventoryId = "1";
        Double minPrice = 50.00;

        when(productRepository.findAllProductsByInventoryIdAndProductPriceBetween(inventoryId, minPrice, Double.MAX_VALUE))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId, null, minPrice, null, null, null, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andOnlyMaxPrice_shouldSucceed() {
        String inventoryId = "1";
        Double maxPrice = 200.00;

        when(productRepository.findAllProductsByInventoryIdAndProductPriceBetween(inventoryId, 0.0, maxPrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId, null, null, maxPrice, null, null, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andOnlyMinSalePrice_shouldSucceed() {
        String inventoryId = "1";
        Double minSalePrice = 10.00;

        when(productRepository.findAllProductsByInventoryIdAndProductSalePriceBetween(inventoryId, minSalePrice, Double.MAX_VALUE))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId, null, null, null, null, minSalePrice, null);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllProductsByInventoryId_andOnlyMaxSalePrice_shouldSucceed() {
        String inventoryId = "1";
        Double maxSalePrice = 50.00;

        when(productRepository.findAllProductsByInventoryIdAndProductSalePriceBetween(inventoryId, 0.0, maxSalePrice))
                .thenReturn(Flux.just(product));

        Flux<ProductResponseDTO> result = productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId, null, null, null, null, null, maxSalePrice);

        StepVerifier.create(result).expectNextCount(1).verifyComplete();
    }

    @Test
    public void deleteProduct_validProductAndInventory_ShouldSucceed() {
        //arrange
        String productId = "123F567C9";
        when(inventoryRepository.findInventoryByInventoryId(product.getInventoryId()))
                .thenReturn(Mono.just(inventory));
        when(productRepository.findProductByInventoryIdAndProductId(product.getInventoryId(), productId))
                .thenReturn(Mono.just(product));
        when(productRepository.delete(product)).thenReturn(Mono.empty());
        Mono<Void> deletedProduct = productInventoryService.deleteProductInInventory(
                product.getInventoryId(), productId, "test-user");
        //act and assert
        StepVerifier
                .create(deletedProduct)
                .expectNextCount(0)
                .verifyComplete();

        verify(productRepository).delete(product);
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
                .supplierName("Original Supplier")
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

        when(productRepository.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
                eq(inventoryId),
                eq(productRequestDTO.getProductName()),
                eq(productId)
        )).thenReturn(Mono.just(false));

        // Act and Assert
        StepVerifier
                .create(productInventoryService.updateProductInInventory(
                        Mono.just(productRequestDTO), inventoryId, productId, "test-user"))
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
        verify(productRepository).save(argThat(savedProduct ->
                "Original Supplier".equals(savedProduct.getSupplierName())));
    }

    @Test
    void updateProductInInventory_withoutQuantityChange_shouldNotWriteAuditEntry() {
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();
        ProductRequestDTO request = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productDescription("Description")
                .productPrice(99.99)
                .productQuantity(10)
                .productSalePrice(15.99)
                .build();
        Inventory inventory = Inventory.builder()
                .inventoryId(inventoryId)
                .inventoryType(inventoryType.getType())
                .build();
        Product existingProduct = Product.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Original Product Name")
                .productQuantity(10)
                .supplierName("Original Supplier")
                .build();
        Product savedProduct = Product.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Updated Product Name")
                .productQuantity(10)
                .productDescription("Description")
                .productPrice(99.99)
                .productSalePrice(15.99)
                .supplierName("Original Supplier")
                .build();

        when(inventoryRepository.findInventoryByInventoryId(inventoryId)).thenReturn(Mono.just(inventory));
        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(existingProduct));
        when(productRepository.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
                inventoryId, request.getProductName(), productId)).thenReturn(Mono.just(false));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

        StepVerifier.create(productInventoryService.updateProductInInventory(
                        Mono.just(request), inventoryId, productId, "test-user"))
                .expectNextMatches(response -> response.getProductQuantity() == 10)
                .verifyComplete();

        verify(supplyAuditRepository, never()).insert(any(SupplyAuditEntry.class));
    }

    @Test
    void updateProductInInventory_WithSupplierName_ShouldSaveAndReturnTrimmedSupplierName() {
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();
        ProductRequestDTO request = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productDescription("Description")
                .productPrice(99.99)
                .productQuantity(20)
                .productSalePrice(15.99)
                .supplierName("  New Supplier  ")
                .build();
        Product existingProduct = Product.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Original Product Name")
                .productQuantity(10)
                .supplierName("Original Supplier")
                .build();

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(Inventory.builder().inventoryId(inventoryId).build()));
        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(existingProduct));
        when(productRepository.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
                inventoryId, request.getProductName(), productId)).thenReturn(Mono.just(false));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productInventoryService.updateProductInInventory(
                        Mono.just(request), inventoryId, productId, "test-user"))
                .assertNext(response -> assertEquals("New Supplier", response.getSupplierName()))
                .verifyComplete();
        verify(productRepository).save(argThat(savedProduct ->
                "New Supplier".equals(savedProduct.getSupplierName())));
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
                .create(productInventoryService.updateProductInInventory(
                        Mono.just(productRequestDTO), inventoryId, productId, "test-user"))
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

        when(productRepository.findProductByProductId(eq(productId)))
                .thenReturn(Mono.just(Product.builder().productId(productId).inventoryId(inventoryId)
                        .productQuantity(10).build()));

        // Assert
        assertThrows(InvalidInputException.class, () ->
                productInventoryService.updateProductInInventory(
                        Mono.just(productRequestDTO), inventoryId, productId, "test-user").block()
        );
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

        when(productRepository.findProductByProductId(eq(productId)))
                .thenReturn(Mono.just(
                        Product.builder()
                                .productId(productId)
                                .inventoryId(inventoryId)
                                .productName("existing")
                                .productQuantity(10)
                                .build()
                ));


        // Act and Assert
        StepVerifier.create(productInventoryService.updateProductInInventory(
                        Mono.just(productRequestDTO), inventoryId, productId, "test-user"))
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

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));

        when(productRepository.existsByInventoryIdAndProductNameIgnoreCase(eq(inventoryId), eq("Benzodiazepines")))
                .thenReturn(Mono.just(false));

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(product));
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(
                Mono.just(productRequestDTO), inventoryId, "test-user");
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
    void addSupplyToInventory_WithSupplierName_ShouldSaveAndReturnTrimmedSupplierName() {
        String inventoryId = "1";
        ProductRequestDTO request = ProductRequestDTO.builder()
                .productName("Bandages")
                .productDescription("Sterile bandages")
                .productPrice(10.00)
                .productQuantity(20)
                .productSalePrice(12.00)
                .supplierName("  Clinic Supplier  ")
                .build();

        when(inventoryRepository.findInventoryByInventoryId(inventoryId)).thenReturn(Mono.just(inventory));
        when(productRepository.existsByInventoryIdAndProductNameIgnoreCase(inventoryId, request.getProductName()))
                .thenReturn(Mono.just(false));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(productInventoryService.addSupplyToInventory(
                        Mono.just(request), inventoryId, "test-user"))
                .assertNext(response -> assertEquals("Clinic Supplier", response.getSupplierName()))
                .verifyComplete();
        verify(productRepository).save(argThat(savedProduct ->
                "Clinic Supplier".equals(savedProduct.getSupplierName())));
    }

    @Test
    void addSupplyToInventory_WithBlankSupplierName_ShouldRejectWithoutSaving() {
        String inventoryId = "1";
        ProductRequestDTO request = ProductRequestDTO.builder()
                .productName("Bandages")
                .productDescription("Sterile bandages")
                .productPrice(10.00)
                .productQuantity(20)
                .productSalePrice(12.00)
                .supplierName("   ")
                .build();

        when(inventoryRepository.findInventoryByInventoryId(inventoryId)).thenReturn(Mono.just(inventory));
        when(productRepository.existsByInventoryIdAndProductNameIgnoreCase(inventoryId, request.getProductName()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(productInventoryService.addSupplyToInventory(
                        Mono.just(request), inventoryId, "test-user"))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof InvalidInputException);
                    assertEquals("Supplier name cannot be empty.", error.getMessage());
                })
                .verify();
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addSupplyToInventory_ShouldFail_WhenProductNameExists() {
        String inventoryId = "1";

        when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.just(inventory));
        when(productRepository.existsByInventoryIdAndProductNameIgnoreCase(eq(inventoryId), eq("Benzodiazepines")))
                .thenReturn(Mono.just(true));

        Mono<ProductResponseDTO> result =
                productInventoryService.addSupplyToInventory(Mono.just(productRequestDTO), inventoryId, "test-user");

        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof UnprocessableEntityException)
                .verify();
    }
    @Test
    void addSupplyToInventory_WithInvalidInventoryId_ShouldThrowNotFoundException(){
        // Arrange
        String inventoryId = "1";
        Mockito.when(inventoryRepository.findInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());
        //Act
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(
                Mono.just(productRequestDTO), inventoryId, "test-user");
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
        Mono<ProductResponseDTO> result = productInventoryService.addSupplyToInventory(
                Mono.just(productRequestDTO), inventoryId, "test-user");
        //Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof InvalidInputException);
                    assertTrue(ex.getMessage().contains("greater than 0"));
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
        InventoryTypeRequestDTO dto = InventoryTypeRequestDTO.builder()
                .type("Internal")
                .build();

        assertNotNull(inventoryType); // make sure this is initialized in @BeforeEach

        when(inventoryTypeRepository.existsByTypeIgnoreCase(eq("Internal")))
                .thenReturn(Mono.just(false));

        when(inventoryTypeRepository.insert(any(InventoryType.class)))
                .thenReturn(Mono.just(inventoryType));

        // Act
        Mono<InventoryTypeResponseDTO> result = productInventoryService.addInventoryType(Mono.just(dto));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(found -> {
                    assertNotNull(found);
                    assertEquals(inventoryType.getType(), found.getType());
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

        when(inventoryRepository.findAll()).thenReturn(Flux.empty());

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
                .expectNextCount(0)   // no items
                .verifyComplete();
    }

    @Test
    void getLowStockProducts_WithNoMatchingInventory_ShouldNotThrowNotFound() {
        // Arrange
        String invalidInventoryId = "999";
        int stockThreshold = 5;

        when(productRepository.findAllByInventoryIdAndProductQuantityLessThan(invalidInventoryId, stockThreshold))
                .thenReturn(Flux.empty());

        // Act
        Flux<ProductResponseDTO> result = productInventoryService.getLowStockProducts(invalidInventoryId, stockThreshold);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
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
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId, "test-user");

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

        verify(supplyAuditRepository).insert(argThat(new ArgumentMatcher<SupplyAuditEntry>() {
            @Override
            public boolean matches(SupplyAuditEntry entry) {
                return inventoryId.equals(entry.inventoryId())
                        && productId.equals(entry.supplyId())
                        && "Product Name".equals(entry.supplyName())
                        && entry.previousQuantity() == 10
                        && entry.newQuantity() == 9
                        && entry.quantityChange() == -1
                        && entry.action() == SupplyAuditAction.CONSUMED
                        && entry.changedAt() != null
                        && "test-user".equals(entry.changedBy());
            }
        }));
    }

    @Test
    void consumeProduct_WithInvalidInventoryId_ShouldThrowNotFound() {
        // Arrange
        String inventoryId = "NonExistingInventoryId";
        String productId = UUID.randomUUID().toString();

        when(productRepository.findProductByInventoryIdAndProductId(inventoryId, productId))
                .thenReturn(Mono.empty());

        // Act
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId, "test-user");

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
        Mono<ProductResponseDTO> result = productInventoryService.consumeProduct(inventoryId, productId, "test-user");

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
                .expectError(InvalidInputException.class)
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
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    public void CreateSupplyPdfThrowsDocumentException() throws DocumentException {
        String inventoryId = "valid-id";

        // Build test products without depending on the generated constructor's field order.
        Product product1 = Product.builder()
                .id("1")
                .productId("P001")
                .inventoryId(inventoryId)
                .productName("Product1")
                .productDescription("Description1")
                .productQuantity(10)
                .productPrice(5.99)
                .productSalePrice(7.99)
                .productProfit(product.getProductProfit())
                .status(Status.AVAILABLE)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .id("2")
                .productId("P002")
                .inventoryId(inventoryId)
                .productName("Product2")
                .productDescription("Description2")
                .productQuantity(20)
                .productPrice(10.99)
                .productSalePrice(12.99)
                .productProfit(product.getProductProfit())
                .status(Status.RE_ORDER)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        // Mock the repository to return a Flux of products
        when(productRepository.findAllProductsByInventoryId(inventoryId))
                .thenReturn(Flux.just(product1, product2));


        try (MockedStatic<PdfWriter> pdfWriter = mockStatic(PdfWriter.class)) {
            pdfWriter.when(() -> PdfWriter.getInstance(any(Document.class), any(OutputStream.class)))
                    .thenThrow(new DocumentException("PDF generation error"));

            Mono<byte[]> result = productInventoryService.createSupplyPdf(inventoryId);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals("Error generating PDF"))
                    .verify();
        }

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
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(
                inventoryId, productId, restockQuantity, "test-user");

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
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(
                inventoryId, productId, restockQuantity, "test-user");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Product with id: " + productId + " not found in inventory " + inventoryId))
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
        Mono<ProductResponseDTO> result = productInventoryService.restockLowStockProduct(
                inventoryId, productId, restockQuantity, "test-user");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().equals("The restock quantity must be greater than 0."))
                .verify();

        verify(productRepository, never()).findProductByInventoryIdAndProductId(inventoryId, productId);
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
        when(inventoryRepository.existsByInventoryName(anyString())).thenReturn(Mono.just(false));


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

    @Test
    void getSupplyHistory_returnsMostRecentAuditEntriesForSupply() {
        String inventoryId = "inventory-1";
        String productId = "product-1";
        SupplyAuditEntry entry = new SupplyAuditEntry(
                "audit-1",
                inventoryId,
                productId,
                "Benzodiazepines",
                8,
                10,
                2,
                SupplyAuditAction.QUANTITY_UPDATED,
                Instant.now(),
                "test-user");
        when(supplyAuditRepository.findTop20ByInventoryIdAndSupplyIdOrderByChangedAtDesc(
                inventoryId, productId))
                .thenReturn(Flux.just(entry));

        StepVerifier.create(productInventoryService.getSupplyHistory(inventoryId, productId))
                .expectNextMatches(actual -> actual.equals(entry)
                        && actual.quantityChange() == 2
                        && "test-user".equals(actual.changedBy()))
                .verifyComplete();

        verify(supplyAuditRepository)
                .findTop20ByInventoryIdAndSupplyIdOrderByChangedAtDesc(inventoryId, productId);
    }


}
