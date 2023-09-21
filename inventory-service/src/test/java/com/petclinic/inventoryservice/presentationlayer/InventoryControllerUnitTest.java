package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    List<ProductResponseDTO> productResponseDTOS = Arrays.asList(
            ProductResponseDTO.builder()
                    .id("1")
                    .inventoryId("1")
                    .inventoryId("123F567C9")
                    .productName("Benzodiazepines")
                    .productDescription("Sedative Medication")
                    .productPrice(100.00)
                    .productQuantity(10)
                    .build(),
            ProductResponseDTO.builder()
                    .id("1")
                    .inventoryId("1")
                    .inventoryId("123F567C9")
                    .productName("Benzodiazepines")
                    .productDescription("Sedative Medication")
                    .productPrice(100.00)
                    .productQuantity(10)
                    .build()
    );
    ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
            .productName("Benzodiazepines")
            .productDescription("Sedative Medication")
            .productPrice(100.00)
            .productQuantity(10)
            .build();

    @Test
    void getProductsInInventory_withValidId_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", productResponseDTOS.get(1).getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_andValidProductPrice_andValidProductQuantity_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, 10))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}&productQuantity={productQuantity}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, 10)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_andValidProductPrice_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", 100.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductName_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines", null, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}",
                        productResponseDTOS.get(1).getInventoryId(), "Benzodiazepines")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductPrice_andValidProductQuantity_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, 100.00, 10))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}&productQuantity={productQuantity}",
                        productResponseDTOS.get(1).getInventoryId(), 100.00, 10)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductQuantity_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, null, 10))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productQuantity={productQuantity}",
                        productResponseDTOS.get(1).getInventoryId(), 10)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }

    @Test
    void getProductsInInventory_withValidId_andValidProductPrice_shouldSucceed(){
        when(productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(productResponseDTOS.get(1).getInventoryId(), null, 100.00, null))
                .thenReturn(Flux.fromIterable(productResponseDTOS));

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}",
                        productResponseDTOS.get(1).getInventoryId(),  100.00)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value(responseDTOs ->{
                    assertNotNull(responseDTOs);
                    assertEquals(productResponseDTOS.size(), responseDTOs.size());
                });
    }
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