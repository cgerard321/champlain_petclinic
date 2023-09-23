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
import reactor.test.StepVerifier;


import java.util.UUID;

import static com.petclinic.inventoryservice.datalayer.Inventory.InventoryType.internal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port:0"})
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

    Product product1 = buildProduct("productId_1","inventoryId_3","productName","productDescription",10.99, 100);


    @BeforeEach
    public void dbSetup(){


        Publisher<Inventory> inventoryPublisher = inventoryRepository.deleteAll()
                .thenMany(inventoryRepository.save(inventory1))
                .thenMany(inventoryRepository.save(inventory2));
        StepVerifier
                .create(inventoryPublisher)
                .expectNextCount(1)
                .verifyComplete();
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

        Publisher<Product> productPublisher1 = productRepository.save(Product.builder()
                .productId("productId_1")
                .inventoryId("inventoryId_3")
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .build());
        StepVerifier
                .create(productPublisher1)
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
                    assertEquals(2, list.size());
                });
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
    //delete all

    @Test
    void deleteProductInventory_WithValidInventoryId_ShouldDeleteAllProducts() {
        // Act
        webTestClient
                .delete()
                .uri("/inventories" +
                        "/{inventoryId}/products", "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Expecting 204 NO CONTENT status.
    }

    @Test
    void deleteAllInventories_ShouldDeleteAllInventoriesAndAssociatedProducts() {
        // Act
        webTestClient
                .delete()
                .uri("/inventory")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent(); // Expecting 204 NO CONTENT status.
    }


    @Test
    void testUpdateProductInInventory_WithNonExistentProductId_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "1";
        String productId = "test";

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Benzodiazepines")
                .productDescription("Updated Sedative Medication")
                .productPrice(150.00)
                .productQuantity(20)
                .build();

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/"+ inventoryId+ "/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    /*
    @Test
    public void testUpdateProductInInventory_ShouldSucceed() {
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Benzodiazepines 02")
                .productDescription("Updated Sedative Medication 02")
                .productPrice(150.00)
                .productQuantity(20)
                .build();
        webTestClient.put()
                .uri("/inventory/" + "inventoryId_3" + "/products/" + "productId_1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(productResponseDTO -> {
                    assertNotNull(productResponseDTO);
                    assertEquals(product1.getProductId(), "productId_1");
                    assertEquals(productRequestDTO.getProductName(), productResponseDTO.getProductName());
                    assertEquals(productRequestDTO.getProductDescription(), productResponseDTO.getProductDescription());
                    assertEquals(productRequestDTO.getProductPrice(), productResponseDTO.getProductPrice());
                    assertEquals(productRequestDTO.getProductQuantity(), productResponseDTO.getProductQuantity());
                });
    }

     */

    /*
    @Test
    public void testUpdateProductInInventory_ShouldSucceed() {
        // Arrange
        String validInventoryId = inventory1.getInventoryId();
        String validProductId = "productId_1";

        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Product Name")
                .productDescription("Updated Product Description")
                .productPrice(150.00)
                .productQuantity(20)
                .build();

        // Act and assert
        webTestClient
                .put()
                .uri("/inventory/" + validInventoryId + "/products/" + validProductId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(updatedProduct -> {
                    assertNotNull(updatedProduct);
                    assertEquals(validProductId, updatedProduct.getProductId());
                    assertEquals(productRequestDTO.getProductName(), updatedProduct.getProductName());
                    assertEquals(productRequestDTO.getProductDescription(), updatedProduct.getProductDescription());
                    assertEquals(productRequestDTO.getProductPrice(), updatedProduct.getProductPrice());
                    assertEquals(productRequestDTO.getProductQuantity(), updatedProduct.getProductQuantity());
                });
    }

     */




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


    private Inventory buildInventory(String inventoryId, String name, InventoryType inventoryType, String inventoryDescription) {
        return Inventory.builder()
                .inventoryId(inventoryId)
                .inventoryName(name)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .build();
    }

    private Product buildProduct(String productId,String inventoryId, String productName, String productDescription, Double productPrice, Integer productQuantity) {
        return Product.builder()
                .productId(productId)
                .inventoryId(inventoryId)
                .productName(productName)
                .productDescription(productDescription)
                .productPrice(productPrice)
                .productQuantity(productQuantity)
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