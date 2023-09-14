package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = InventoryController.class)
class InventoryControllerUnitTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    ProductInventoryService productInventoryService;

    ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
            .id("1")
            .inventoryId("1")
            .sku("123F567C9")
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

}