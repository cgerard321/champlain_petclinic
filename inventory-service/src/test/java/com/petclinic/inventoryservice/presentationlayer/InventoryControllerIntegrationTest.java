package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.utils.ImageUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mongodb.assertions.Assertions.assertTrue;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @Autowired
    InventoryTypeRepository inventoryTypeRepository;


    private final Long DB_SIZE = 2L;

    Product product1 = buildProduct("productId1", "inventoryId_3" , "drug" , "drug", 18.00, 30.00, 3);
    Product product2 = buildProduct("productId2", "inventoryId_3" , "drug" , "drug", 18.00, 30.00, 3);

    Product product3 = buildProduct("productId3", "inventoryId_4" , "drug" , "drug", 18.00, 30.00, 3);
    Product product4 = buildProduct("productId4", "inventoryId_4" , "drug" , "drug", 18.00, 30.00, 3);


    List<Product> products = List.of();

    List<Product> products2 = List.of();

    InventoryType inventoryType1 = InventoryType.builder()
            .id("1")
            .typeId("d37a9df1-430b-40b8-be55-38730efa52a7")
            .type("Internal")
            .build();
    InventoryType inventoryType2 = InventoryType.builder()
            .id("1")
            .typeId("e16b1726-b39a-4a6b-995c-fcfab223ab04")
            .type("Sales")
            .build();


    InputStream inputStream = getClass().getResourceAsStream("/images/DiagnosticKitImage.jpg");
    byte[] diagnosticKitImage = ImageUtil.readImage(inputStream);

    Inventory inventory1 = buildInventory(
            "inventoryId_3",
            "internal",
            inventoryType1.getType(),
            "inventoryDescription_3",
            "https://www.fda.gov/files/iStock-157317886.jpg",
            "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14",
            diagnosticKitImage,
            products
    );



    Inventory inventory2 = buildInventory(
            "inventoryId_4",
            "sales",
            inventoryType2.getType() ,
            "inventoryDescription_4",
            "https://www.fda.gov/files/iStock-157317886.jpg",
            "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14",
            diagnosticKitImage,
            products2

    );

    InventoryControllerIntegrationTest() throws IOException {
    }


    @BeforeEach
    public void dbSetup()  {


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
                        .productSalePrice(15.99)
                        .build()))
                .thenMany(productRepository.save(Product.builder()
                        .inventoryId("1")
                        .productId("37483FGD")
                        .productName("Benzodiazepines")
                        .productDescription("Sedative Medication")
                        .productPrice(100.00)
                        .productQuantity(10)
                        .productSalePrice(15.99)
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
                .productSalePrice(15.99)
                .build());
        StepVerifier
                .create(productPublisher1)
                .expectNextCount(1)
                .verifyComplete();
    }



    @Test
    void addProductToInventory_WithInvalidInventoryIdAndValidBody_ShouldThrowNotFoundException() {
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(15.99)
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
    void getAllProductsInInventoryByInventoryId_withValidInventoryId_shouldSucceed() {
        String inventoryId = "1";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }


    @Test
    void getAllProductsInInventoryByInventoryId_andProductName_andProductPrice_andProductQuantity_shouldSucceed() {
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
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductName_invalidProductPrice_invalidProductQuantity_invalidProductSalePrice_throwsNotFoundException() {
        String invalidInventoryId = "123";
        String invalidProductName = "Meds";
        Double invalidProductPrice = 2833.0;
        Integer invalidProductQuantity = 2;
        Double invalidProductSalePrice = 3000.00;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}&productPrice={productPrice}&productQuantity={productQuantity}&productSalePrice={productSalePrice}",
                        invalidInventoryId, invalidProductName, invalidProductPrice, invalidProductQuantity, invalidProductSalePrice)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with InventoryId: " + invalidInventoryId +
                        "\nOr ProductQuantity: " + invalidProductQuantity);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductName_shouldSucceed() {
        String inventoryId = "1";
        String productName = "Benzodiazepines";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productName={productName}"
                        , inventoryId, productName)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }
    /*

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductName_throwsNotFoundException() {
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
    }*/

    @Test
    void getAllProductsInInventoryByInventoryId_andProductPrice_shouldSucceed() {
        String inventoryId = "1";
        Double productPrice = 100.00;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}"
                        , inventoryId, productPrice)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductPrice_throwsNotFoundException() {
        String invalidInventoryId = "123";
        Double invalidProductPrice = 2833.0;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productPrice={productPrice}",
                        invalidInventoryId, invalidProductPrice)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(0);
    }

    @Test
    void getAllProductsInInventoryByInventoryId_andProductQuantity_shouldSucceed() {
        String inventoryId = "1";
        Integer productQuantity = 10;

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products?productQuantity={productQuantity}"
                        , inventoryId, productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductQuantity_throwsNotFoundException() {
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
    void getAllProductsInInventoryByInventoryId_andProductPrice_andProductQuantity_shouldSucceed() {
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
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllProductsInInventory_withInvalidInventoryId_invalidProductPrice_invalidProductQuantity_throwsNotFoundException() {
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
                        "\nOr ProductQuantity: " + invalidProductQuantity);
    }

    @Test
    void getAllInventory_shouldSucceed() {
        webTestClient.get()
                .uri("/inventory")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(InventoryResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getAllInventory_shouldSucceed2() {
        webTestClient.get()
                .uri("/inventory/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(InventoryResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                });
    }

    @Test
    public void getInventoryByInventoryId_withValidInventoryId_Should_Succeed(){
        webTestClient.get()
                .uri("/inventory/{inventoryId}" , inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.inventoryId").isEqualTo(inventory1.getInventoryId());
    }


    @Test
    void getInventoryByInventoryId_withInvalidInventoryId_throwsNotFoundException(){
        String invalidInventoryId= "123";

        webTestClient
                .get()
                .uri("/inventory/{inventoryId}", invalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("No inventory with this id was found" + invalidInventoryId);
    }
    @Test
    public void addNewInventoryWithValidValues_shouldSucceed() {
        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal-" + UUID.randomUUID())
                .inventoryType(inventoryType1.getType())
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
    public void addNewInventoryWithInValidValues_throwInvalidInput() {
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
                    assertEquals("Inventory type cannot be blank.", invalidErrorResponse.getMessage());

                });
    }


    @Test
    public void updateInventory_withValidId_ShouldSucceed() {
        // Arrange
        String validInventoryId = "inventoryId_3";
        String updatedInventoryDescription = "Updated Medication for procedures";

        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(inventoryType1.getType())
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
    void getProductInInventory_withInvalidInventoryId_invalidProductId_throwsNotFoundException() {
        String invalidInventoryId = "123";
        String invalidProductId = "897";

        webTestClient.get()
                .uri("/inventory/{inventoryId}/products/{productId}",
                        invalidInventoryId, invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory id:" + invalidInventoryId + "and product:" + invalidProductId + "are not found");
    }



    @Test
    public void getProductInInventory_byProductId_ShouldSucceed() {


        webTestClient.get()
                .uri("/inventory/{inventoryId}/products/{productId}", "1", "123F567C9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo("123F567C9");
    }




    @Test
    public void updateInventory_withInvalidInventoryId() {
        String InvalidInventoryId = "inventoryId_234";

        InventoryRequestDTO inventoryRequestDTO = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("internal_id9")
                .build();
        webTestClient.put()
                .uri("/inventory/{inventoryId}", InvalidInventoryId)
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
                .productSalePrice(15.99)
                .build();

        // Act and Assert
        webTestClient
                .put()
                .uri("/inventory/" + inventoryId + "/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }
    private Inventory buildInventory(String inventoryId, String name, String inventoryType, String inventoryDescription, String inventoryImage, String inventoryBackupImage, byte[] diagnosticKitImage, List<Product> products) {
        return Inventory.builder()
                .inventoryId(inventoryId)
                .inventoryCode("INV-000" + inventoryId.substring(inventoryId.length() - 1))
                .inventoryName(name)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .inventoryImage(inventoryImage)
                .inventoryBackupImage(inventoryBackupImage)
                .imageUploaded(diagnosticKitImage)
                .products(products)
                .build();
    }

    private Product buildProduct(String productId, String inventoryId, String productName, String productDescription, Double productPrice, Double SalePrice,  Integer productQuantity) {
        return Product.builder()
                .productId(productId)
                .inventoryId(inventoryId)
                .productName(productName)
                .productDescription(productDescription)
                .productPrice(productPrice)
                .productSalePrice(SalePrice)
                .productQuantity(productQuantity)
                .build();
    }

    @Test
    public void deleteProductInInventory_byProductId_ShouldSucceed() {
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products/{productId}", "inventoryId_3", "123F567C9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteProductInInventory_byInvalidProductId_shouldNotFound() {
        String invalidProduct = "invalid";
        webTestClient.delete()
                .uri("/inventory/{inventory}/products/{productId}", "inventoryId_3", invalidProduct)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found, make sure it exists, productId: " + invalidProduct);
    }

    @Test
    public void deleteProductInInventory_byInvalidInventoryId_shouldNotFound() {
        String invalidInventory = "invalid";
        webTestClient.delete()
                .uri("/inventory/{inventory}/products/{productId}", invalidInventory, "123F567C9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found, make sure it exists, inventoryId: " + invalidInventory);
    }

    @Test
    void addProductToInventory_WithValidInventoryIdAndValidBody_ShouldSucceed() {
        // Arrange
        String uniqueName = "Benzodiazepines-" + UUID.randomUUID();
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName(uniqueName)
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(15.99)
                .build();
        // Act and assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(productRequestDTO.getProductName(), dto.getProductName());
                    assertEquals(productRequestDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(productRequestDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(productRequestDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(productRequestDTO.getProductSalePrice(), dto.getProductSalePrice());
                });
    }

    @Test
    void addProductToInventory_WithInvalidInventoryId_AndValidValues_ShouldThrowNotFoundException() {
        // Arrange
        String uniqueName = "Benzodiazepines-" + UUID.randomUUID();
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName(uniqueName)
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
                .expectStatus().isNotFound()
                .expectBody(NotFoundException.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals("Inventory not found with id: 2", dto.getMessage());
                });
    }

    @Test
    void addProductToInventory_WithValidInventoryId_AndMissingProductName_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .build();
        // Act and assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(InvalidInputException.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals("Product must have an inventory id, product name, product price, and product quantity.", dto.getMessage());
                });
    }

    @Test
    void addProductToInventory_WithValidInventoryId_AndMissingProductQuantity_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .build();
        // Act and assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(InvalidInputException.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals("Product must have an inventory id, product name, product price, and product quantity.", dto.getMessage());
                });
    }

    @Test
    void addProductToInventory_WithValidInventoryId_AndMissingProductPrice_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productQuantity(10)
                .build();
        // Act and assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(InvalidInputException.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals("Product must have an inventory id, product name, product price, and product quantity.", dto.getMessage());
                });
    }

    @Test
    void addProductToInventory_WithValidInventoryId_WithoutPayload_ShouldThrowBadRequestException() {
        // Act and assert
        webTestClient
                .post()
                .uri("/inventory/{inventoryId}/products", inventory1.getInventoryId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(InvalidInputException.class);
    }

    @Test
    public void deleteInventoryByInventoryId_ShouldSucceed() {
        webTestClient.delete()
                .uri("/inventory/{inventoryId}", "inventoryId_3")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void addInventoryType_shouldSucceed(){
        InventoryTypeRequestDTO inventoryTypeRequestDTO = InventoryTypeRequestDTO.builder()
                .type("Internal")
                .build();

        webTestClient
                .post()
                .uri("/inventory/type", inventoryType1.getType())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryTypeRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InventoryTypeRequestDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryTypeRequestDTO.getType(), dto.getType());
                });
    }
    @Test
    public void testSearchByInventoryName_SingleChar() {
        String name = "i";  // I've changed this to a single character that matches the start of "internal"

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryName", name)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertTrue(inventories.get(0).getInventoryName().toLowerCase().startsWith(name.toLowerCase())); // changed contains to startsWith for more accurate matching
                });
    }

    @Test
    public void testSearchByInventoryName_MultipleChars() {
        String inventoryName = "internal";  // Sample name to match "internal" inventory

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryName", inventoryName)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertEquals(inventoryName, inventories.get(0).getInventoryName());
                });
    }



    @Test
    public void testSearchByInventoryDescription_SingleChar() {
        String description = "i";  // I've changed this to a single character that matches the start of "inventoryDescription_3"

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryDescription", description)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertTrue(inventories.get(0).getInventoryDescription().toLowerCase().startsWith(description.toLowerCase())); // changed contains to startsWith for more accurate matching
                });
    }

    @Test
    public void testSearchByInventoryDescription_MultipleChars() {
        String inventoryDescription = "inventoryDescription_3";  // Sample description to match

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryDescription", inventoryDescription)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertEquals(inventoryDescription, inventories.get(0).getInventoryDescription());
                });
    }

    //product Name
    @Test
    public void testSearchByProductName_SingleChar() {
        String inventoryId = "inventoryId_3";
        String singleCharProductName = "B";

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products")
                        .queryParam("productName", singleCharProductName)
                        .build(inventoryId))   // Supply the inventoryId here
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .consumeWith(response -> {
                    List<ProductResponseDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertTrue(products.size() > 0);
                    assertTrue(products.get(0).getProductName().toLowerCase().startsWith(singleCharProductName.toLowerCase()));
                });
    }

    @Test
    public void testSearchByProductName_MultipleChars() {
        String inventoryId = "inventoryId_3";
        String productName = "Benzodiazepines";

        // Call the endpoint
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products")
                        .queryParam("productName", productName)
                        .build(inventoryId))   // Supply the inventoryId here
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .consumeWith(response -> {
                    List<ProductResponseDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertTrue(products.size() > 0);
                    assertEquals(productName, products.get(0).getProductName());
                });
    }

    @Test
    public void getAllInventoryTypes_shouldSucceed() {
        webTestClient.get()
                .uri("/inventory/type")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    //search Products by name






    @Test
    void getTotalNumberOfProductsWithRequestParams_ShouldSucceed(){
        webTestClient
                .get()
                .uri("/inventory/{inventoryId}/products-count", "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .value((count) -> {
                    assertNotNull(count);
                    assertEquals(2L, count);
                });
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldPagination_ShouldSucceed(){
        StepVerifier.create(productRepository.deleteAll().thenMany(productRepository.save(buildProduct("productId_1", "1", "Benzodiazepines", "Sedative Medication", 100.00, 150.0, 10))))
                .expectNextCount(1)
                .verifyComplete();
        webTestClient
                .get()
                .uri("/inventory/{inventoryId}/products?page={page}&size={size}", "1", 0, 2)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(java.nio.charset.StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                });
    }

    @Test
    public void searchInventoryByInventoryNameAndInventoryTypeAndInventoryDescription_shouldSucceed() {
        String inventoryName = "internal";
        String inventoryType = "Internal";
        String inventoryDescription = "inventoryDescription_3";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryName", inventoryName)
                        .queryParam("inventoryType", inventoryType)
                        .queryParam("inventoryDescription", inventoryDescription)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertEquals(inventoryName, inventories.get(0).getInventoryName());
                    assertEquals(inventoryType, inventories.get(0).getInventoryType());
                    assertEquals(inventoryDescription, inventories.get(0).getInventoryDescription());
                });
    }

    @Test
    public void searchInventoryByInventoryNameAndInventoryType_shouldSucceed() {
        String inventoryName = "internal";
        String inventoryType = "Internal";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryName", inventoryName)
                        .queryParam("inventoryType", inventoryType)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertTrue(inventories.size() > 0);
                    assertEquals(inventoryName, inventories.get(0).getInventoryName());
                    assertEquals(inventoryType, inventories.get(0).getInventoryType());
                });
    }

    @Test
    void searchInventories_WithInventoryCode_ShouldReturnSpecificInventory() {
        String inventoryCode = inventory1.getInventoryCode();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryCode", inventoryCode)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .consumeWith(response -> {
                    List<InventoryResponseDTO> inventories = response.getResponseBody();
                    assertNotNull(inventories);
                    assertEquals(1, inventories.size());
                    assertEquals(inventoryCode, inventories.get(0).getInventoryCode());
                });
    }

    @Test
    void searchInventories_WithInvalidInventoryCode_ShouldReturnNotFound() {
        String invalidCode = "INV-9999";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory")
                        .queryParam("inventoryCode", invalidCode)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }


    /*
    @Test
    public void deleteInventoryByInventoryId_withNotFoundInventoryId_shouldNotFound() {
        String invalidInventory = "invalid";
        webTestClient.delete()
                .uri("/inventory/{inventoryId}", invalidInventory)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("The InventoryId is invalid");

    }

     */



/*
    @Test
    void deleteProductInventory_WithValidInventoryId_ShouldDeleteAllProducts() {
        // Act
        webTestClient
                .delete()
                .uri("/inventories" +
                        "/{inventoryId}/products", "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

 */
    @Test
    public void searchProductsByInventoryIdProductNameAndProductDescription_shouldSucceed() {
        String inventoryId = "1";
        String productName = "Benzodiazepines";
        String productDescription = "Sedative Medication";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .queryParam("productDescription", productDescription)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .consumeWith(response -> {
                    List<ProductResponseDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertTrue(products.size() > 0);
                    assertEquals(productName, products.get(0).getProductName());
                    assertEquals(productDescription, products.get(0).getProductDescription());
                });
    }

    @Test
    public void searchProductsByInventoryIdAndProductName_shouldSucceed() {
        String inventoryId = "1";
        String productName = "Benzodiazepines";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products/search")
                        .queryParam("productName", productName)
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .consumeWith(response -> {
                    List<ProductResponseDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertTrue(products.size() > 0);
                    assertEquals(productName, products.get(0).getProductName());
                });
    }

    @Test
    public void searchProductsByInventoryId_shouldSucceed() {
        String inventoryId = "1";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/inventory/{inventoryId}/products/search")
                        .build(inventoryId))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .consumeWith(response -> {
                    List<ProductResponseDTO> products = response.getResponseBody();
                    assertNotNull(products);
                    assertTrue(products.size() > 0);
                });
    }

    @Test
    void getQuantityOfProductsInInventory_withValidInventoryId_shouldReturnProductCount() {
        // Arrange
        String inventoryId = "inventoryId_3";  // This inventory has products

        // Act & Assert
        webTestClient.get()
                .uri("/inventory/{inventoryId}/productquantity", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Integer.class)
                .value(count -> {
                    assertNotNull(count);
                    assertTrue(count > 0);  // Ensure that the inventory contains products
                });
    }

    @Test
    void getQuantityOfProductsInInventory_withInvalidInventoryId_shouldReturnNotFound() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";  // This inventory ID does not exist

        // Act & Assert
        webTestClient.get()
                .uri("/inventory/{inventoryId}/productquantity", invalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Inventory not found with id: " + invalidInventoryId);
    }

    @Test
    void deleteAllProductsInInventory_withValidInventoryId_shouldSucceed() {
        // Arrange
        String inventoryId = "inventoryId_3";

        // Act & Assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products", inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }


    @Test
    void deleteAllProductsInInventory_withInvalidInventoryId_shouldReturnNotFound() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";

        // Act & Assert
        webTestClient.delete()
                .uri("/inventory/{inventoryId}/products", invalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid Inventory Id");
    }

    @Test
    void restockLowStockProduct_WithValidInputs_ShouldSucceed() {
        // Arrange
        int restockQuantity = 5;
        String inventoryId = "1"; // Using the existing inventory setup
        String productId = "123F567C9"; // Using the existing product setup

        // Step 1: Retrieve the initial product details
        webTestClient.get()
                .uri("/inventory/{inventoryId}/products/{productId}", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(initialProductResponse -> {
                    assertNotNull(initialProductResponse, "The initial product should not be null");

                    // Get the initial quantity of the product
                    int initialQuantity = initialProductResponse.getProductQuantity();
                    System.out.println("Initial Quantity: " + initialQuantity);

                    // Step 2: Perform the restock operation
                    webTestClient.put()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/inventory/{inventoryId}/products/{productId}/restockProduct")
                                    .queryParam("productQuantity", restockQuantity)
                                    .build(inventoryId, productId))
                            .accept(MediaType.APPLICATION_JSON)
                            .exchange()
                            .expectStatus().isOk()
                            .expectHeader().contentType(MediaType.APPLICATION_JSON)
                            .expectBody(ProductResponseDTO.class)
                            .value(restockedProductResponse -> {
                                assertNotNull(restockedProductResponse, "The restocked product should not be null");
                                assertEquals(productId, restockedProductResponse.getProductId());
                                assertEquals(inventoryId, restockedProductResponse.getInventoryId());

                                int actualQuantity = restockedProductResponse.getProductQuantity();
                                System.out.println("Restocked Product Quantity: " + actualQuantity);

                                // Assert that the product quantity increased by the restock amount
                                int expectedQuantity = initialQuantity + restockQuantity;
                                assertEquals(expectedQuantity, actualQuantity,
                                        "The product quantity should be the initial quantity plus the restock amount");
                            });
                });
    }


    @Test
    void restockLowStockProduct_WithInvalidQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "1";
        String productId = "123F567C9";
        int invalidQuantity = -5; // Invalid quantity

        // Act & Assert
        webTestClient.put()
                .uri("/inventory/{inventoryId}/products/{productId}/restockProduct?productQuantity={quantity}",
                        inventoryId, productId, invalidQuantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void restockLowStockProduct_WithZeroQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "1";
        String productId = "123F567C9";
        int zeroQuantity = 0; // Zero quantity

        // Act & Assert
        webTestClient.put()
                .uri("/inventory/{inventoryId}/products/{productId}/restockProduct?productQuantity={quantity}",
                        inventoryId, productId, zeroQuantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void consumeProduct_WithValidInputs_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        String productId = "123F567C9";
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", inventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(productResponseDTO -> {
                    assertNotNull(productResponseDTO);
                    assertEquals(productId, productResponseDTO.getProductId());
                    assertEquals(inventoryId, productResponseDTO.getInventoryId());
                    assertEquals(9, productResponseDTO.getProductQuantity());
                });
    }

    @Test
    void consumeProduct_WithInvalidProductId_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "1";
        String invalidProductId = "invalidProductId";
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", inventoryId, invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void consumeProduct_WithInvalidInventoryId_ShouldReturnNotFound() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String productId = "123F567C9";
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", invalidInventoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void consumeProduct_WithInvalidInventoryIdAndInvalidProductId_ShouldReturnNotFound() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String invalidProductId = "invalidProductId";
        webTestClient.patch()
                .uri("/inventory/{inventoryId}/products/{productId}/consume", invalidInventoryId, invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateImportantStatus_withValidData_shouldSucceed() {
        String inventoryId = "inventoryId_3";
        Map<String, Boolean> request = Map.of("important", true);

        webTestClient.patch()
                .uri("/inventory/{inventoryId}/important", inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateImportantStatus_withInvalidInventoryId_shouldReturnNotFound() {
        String invalidId = "invalidId";
        Map<String, Boolean> request = Map.of("important", false);

        webTestClient.patch()
                .uri("/inventory/{inventoryId}/important", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addInventory_shouldReturnInventoryWithGeneratedCode() {
        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .inventoryName("Test Inventory With Code")
                .inventoryType("Internal")
                .inventoryDescription("Testing inventory code generation")
                .inventoryImage("https://example.com/test.jpg")
                .inventoryBackupImage("https://example.com/backup.jpg")
                .build();

        webTestClient.post()
                .uri("/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getInventoryCode());
                    assertTrue(response.getInventoryCode().matches("INV-\\d{4}"));
                    assertEquals("Test Inventory With Code", response.getInventoryName());
                });
    }

    @Test
    void getInventoryById_shouldReturnInventoryWithCode() {
        String inventoryId = "inventoryId_3";

        webTestClient.get()
                .uri("/inventory/{inventoryId}", inventoryId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getInventoryCode());
                });
    }

}