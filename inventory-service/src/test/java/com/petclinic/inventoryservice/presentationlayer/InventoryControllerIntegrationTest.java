package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.petclinic.inventoryservice.datalayer.Inventory.InventoryType.internal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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


    private final Long DB_SIZE =2L;

    Inventory inventory1 = buildInventory("inventoryId_3", "internal", InventoryType.internal ,"inventoryDescription_3");

    Inventory inventory2 = buildInventory("inventoryId_4", "sales", InventoryType.sales ,"inventoryDescription_4");



    @BeforeEach
    public void dbSetup(){


        Publisher<Inventory> inventoryPublisher = inventoryRepository.deleteAll()
                .thenMany(inventoryRepository.save(inventory1))
                .thenMany(inventoryRepository.save(inventory2));
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
                .uri("/inventory/{inventoryId}/products", "2")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }





    @Test
    public void addNewInventoryWithValidValues_shouldSucceed(){
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(internal)
                .inventoryDescription("inventory_3")
                .build();

        webTestClient.post()
                .uri("/inventory")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(inventoryResponseDTO -> {
                    assertNotNull(inventoryResponseDTO);
                    assertNotNull(inventoryResponseDTO.getInventoryId());
                    assertThat(inventoryResponseDTO.getInventoryName()).isEqualTo(inventoryResponseDTO.getInventoryName());


                });

    }



    @Test
    public void addNewInventoryWithInValidValues_throwInvalidInput(){
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(null)
                .inventoryDescription("inventory_3")
                .build();

        webTestClient.post()
                .uri("/inventory")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InvalidInputException.class)
                .value(invalidErrorResponse -> {
                    assertNotNull(invalidErrorResponse);
                    assertEquals("Invalid input data: inventory type cannot be blank.", invalidErrorResponse.getMessage());

                });
    }


    @Test
    public void updateInventory_withValidId_ShouldSucceed() {
        // Arrange
        String validInventoryId = "inventoryId_3";
        String updatedInventoryDescription = "Updated Medication for procedures";

        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(internal)
                .inventoryDescription(updatedInventoryDescription)
                .build();

        // Act and assert
        webTestClient
                .put()
                .uri("/inventory/{inventoryId}", validInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(inventoryResponseDTO -> {
                    assertNotNull(inventoryResponseDTO);
                    assertEquals(validInventoryId, inventoryResponseDTO.getInventoryId());
                    assertEquals(updatedInventoryDescription, inventoryResponseDTO.getInventoryDescription());

                });
    }



    @Test
    public void updateInventory_withInvalidInventoryId() {
        String InvalidInventoryId = "inventoryId_234";

        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(InventoryType.sales)
                .inventoryDescription("internal_id9")
                .build();
        webTestClient.put()
                .uri("/inventory/{inventoryId}",InvalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody();

    }


    private Inventory buildInventory(String inventoryId, String name, InventoryType inventoryType, String inventoryDescription) {
        return Inventory.builder()
                .inventoryId(inventoryId)
                .inventoryName(name)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .build();
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