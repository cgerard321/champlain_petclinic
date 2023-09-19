package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = InventoryController.class)
class InventoryControllerUnitTest {
    @Autowired
    WebTestClient webTestClient;
    @MockBean
    ProductInventoryService productInventoryService;

    ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
            .id("1")
            .inventoryId("1")
            .inventoryId("123F567C9")
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .build();
    ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .build();
//    @Test
//    void addProductToInventory_validProduct_ShouldSucceed(){
//        // Arrange
//        when(productInventoryService.addProductToInventory(Mono.just(productRequestDTO), productResponseDTO.getInventoryId()))
//                .thenReturn(Mono.just(productResponseDTO));
//        //Act and assert
//        webTestClient
//                .post()
//                .uri("/inventory/{inventoryId}/products", productResponseDTO.getInventoryId())
//                .bodyValue(productRequestDTO)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(ProductResponseDTO.class)
//                .value(dto -> {
//                    assertNotNull(dto);
//                    assertEquals(productResponseDTO.getId(), dto.getId());
//                    assertEquals(productResponseDTO.getInventoryId(), dto.getInventoryId());
//                    assertEquals(productResponseDTO.getSku(), dto.getSku());
//                    assertEquals(productResponseDTO.getProductName(), dto.getProductName());
//                    assertEquals(productResponseDTO.getProductDescription(), dto.getProductDescription());
//                    assertEquals(productResponseDTO.getProductPrice(), dto.getProductPrice());
//                    assertEquals(productResponseDTO.getProductQuantity(), dto.getProductQuantity());
//                });
//        verify(productInventoryService).addProductToInventory(Mono.just(productRequestDTO), productResponseDTO.getInventoryId());
//    }

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
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .id(productId)
                .inventoryId(inventoryId)
                .productName("Updated Product")
                .productDescription("Updated Description")
                .productPrice(200.00)
                .productQuantity(20)
                .build();

        when(productInventoryService.updateProductInInventory(any(), eq(inventoryId), eq(productId)))
                .thenReturn(Mono.just(responseDTO));

        //when(productInventoryService.updateProductInInventory(Mono.just(requestDTO), inventoryId, productId))
        //        .thenReturn(Mono.just(responseDTO));

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
                    assertEquals(responseDTO.getId(), dto.getId());
                    assertEquals(responseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(responseDTO.getProductName(), dto.getProductName());
                    assertEquals(responseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(responseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(responseDTO.getProductQuantity(), dto.getProductQuantity());
                });

        verify(productInventoryService, times(1))
                .updateProductInInventory(any(), eq(inventoryId), eq(productId));
    }

}