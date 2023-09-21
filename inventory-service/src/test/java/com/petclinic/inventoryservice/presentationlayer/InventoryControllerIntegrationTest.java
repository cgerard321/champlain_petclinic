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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        /*Publisher<Product> productPublisher = productRepository.save(Product.builder()
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
                .verifyComplete();*/
        Publisher<Product> productPublisher = productRepository.deleteAll()
                .thenMany(productRepository.save(Product.builder()
                        .inventoryId("1")
                        .productId("123F567C9")
                        .productName("Benzodiazepines")
                        .productDescription("Sedative Medication")
                        .productPrice(100.00)
                        .productQuantity(10)
                        .build()))
                .thenMany(productRepository.save(Product.builder()
                        .inventoryId("1")
                        .productId("37483FGD")
                        .productName("Benzodiazepines")
                        .productDescription("Sedative Medication")
                        .productPrice(100.00)
                        .productQuantity(10)
                        .build()));
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

    @Test
    void getAllProductsInInventoryByInventoryId_withValidInventoryId_shouldSucceed(){
        String inventoryId = "1";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryType_throwsNotFoundException(){
        String invalidInventoryId = "123";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", invalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductName_andProductPrice_andProductQuantity_shouldSucceed(){
        String inventoryId = "1";
        String productName = "Benzodiazepines";
        Double productPrice = 100.00;
        Integer productQuantity = 10;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}&productQuantity={productQuantity}"
                        , inventoryId, productName, productPrice, productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductName_invalidProductPrice_invalidProductQuantity_throwsNotFoundException(){
        String invalidInventoryId = "123";
        String invalidProductName = "Meds";
        Double invalidProductPrice = 2833.0;
        Integer invalidProductQuantity = 2;


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}&productQuantity={productQuantity}",
                        invalidInventoryId, invalidProductName, invalidProductPrice, invalidProductQuantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductName: " + invalidProductName + "\nOr ProductPrice: " + invalidProductPrice + "\nOr ProductQuantity: " + invalidProductQuantity);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductName_shouldSucceed(){
        String inventoryId = "1";
        String productName = "Benzodiazepines";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}"
                        , inventoryId, productName)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductName_throwsNotFoundException(){
        String invalidInventoryId = "123";
        String invalidProductName = "Meds";


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}",
                        invalidInventoryId, invalidProductName)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductName: " + invalidProductName);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductPrice_shouldSucceed(){
        String inventoryId = "1";
        Double productPrice = 100.00;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}"
                        , inventoryId, productPrice)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }
    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductPrice_throwsNotFoundException(){
        String invalidInventoryId = "123";
        Double invalidProductPrice = 2833.0;


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}",
                        invalidInventoryId, invalidProductPrice)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductPrice: " + invalidProductPrice);
    }
    @Test
    void getAllProductsInInventoryByInventoryId_andProductQuantity_shouldSucceed(){
        String inventoryId = "1";
        Integer productQuantity = 10;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productQuantity={productQuantity}"
                        , inventoryId, productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductQuantity_throwsNotFoundException(){
        String invalidInventoryId = "123";
        Integer invalidProductQuantity = 2;


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productQuantity={productQuantity}",
                        invalidInventoryId, invalidProductQuantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductQuantity: " + invalidProductQuantity);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductPrice_andProductQuantity_shouldSucceed(){
        String inventoryId = "1";
        Double productPrice = 100.00;
        Integer productQuantity = 10;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}&productQuantity={productQuantity}"
                        , inventoryId, productPrice, productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductPrice_invalidProductQuantity_throwsNotFoundException(){
        String invalidInventoryId = "123";
        Double invalidProductPrice = 2833.0;
        Integer invalidProductQuantity = 9;


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}&productQuantity={productQuantity}",
                        invalidInventoryId, invalidProductPrice, invalidProductQuantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductPrice: " + invalidProductPrice + "\nOr ProductQuantity: " + invalidProductQuantity);
    }

    @Test
    void getAllInventory_shouldSucceed(){
        webTestClient.get()
                .uri("/inventory")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(InventoryResponseDTO.class)
                .value((list) ->{
                    assertNotNull(list);
                    assertEquals(10, list.size());
                });
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