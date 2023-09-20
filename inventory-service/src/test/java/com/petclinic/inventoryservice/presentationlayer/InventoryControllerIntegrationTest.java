package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class InventoryControllerIntegrationTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;
    @BeforeEach
    public void dbSetup(){
        Publisher<Inventory> inventoryPublisher = inventoryRepository.save(Inventory.builder()
                .inventoryId("1")
                .inventoryType("Medication")
                .inventoryDescription("Medication for procedures")
                .build());
        StepVerifier
                .create(inventoryPublisher)
                .expectNextCount(1)
                .verifyComplete();
        Publisher<Product> productPublisher = productRepository.save(Product.builder()
                .inventoryId("1")
                .inventoryId("123F567C9")
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .build());
        StepVerifier
                .create(productPublisher)
                .expectNextCount(1)
                .verifyComplete();
    }
    @Test
    void addProductToInventory_WithInvalidInventoryIdAndValidBody_ShouldThrowNotFoundException(){
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .build();
        // Act and assert
        webTestClient
                .post()
                .uri("/inventories/{inventoryId}/products", "2")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

/*
    @Test
    public void testUpdateProductInInventory_ShouldSucceed() {
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Benzodiazepines")
                .productDescription("Updated Sedative Medication")
                .productPrice(150.00)
                .productQuantity(20)
                .build();

        webTestClient.put()
                .uri("/inventories/{inventoryId}/products/{productId}", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(productResponseDTO -> {
                    assertNotNull(productResponseDTO);
                    assertEquals(productId, productResponseDTO.getProductId());
                    assertEquals(productRequestDTO.getProductName(), productResponseDTO.getProductName());
                    assertEquals(productRequestDTO.getProductDescription(), productResponseDTO.getProductDescription());
                    assertEquals(productRequestDTO.getProductPrice(), productResponseDTO.getProductPrice());
                    assertEquals(productRequestDTO.getProductQuantity(), productResponseDTO.getProductQuantity());
                });
    }

 */

    @Test
    void testUpdateProductInInventory_ProductNotFound_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "1";
        String productId = UUID.randomUUID().toString();

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Benzodiazepines")
                .productDescription("Updated Sedative Medication")
                .productPrice(150.00)
                .productQuantity(20)
                .build();

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventories/{inventoryId}/products/{productId}", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }


//    @Test
//    void addProductToInventory_WithValidInventoryIdAndValidBody_ShouldSucceed(){
//        // Arrange
//        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
//                .productName("Benzodiazepines")
//                .productDescription("Sedative Medication")
//                .productPrice(100.00)
//                .productQuantity(10)
//                .build();
////        when(inventoryRepository.findInventoryByInventoryId("1")).thenReturn(Mono.just(Inventory.builder()
////                .inventoryId("1")
////                .inventoryType("Medication")
////                .inventoryDescription("Medication for procedures")
////                .build()));
//        // Act and assert
//        webTestClient
//                .post()
//                .uri("/inventories/{inventoryId}/products", "1")
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(productRequestDTO)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(ProductResponseDTO.class)
//                .value(dto -> {
//                    assertNotNull(dto);
//                    assertEquals(productRequestDTO.getProductName(), dto.getProductName());
//                    assertEquals(productRequestDTO.getProductDescription(), dto.getProductDescription());
//                    assertEquals(productRequestDTO.getProductPrice(), dto.getProductPrice());
//                    assertEquals(productRequestDTO.getProductQuantity(), dto.getProductQuantity());
//                });
//    }
}