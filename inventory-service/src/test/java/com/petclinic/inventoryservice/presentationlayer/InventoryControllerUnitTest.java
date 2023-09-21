package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
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

import static com.petclinic.inventoryservice.datalayer.Inventory.InventoryType.internal;
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



    @Test
    void updateInventory_ValidRequest_ShouldReturnOk() {
        // Arrange
        String validInventoryId = "123";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("Updated Internal")
                .inventoryType(internal)
                .inventoryDescription("Updated inventory_3")
                .build();

        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("Updated Internal")
                .inventoryType(internal)
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
    void addInventory_ValidRequest_ShouldReturnCreated() {
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("New Internal")
                .inventoryType(internal)
                .inventoryDescription("New inventory_4")
                .build();

        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId("inventoryid1")
                .inventoryName("New Internal")
                .inventoryType(internal)
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

    @Test
    void updateInventory_InvalidId_ShouldReturnError() {
        // Arrange
        String invalidInventoryId = "invalid_id";
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("Updated Internal")
                .inventoryType(internal)
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
}
