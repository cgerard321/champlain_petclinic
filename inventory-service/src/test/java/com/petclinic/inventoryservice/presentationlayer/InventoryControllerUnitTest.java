package com.petclinic.inventoryservice.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.petclinic.inventoryservice.datalayer.Product.Status.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(controllers = InventoryController.class)
class InventoryControllerUnitTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    WebTestClient webTestClient;
    @MockBean
    ProductInventoryService productInventoryService;

    ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
            .inventoryId("1")
            .productId("123F567C9")
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .productSalePrice(15.99)
            .build();
    List<ProductResponseDTO> productResponseDTOS = Arrays.asList(
            ProductResponseDTO.builder()
                    .inventoryId("1")
                    .inventoryId("123F567C9")
                    .productName("Benzodiazepines")
                    .productDescription("Sedative Medication")
                    .productPrice(100.00)
                    .productQuantity(10)
                    .productSalePrice(15.99)
                    .build(),
            ProductResponseDTO.builder()
                    .inventoryId("1")
                    .inventoryId("123F567C9")
                    .productName("Benzodiazepines")
                    .productDescription("Sedative Medication")
                    .productPrice(100.00)
                    .productQuantity(10)
                    .productSalePrice(15.99)
                    .build()
    );
    List<InventoryTypeResponseDTO> typesDTOS = Arrays.asList(
            InventoryTypeResponseDTO.builder()
                    .type("Internal")
                    .typeId(UUID.randomUUID().toString())
                    .build(),
            InventoryTypeResponseDTO.builder()
                    .type("External")
                    .typeId(UUID.randomUUID().toString())
                    .build()
    );

    InventoryType inventoryType4 = InventoryType.builder()
            .typeId(UUID.randomUUID().toString())
            .type("Medications")
            .build();

//    Inventory inventory4 = Inventory.builder()
//            .inventoryId(UUID.randomUUID().toString())
//            .inventoryName("Medications")
//            .inventoryType(inventoryType4.getType())
//            .inventoryDescription("Antibiotics for pet infections")
//            .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
//            .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
//            .build();


      ProductResponseDTO lowStockProduct = ProductResponseDTO.builder()
            .inventoryId("inventoryId_1")
            .productId("productId_1")
            .productName("Low Stock Product")
            .productQuantity(5)
            .productPrice(100.00)
            .build();

    List<ProductResponseDTO> lowStockProducts = List.of(lowStockProduct);


//    @Test
//    public void testAddSupplyToInventoryByName() throws Exception {
//        String inventoryName = "Sedative Medications";
//
//        SupplyRequestDTO requestDTO = new SupplyRequestDTO(
//                "Sedative Medications",
//                "Medications for relaxation and sleep",
//                100.0,
//                10,
//                10.0
//        );
//
//        SupplyResponseDTO supplyResponseDTO = SupplyResponseDTO.builder()
//                .supplyId("c2e3ebba-d1a7-48d7-9b37-efbea4944874")
//                .inventoryId("5e2520b1-8c12-48fe-ae79-01552c8588e2")
//                .supplyName("Sedative Medications")
//                .supplyDescription("Medications for relaxation and sleep")
//                .supplyPrice(100.0)
//                .supplyQuantity(10)
//                .supplySalePrice(10.0)
//                .status(AVAILABLE)
//                .build();
//
//        InventoryResponseDTO responseDTO = InventoryResponseDTO.builder()
//                .inventoryId("5e2520b1-8c12-48fe-ae79-01552c8588e2")
//                .inventoryName(inventoryName)
//                .inventoryType("TestInventoryType")
//                .inventoryDescription("TestInventoryDescription")
//                .supplies(List.of(supplyResponseDTO))
//                .build();
//
//        when(supplyInventoryService.addSupplyToInventoryByInventoryName(anyString(), any())).thenReturn(Mono.just(responseDTO));
//
//        webTestClient.post()
//                .uri("/inventory/" + inventoryName + "/supplies")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(objectMapper.writeValueAsString(requestDTO))
//                .exchange()
//                .expectStatus().isCreated();
//    }


//    @Test
//    public void addSupplyToInventoryByName_Negative() {
//        String inventoryName = "NonExistingInventory";
//
//        SupplyRequestDTO requestDTONegative = new SupplyRequestDTO(
//                supply1.getSupplyName(),
//                supply1.getSupplyDescription(),
//                supply1.getSupplyPrice(),
//                supply1.getSupplyQuantity(),
//                supply1.getSupplySalePrice()
//        );
//
//        when(supplyInventoryService.addSupplyToInventoryByInventoryName(anyString(), any(Mono.class)))
//                .thenReturn(Mono.error(new RuntimeException("Inventory not found")));
//
//        webTestClient.post()
//                .uri("/inventoriesV2/" + inventoryName + "/supplies")
//                .body(BodyInserters.fromValue(requestDTONegative))
//                .exchange()
//                .expectStatus().isNotFound();
//    }

    @Test
    void updateInventory_ValidRequest_ShouldReturnOk() {
        // Arrange
        String validInventoryId = "123";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("Updated Internal")
                .inventoryType("Internal")
                .inventoryDescription("Updated inventory_3")
                .build();

        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("Updated Internal")
                .inventoryType("Internal")
                .inventoryDescription("Updated inventory_3")
                .build();

        when(productInventoryService.updateInventory(any(), eq(validInventoryId)))
                .thenReturn(Mono.just(inventoryResponseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}", validInventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryResponseDTO.getInventoryName(), dto.getInventoryName());
                    assertEquals(inventoryResponseDTO.getInventoryDescription(), dto.getInventoryDescription());
                });

        // Verify that the productInventoryService's updateInventory method was called with the correct arguments
        verify(productInventoryService, times(1))
                .updateInventory(any(), eq(validInventoryId));
    }

    @Test
    public void deleteProductInInventory_byProductId_shouldSucceed() {
        //arrange
        when(productInventoryService.deleteProductInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId()))
                .thenReturn(Mono.empty());
        //act and assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products/{productId}", productResponseDTO.getInventoryId(), productResponseDTO.getProductId())
                .exchange()
                .expectStatus().isNoContent();

    }


    @Test
    void addInventory_ValidRequest_ShouldReturnCreated() {
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("New Internal")
                .inventoryType("Internal")
                .inventoryDescription("New inventory_4")
                .build();

        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId("inventoryid1")
                .inventoryName("New Internal")
                .inventoryType("Internal")
                .inventoryDescription("New inventory_4")
                .build();

        when(productInventoryService.addInventory(any()))
                .thenReturn(Mono.just(inventoryResponseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InventoryResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryResponseDTO.getInventoryName(), dto.getInventoryName());
                    assertEquals(inventoryResponseDTO.getInventoryDescription(), dto.getInventoryDescription());
                });


        verify(productInventoryService, times(1))
                .addInventory(any());
    }


    @Test
    void getProductsInInventory_withValidId_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, null, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", productResponseDTOS.get(1).getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_andValidProductPrice_andValidProductQuantity_andValidProductSalePrice_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, 10, 200.00))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}&productQuantity={productQuantity}&productSalePrice={productSalePrice}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, 10, 200.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_andValidProductPrice_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", null, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductPrice_andValidProductQuantity_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, 100.00, 10, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}&productQuantity={productQuantity}",
                        productResponseDTOS.get(1).getInventoryId(), 100.00, 10)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductQuantity_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, null, 10, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productQuantity={productQuantity}",
                        productResponseDTOS.get(1).getInventoryId(), 10)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductPrice_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, 100.00, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}",
                        productResponseDTOS.get(1).getInventoryId(), 100.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductSalePrice_shouldSucceed() {
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, null, null, 200.00))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productSalePrice={productSalePrice}",
                        productResponseDTOS.get(1).getInventoryId(), 200.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }


    @Test
    void getInventoryByInventoryId_ValidIdShouldSucceed() {
        //arrange
        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId("inventoryId_2")
                .inventoryName("Pet food")
                .inventoryType("internal")
                .inventoryDescription("pet")
                .build();

        when(productInventoryService.getInventoryById(inventoryResponseDTO.getInventoryId()))
                .thenReturn(Mono.just(inventoryResponseDTO));

        webTestClient
                .get()
                .uri("/inventory/{inventoryId}", inventoryResponseDTO.getInventoryId())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryResponseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(inventoryResponseDTO.getInventoryName(), dto.getInventoryName());
                    assertEquals(inventoryResponseDTO.getInventoryType(), dto.getInventoryType());
                    assertEquals(inventoryResponseDTO.getInventoryDescription(), dto.getInventoryDescription());
                });

        verify(productInventoryService, times(1))
                .getInventoryById(inventoryResponseDTO.getInventoryId());

    }


    @Test
    void getProductsInInventoryByInventoryIdAndProductId_ValidRequest_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        String productId = "123F567C9";
        ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.getProductByProductIdInInventory(eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(productResponseDTO));

        // Act
        Mono<ProductResponseDTO> productResponseDTOMono = productInventoryService.getProductByProductIdInInventory(inventoryId, productId);

        // Assert
        StepVerifier
                .create(productResponseDTOMono)
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertEquals(productResponseDTO.getInventoryId(), response.getInventoryId());
                    assertEquals(productResponseDTO.getProductId(), response.getProductId());
                    assertEquals(productResponseDTO.getProductName(), response.getProductName());
                    assertEquals(productResponseDTO.getProductDescription(), response.getProductDescription());
                    assertEquals(productResponseDTO.getProductPrice(), response.getProductPrice());
                    assertEquals(productResponseDTO.getProductQuantity(), response.getProductQuantity());
                    assertEquals(productResponseDTO.getProductSalePrice(), response.getProductSalePrice());
                    return true;
                })
                .verifyComplete();

        verify(productInventoryService, times(1))
                .getProductByProductIdInInventory(eq(inventoryId), eq(productId));
    }


    @Test
    void getProductsInInventoryByInventoryIdAndProductId_InvalidRequest_ShouldFail() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String invalidProductId = "invalidProductId";

        when(productInventoryService.getProductByProductIdInInventory(eq(invalidInventoryId), eq(invalidProductId)))
                .thenReturn(Mono.empty());

        // Act
        Mono<ProductResponseDTO> productResponseDTOMono = productInventoryService.getProductByProductIdInInventory(invalidInventoryId, invalidProductId);

        // Assert
        StepVerifier
                .create(productResponseDTOMono)
                .expectComplete()
                .verify();

        verify(productInventoryService, times(1))
                .getProductByProductIdInInventory(eq(invalidInventoryId), eq(invalidProductId));
    }

    @Test
    void getInventoryByInvalidInventoryId_ReturnNotFound() {

        String invalidInventoryId = "invalid_id";
        when(productInventoryService.getInventoryById(invalidInventoryId))
                .thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/inventory/{inventoryId}", invalidInventoryId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(productInventoryService, times(1))
                .getInventoryById(invalidInventoryId);
    }

    @Test
    void updateInventory_InvalidId_ShouldReturnError() {
        // Arrange
        String invalidInventoryId = "invalid_id";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("Updated Internal")
                .inventoryType("Internal")
                .inventoryDescription("Updated inventory_3")
                .build();


        when(productInventoryService.updateInventory(any(), eq(invalidInventoryId)))
                .thenReturn(Mono.error(new NotFoundException("Inventory not found with id: " + invalidInventoryId)));


        webTestClient
                .put()
                .uri("/inventory/{inventoryId}", invalidInventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    void addInventory_InvalidType_ShouldReturnError() {
        // Arrange

        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("Updated Internal")
                .inventoryType(null)
                .inventoryDescription("Updated inventory_3")
                .build();

        when(productInventoryService.addInventory(any()))
                .thenReturn(Mono.error(new InvalidInputException("Invalid input data: inventory type cannot be blank.")));

        webTestClient
                .post()
                .uri("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    //delete all
    @Test
    void deleteProductInventory_ValidInventoryId_ShouldCallServiceDelete() {
        // Arrange
        String inventoryId = "1";
        when(productInventoryService.deleteAllProductsForAnInventory(inventoryId)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient
                .delete()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .exchange()
                .expectStatus().isNoContent();  // Expecting 204 NO CONTENT status.

        verify(productInventoryService, times(1)).deleteAllProductsForAnInventory(inventoryId);
    }


    @Test
    void updateProductInInventory_ValidRequest_ShouldSucceed() {
        // Arrange
        String inventoryId = "123";
        String productId = "456";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                });

        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));
    }

    @Test
    void updateProductInInventory_ProductNotFound_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "123";
        String productId = "456";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.error(new NotFoundException("Product not found")));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Product not found");

        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));
    }


    @Test
    void updateProductInInventory_InvalidInput_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "123";
        String productId = "456";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.error(new InvalidInputException("Invalid input")));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message", "Invalid input");

        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));
    }

    @Test
    void deleteAllInventories_ShouldCallServiceDeleteAll() {
        // Arrange
        when(productInventoryService.deleteAllInventory()).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient
                .delete()
                .uri("/inventory")
                .exchange()
                .expectStatus().isNoContent();  // Expecting 204 NO CONTENT status.

        verify(productInventoryService, times(1)).deleteAllInventory();
    }


    @Test
    public void deleteProductInInventory_byInvalidProductId_shouldNotFound() {
        //arrange
        String invalidProductId = "invalid";

        when(productInventoryService.deleteProductInInventory(productResponseDTO.getInventoryId(), invalidProductId))
                .thenReturn(Mono.error(new NotFoundException()));

        //act and assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products/{productId}", productResponseDTO.getInventoryId(), invalidProductId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Product not found, make sure it exists, productId: " + invalidProductId);
    }

    @Test
    public void deleteProductInInventory_byInvalidInventoryId_shouldNotFound() {
        //arrange
        String invalidInventoryId = "invalid";

        when(productInventoryService.deleteProductInInventory(invalidInventoryId, productResponseDTO.getProductId()))
                .thenReturn(Mono.error(new NotFoundException()));

        //act and assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products/{productId}", invalidInventoryId, productResponseDTO.getProductId())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Inventory not found, make sure it exists, inventoryId: " + invalidInventoryId);
    }

    @Test
    void addSupplyToInventory_ShouldCallServiceAddProduct() {
        // Arrange
        String inventoryId = "123";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.addSupplyToInventory(any(), eq(inventoryId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                });

        verify(productInventoryService, times(1))
                .addSupplyToInventory(any(), eq(inventoryId));
    }

    @Test
    void addSupplyToInventory_InvalidInput_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "123";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        when(productInventoryService.addSupplyToInventory(any(), eq(inventoryId)))
                .thenReturn(Mono.error(new InvalidInputException("Invalid input")));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message", "Invalid input");

        verify(productInventoryService, times(1))
                .addSupplyToInventory(any(), eq(inventoryId));
    }

    @Test
    public void deleteInventory_byInventoryId_shouldSucceed() {

        String validInventoryId = "123";

        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("Updated Internal")
                .inventoryType("internal")
                .inventoryDescription("Updated inventory_3")
                .build();
        //arrange
        when(productInventoryService.deleteInventoryByInventoryId(inventoryResponseDTO.getInventoryId()))
                .thenReturn(Mono.empty());

        //act and assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}", inventoryResponseDTO.getInventoryId())
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .deleteInventoryByInventoryId(eq(validInventoryId));
    }

    @Test
    public void deleteInventory_byInvalidInventoryId_shouldThrowNotFoundException() {

        String invalidInventoryId = "nonExistentId";

        //arrange
        when(productInventoryService.deleteInventoryByInventoryId(invalidInventoryId))
                .thenReturn(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + invalidInventoryId)));

        //act and assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}", invalidInventoryId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Inventory not found with InventoryId: " + invalidInventoryId);


        verify(productInventoryService, times(1))
                .deleteInventoryByInventoryId(eq(invalidInventoryId));
    }

    @Test
    public void addInventoryType_shouldSucceed() {
        InventoryTypeRequestDTO inventoryTypeRequestDTO = InventoryTypeRequestDTO.builder()
                .type("Internal")
                .build();

        InventoryTypeResponseDTO inventoryTypeResponseDTO = InventoryTypeResponseDTO.builder()
                .typeId("ee27f756-4790-447a-8ab9-37ce61ff3ffc")
                .type("Internal")
                .build();

        when(productInventoryService.addInventoryType(any()))
                .thenReturn(Mono.just(inventoryTypeResponseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/type")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryTypeRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InventoryTypeResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryTypeResponseDTO.getType(), dto.getType());
                });


        verify(productInventoryService, times(1))
                .addInventoryType(any());
    }

    @Test
    public void getAllInventoryTypes_shouldSucceed() {
        when(productInventoryService.getAllInventoryTypes())
                .thenReturn(Flux.fromIterable(typesDTOS));

        webTestClient.get()
                .uri("/inventory/type")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs -> {
                    assertNotNull(responseDTOs);
                    assertEquals(typesDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void searchInventories_WithNameOnly_ShouldReturnMatchingInventories() {
        // Arrange
        Pageable page = PageRequest.of(0, 10);
        String inventoryName = "SampleName";

        InventoryResponseDTO sampleResponse = new InventoryResponseDTO();

        when(productInventoryService.searchInventories(page, null,inventoryName, null, null, null))
                .thenReturn(Flux.just(sampleResponse));

        // Act and Assert
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryName", inventoryName)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1)
                .contains(sampleResponse);
    }

    @Test
    void searchInventories_WithOnlyType_ShouldReturnMatchingInventories() {
        // Arrange
        Pageable page = PageRequest.of(0, 10);
        String inventoryType = "SampleType";
        InventoryResponseDTO sampleResponse = new InventoryResponseDTO();

        when(productInventoryService.searchInventories(page, null,null, inventoryType, null, null))
                .thenReturn(Flux.just(sampleResponse));

        // Act and Assert
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryType", inventoryType)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1)
                .contains(sampleResponse);
    }

    @Test
    void searchInventories_WithOnlyDescription_ShouldReturnMatchingInventories() {
        // Arrange
        Pageable page = PageRequest.of(0, 10);
        String inventoryDescription = "SampleDescription";
        InventoryResponseDTO sampleResponse = new InventoryResponseDTO();

        when(productInventoryService.searchInventories(page, null,null, null, inventoryDescription, null))
                .thenReturn(Flux.just(sampleResponse));

        // Act and Assert
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryDescription", inventoryDescription)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1)
                .contains(sampleResponse);
    }

    @Test
    void searchInventories_WithNoParams_ShouldReturnAllInventories() {
        // Arrange
        Pageable page = PageRequest.of(0, 10);
        InventoryResponseDTO sampleResponse = new InventoryResponseDTO();

        when(productInventoryService.searchInventories(page, null,null, null, null, null))
                .thenReturn(Flux.just(sampleResponse));

        // Act and Assert
        webTestClient
                .get()
                .uri("/inventory")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .contains(sampleResponse);
    }

    @Test
    void searchInventories_WithInventoryCode_ShouldReturnInventory() {
        Pageable page = PageRequest.of(0, 10);
        String inventoryCode = "INV-0001";
        InventoryResponseDTO sampleResponse = new InventoryResponseDTO();
        sampleResponse.setInventoryCode(inventoryCode);

        when(productInventoryService.searchInventories(page, inventoryCode, null, null, null, null))
                .thenReturn(Flux.just(sampleResponse));

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryCode", inventoryCode)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1);
    }

    @Test
    void getProductsByInventoryIdAndProductName_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productName = "B";
        Mockito.when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                anyString(),
                anyString(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull()
        )).thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products")
                        .queryParam("productName", productName)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(2);
    }

    @Test
    void getAllProductsByInventoryId_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        Mockito.when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                anyString(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull()
        )).thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(2);
    }

    @Test
    public void addSupplyToInventory_StatusAssignedCorrectly() {
        // Arrange
        String inventoryId = "1";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .status(AVAILABLE)
                .build();

        when(productInventoryService.addSupplyToInventory(any(), eq(inventoryId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                    assertEquals(responseDTO.getStatus(), dto.getStatus());
                });

        verify(productInventoryService, times(1))
                .addSupplyToInventory(any(), eq(inventoryId));
    }

    @Test
    public void addSupplyToInventory_StatusAssignedShouldBeOut_Of_Stock() {
        // Arrange
        String inventoryId = "123";

        // Product with quantity 0 should result in OUT_OF_STOCK status
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(100.00)
                .productQuantity(0)  // Quantity is 0, so the status should be OUT_OF_STOCK
                .productSalePrice(10.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(100.00)
                .productQuantity(0)
                .productSalePrice(10.99)
                .status(Status.OUT_OF_STOCK)  // Expected status
                .build();

        // Mocking the service method to return the expected response
        when(productInventoryService.addSupplyToInventory(any(), eq(inventoryId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(Status.OUT_OF_STOCK, dto.getStatus());  // Assert the status is OUT_OF_STOCK
                });

        verify(productInventoryService, times(1)).addSupplyToInventory(any(), eq(inventoryId));
    }

    @Test
    public void addSupplyToInventory_StatusAssignedShouldBeRe_Order(){
        // Arrange
        String inventoryId = "123";

        // Product with quantity less than 20 should result in REORDER status
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(100.00)
                .productQuantity(10)  // Quantity < 20, so the status should be REORDER
                .productSalePrice(10.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("New Product")
                .productDescription("New Description")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(10.99)
                .status(RE_ORDER)  // Expected status
                .build();

        // Mocking the service method to return the expected response
        when(productInventoryService.addSupplyToInventory(any(), eq(inventoryId))
        ).thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(RE_ORDER, dto.getStatus());  // Assert the status is REORDER
                });

        verify(productInventoryService, times(1)).addSupplyToInventory(any(), eq(inventoryId));
    }

//    @Test
//    public void addProductToInventory_StatusAssignedShouldBeRe_Order() {
//        // Arrange
//        String inventoryId = "123";
//
//        // Product with quantity less than 20 should result in REORDER status
//        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
//                .productName("New Product")
//                .productDescription("New Description")
//                .productPrice(100.00)
//                .productQuantity(10)  // Quantity < 20, so the status should be REORDER
//                .productSalePrice(10.99)
//                .build();
//
//        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
//                .id("456")
//                .inventoryId(inventoryId)
//                .productName("New Product")
//                .productDescription("New Description")
//                .productPrice(100.00)
//                .productQuantity(10)
//                .productSalePrice(10.99)
//                .status(RE_ORDER)  // Expected status
//                .build();
//
//        // Mocking the service method to return the expected response
//        when(productInventoryService.addProductToInventory(any(), eq(inventoryId)))
//                .thenReturn(Mono.just(responseDTO));
//
//        // Act and Assert
//        webTestClient
//                .post()
//                .uri("/inventory/{inventoryId}/products", inventoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestDTO)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(ProductResponseDTO.class)
//                .value(dto -> {
//                    assertNotNull(dto);
//                    assertEquals(RE_ORDER, dto.getStatus());  // Assert the status is REORDER
//                });
//
//        verify(productInventoryService, times(1)).addProductToInventory(any(), eq(inventoryId));
//    }

    @Test
    public void updateProductInInventory_StatusAssignedCorrectly() {
        // Arrange
        String inventoryId = "123";
        String productId = "456";
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .productSalePrice(15.99)
                .status(AVAILABLE)
                .build();

        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                    assertEquals(responseDTO.getStatus(), dto.getStatus());
                });

        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));


    }

    @Test
    public void updateProductInInventory_StatusAssignedShouldBeOut_Of_Stock() {
        // Arrange
        String inventoryId = "123";
        String productId = "456";

        // Request with quantity set to 0, which should trigger the OUT_OF_STOCK (DISPOSABLE) status
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(0)  // Quantity is 0, which should mark it as OUT_OF_STOCK (DISPOSABLE)
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(0)
                .productSalePrice(15.99)
                .status(Status.OUT_OF_STOCK)
                .build();

        // Mocking the service to return the updated product with the OUT_OF_STOCK status
        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                    assertEquals(Status.OUT_OF_STOCK, dto.getStatus());
                });

        // Verify that the service method was called exactly once
        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));

    }

    @Test
    public void updateProductInInventory_StatusAssignedShouldBeRe_Order() {

// Arrange
        String inventoryId = "123";
        String productId = "456";

        // Request with quantity less than 20, which should trigger the REORDER status
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(15)  // Quantity is less than 20, which should mark it as REORDER
                .productSalePrice(15.99)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(15)
                .productSalePrice(15.99)
                .status(RE_ORDER)  // Expect the status to be REORDER
                .build();

        // Mocking the service to return the updated product with the REORDER status
        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(responseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(responseDTO.getProductSalePrice(), dto.getProductSalePrice());
                    assertEquals(RE_ORDER, dto.getStatus());  // Verify the status is REORDER
                });

        // Verify that the service method was called exactly once
        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));


    }

    @Test
    void getLowStockProducts_WithDefaultThreshold_ShouldReturnLowStockProducts() {
        // Arrange
        String inventoryId = "inventoryId_1";
        int defaultThreshold = 20;

        when(productInventoryService.getLowStockProducts(inventoryId, defaultThreshold))
                .thenReturn(Flux.fromIterable(lowStockProducts));

        // Act & Assert
        webTestClient.get()
                .uri("/inventory/{inventoryId}/products/lowstock", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(1, products.size());
                    assertEquals(lowStockProduct.getProductName(), products.get(0).getProductName());
                    assertEquals(lowStockProduct.getProductQuantity(), products.get(0).getProductQuantity());
                });

        verify(productInventoryService, times(1)).getLowStockProducts(inventoryId, defaultThreshold);
    }

    @Test
    void getLowStockProducts_WithCustomThreshold_ShouldReturnLowStockProducts() {
        // Arrange
        String inventoryId = "inventoryId_1";
        int customThreshold = 10;

        when(productInventoryService.getLowStockProducts(inventoryId, customThreshold))
                .thenReturn(Flux.fromIterable(lowStockProducts));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products/lowstock")
                        .queryParam("threshold", String.valueOf(customThreshold))
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(1, products.size());
                    assertEquals(lowStockProduct.getProductName(), products.get(0).getProductName());
                    assertEquals(lowStockProduct.getProductQuantity(), products.get(0).getProductQuantity());
                });

        verify(productInventoryService, times(1)).getLowStockProducts(inventoryId, customThreshold);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productName = "B";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(inventoryId, productName, productDescription, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, productDescription, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductName_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productName = "B";

        when(productInventoryService.searchProducts(inventoryId, productName, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, null, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescription_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(inventoryId, null, productDescription, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, productDescription, null);
    }

    @Test
    void searchProductsByInventoryId_withValidFields_shouldSucceed() {
        String inventoryId = "1";

        when(productInventoryService.searchProducts(inventoryId, null, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, null, null);
    }

//    @Test
//    void getLowStockProducts_WithInvalidInventoryId_ShouldReturnNotFound() {
//        // Arrange
//        String invalidInventoryId = "nonExistentInventory";
//        int threshold = 16;
//
//        when(productInventoryService.getLowStockProducts(invalidInventoryId, threshold))
//                .thenReturn(Flux.empty());
//
//        // Act & Assert
//        webTestClient.get()
//                .uri("/inventory/{inventoryId}/products/lowstock", invalidInventoryId)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNotFound();
//
//        verify(productInventoryService, times(1)).getLowStockProducts(invalidInventoryId, threshold);
//    }

//    @Test
//    void getLowStockProducts_WithEmptyResult_ShouldReturnNoContent() {
//        // Arrange
//        String inventoryId = "inventoryId_2";
//        int threshold = 16;
//
//        when(productInventoryService.getLowStockProducts(inventoryId, threshold))
//                .thenReturn(Flux.empty());
//
//        // Act & Assert
//        webTestClient.get()
//                .uri("/inventory/{inventoryId}/products/lowstock", inventoryId)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNoContent();
//
//        verify(productInventoryService, times(1)).getLowStockProducts(inventoryId, threshold);
//    }

    @Test
    void getQuantityOfProductsInInventory_withValidInventoryId_shouldReturnProductQuantity() {
        // Arrange
        String inventoryId = "inventoryId_1";
        Integer expectedQuantity = 10;

        // Mock the service to return the expected quantity
        when(productInventoryService.getQuantityOfProductsInInventory(inventoryId))
                .thenReturn(Mono.just(expectedQuantity));

        // Act and Assert
        webTestClient.get()
                .uri("/inventory/{inventoryId}/productquantity", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Integer.class)
                .value(quantity -> {
                    assertNotNull(quantity);
                    assertEquals(expectedQuantity, quantity); // Check if the returned value matches the mocked quantity
                });

        // Verify that the service was called with the correct inventoryId
        verify(productInventoryService, times(1)).getQuantityOfProductsInInventory(inventoryId);
    }

    @Test
    void deleteAllProductsInInventory_withValidInventoryId_shouldSucceed() {
        // Arrange
        String inventoryId = "inventoryId_1";

        // Mock the service to return an empty Mono
        when(productInventoryService.deleteAllProductsForAnInventory(inventoryId))
                .thenReturn(Mono.empty());

        // Act and Assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .exchange()
                .expectStatus().isNoContent();

        // Verify that the service was called with the correct inventoryId
        verify(productInventoryService, times(1)).deleteAllProductsForAnInventory(inventoryId);
    }

    @Test
    void createPdfReportForInventory_withValidInventoryId_shouldReturnPdfFile() {
        // Arrange
        String inventoryId = "inventoryId_1";
        byte[] pdfContent = "PDF Content".getBytes();

        // Mock the service to return the PDF content
        when(productInventoryService.createSupplyPdf(inventoryId))
                .thenReturn(Mono.just(pdfContent));

        // Act and Assert
        webTestClient.get()
                .uri("/inventory/{inventoryId}/products/download", inventoryId)
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .value(content -> {
                    assertNotNull(content);
                    assertArrayEquals(pdfContent, content); // Check if the returned content matches the mocked content
                });

    }

    @Test
    public void testUpdateProductInventoryId_Success() {
        // Mock response DTO
        ProductResponseDTO mockResponse = new ProductResponseDTO();
        mockResponse.setProductId("prod123");
        mockResponse.setInventoryId("newInventory123");

        // Mock the service method
        when(productInventoryService.updateProductInventoryId(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockResponse));

        // Perform the PUT request
        webTestClient.put()
                .uri("/inventory/currentInventory123/products/prod123/updateInventoryId/newInventory123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(response -> {
                    // Assert that the returned DTO has the expected values
                    assert response.getProductId().equals("prod123");
                    assert response.getInventoryId().equals("newInventory123");
                });
    }

    @Test
    void testUpdateProductInventoryId_NotFound() {
        // Given
        String currentInventoryId = "invalidInventoryId";
        String productId = "invalidProductId";
        String newInventoryId = "invalidNewInventoryId";

        // Mocking the service to return empty Mono
        when(productInventoryService.updateProductInventoryId(currentInventoryId, productId, newInventoryId))
                .thenReturn(Mono.empty());

        // When and Then
        webTestClient.put()
                .uri("/inventory/{currentInventoryId}/products/{productId}/updateInventoryId/{newInventoryId}",
                        currentInventoryId, productId, newInventoryId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void restockLowStockProduct_ValidRequest_ShouldSucceed() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "productId_1";
        Integer productQuantity = 10;

        ProductResponseDTO updatedProductResponseDTO = ProductResponseDTO.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName("Restocked Product")
                .productDescription("Restocked product description")
                .productPrice(100.00)
                .productQuantity(productQuantity)
                .build();

        when(productInventoryService.restockLowStockProduct(inventoryId, productId, productQuantity))
                .thenReturn(Mono.just(updatedProductResponseDTO));

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}/restockProduct?productQuantity={productQuantity}", inventoryId, productId, productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(updatedProductResponseDTO.getInventoryId(), responseDTO.getInventoryId());
                    assertEquals(updatedProductResponseDTO.getProductId(), responseDTO.getProductId());
                    assertEquals(updatedProductResponseDTO.getProductQuantity(), responseDTO.getProductQuantity());
                });

        // Verify that the service method was called with the correct arguments
        verify(productInventoryService, times(1)).restockLowStockProduct(inventoryId, productId, productQuantity);
    }

    @Test
    void restockLowStockProduct_InvalidQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "productId_1";
        Integer invalidQuantity = 0;

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}/restockProduct?productQuantity={productQuantity}", inventoryId, productId, invalidQuantity)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify that the service method was not called
        verify(productInventoryService, times(0)).restockLowStockProduct(any(), any(), any());
    }

    @Test
    void restockLowStockProduct_ProductNotFound_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "nonExistentProductId";
        Integer productQuantity = 10;

        when(productInventoryService.restockLowStockProduct(inventoryId, productId, productQuantity))
                .thenReturn(Mono.empty());

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}/products/{productId}/restockProduct?productQuantity={productQuantity}", inventoryId, productId, productQuantity)
                .exchange()
                .expectStatus().isNotFound();

        // Verify that the service method was called with the correct arguments
        verify(productInventoryService, times(1)).restockLowStockProduct(inventoryId, productId, productQuantity);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productName = "B";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, productName, productDescription, status))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndStatus_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productName = "B";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, productName, null, status))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, null, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescriptionAndStatus_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, null, productDescription, status))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndStatus_withValidFields_shouldSucceed() {
        String inventoryId = "1";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, null, null, status))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(products -> {
                    assertNotNull(products);
                    assertEquals(2, products.size());
                });
        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, null, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productName = "C";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(inventoryId, productName, productDescription, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, productDescription, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productName = "B";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(invalidInventoryId, productName, productDescription, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, productName, productDescription, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productName = "C";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, productName, productDescription, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productName = "B";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(invalidInventoryId, productName, productDescription, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, productName, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndStatus_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productName = "C";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, productName, null, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, null, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndStatus_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productName = "B";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(invalidInventoryId, productName, null, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("status", status)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, productName, null, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescriptionAndStatus_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, null, productDescription, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescriptionAndStatus_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productDescription = "Sedative";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(invalidInventoryId, null, productDescription, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .queryParam("status", status)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, null, productDescription, status);
    }

    @Test
    void searchProductsByInventoryIdAndStatus_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(inventoryId, null, null, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("status", status)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, null, status);
    }

    @Test
    void searchProductsByInventoryIdAndStatus_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        Status status = Status.AVAILABLE;

        when(productInventoryService.searchProducts(invalidInventoryId, null, null, status))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("status", status)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, null, null, status);
    }

    @Test
    void searchProductsByInventoryId_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";

        when(productInventoryService.searchProducts(invalidInventoryId, null, null, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, null, null, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductName_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productName = "B";

        when(productInventoryService.searchProducts(invalidInventoryId, productName, null, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, productName, null, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescription_withInvalidInventoryId_shouldReturnNoContent() {
        String invalidInventoryId = "invalidInventoryId";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(invalidInventoryId, null, productDescription, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .build(invalidInventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        verify(productInventoryService, times(1))
                .searchProducts(invalidInventoryId, null, productDescription, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductName_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productName = "C";

        when(productInventoryService.searchProducts(inventoryId, productName, null, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, productName, null, null);
    }

    @Test
    void searchProductsByInventoryIdAndProductDescription_withNoResult_shouldReturnEmpty() {
        String inventoryId = "1";
        String productDescription = "Sedative";

        when(productInventoryService.searchProducts(inventoryId, null, productDescription, null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/inventory/{inventoryId}/products/search")
                        .queryParam("productDescription", productDescription)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productInventoryService, times(1))
                .searchProducts(inventoryId, null, productDescription, null);
    }

    @Test
    void consumeProduct_withValidFields_shouldSucceed() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "productId_1";

        // Mock the service to return the updated product
        when(productInventoryService.consumeProduct(inventoryId, productId))
                .thenReturn(Mono.just(productResponseDTO));

        // Act and Assert
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(product -> {
                    assertNotNull(product);
                    assertEquals(productResponseDTO, product); // Check if the returned product matches the mocked product
                });

        // Verify that the service was called with the correct inventoryId and productId
        verify(productInventoryService, times(1)).consumeProduct(inventoryId, productId);
    }

    @Test
    void consumeProduct_withInvalidInventoryIdAndProductId_shouldThrowNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String invalidProductId = "invalidProductId";

        // Mock the service to return an error
        when(productInventoryService.consumeProduct(invalidInventoryId, invalidProductId))
                .thenReturn(Mono.error(new NotFoundException()));

        // Act and Assert
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", invalidInventoryId, invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductId: " + invalidProductId);
    }

    @Test
    void consumeProduct_withInvalidProductId_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String invalidProductId = "invalidProductId";

        // Mock the service to return an error
        when(productInventoryService.consumeProduct(inventoryId, invalidProductId))
                .thenReturn(Mono.error(new NotFoundException()));

        // Act and Assert
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", inventoryId, invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Inventory not found with InventoryId: " + inventoryId +
                        "\nOr ProductId: " + invalidProductId);
    }

    @Test
    void consumeProduct_withInvalidInventoryId_shouldThrowNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String productId = "productId_1";

        // Mock the service to return an error
        when(productInventoryService.consumeProduct(invalidInventoryId, productId))
                .thenReturn(Mono.error(new NotFoundException()));

        // Act and Assert
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", invalidInventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductId: " + productId);
    }

    @Test
    void consumeProduct_withProductQuantityZero_shouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "productId_1";

        // Mock the service to return an error
        when(productInventoryService.consumeProduct(inventoryId, productId))
                .thenReturn(Mono.error(new InvalidInputException("Product quantity is 0")));

        // Act and Assert
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message", "Product quantity is 0");
    }

}
