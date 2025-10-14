package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.Inventory.Status;
import com.petclinic.bffapigateway.exceptions.InvalidInputsInventoryException;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.exceptions.InventoryProductUnprocessableEntityException;
import com.petclinic.bffapigateway.exceptions.ProductListNotFoundException;
import com.petclinic.bffapigateway.utils.InventoryUtils.ImageUtil;
import com.petclinic.bffapigateway.utils.Rethrower;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceClientIntegrationTest {

    @MockBean
    private InventoryServiceClient inventoryServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static MockWebServer mockWebServer;

    InventoryServiceClientIntegrationTest() throws IOException {
    }

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize(){
        inventoryServiceClient = new InventoryServiceClient("localhost", String.valueOf(mockWebServer.getPort()));
        ReflectionTestUtils.setField(inventoryServiceClient, "rethrower", new Rethrower(new ObjectMapper()));
    }

    @AfterAll
    static void tearDown() throws IOException{
        mockWebServer.shutdown();
    }

    InputStream inputStream = getClass().getResourceAsStream("/images/DiagnosticKitImage.jpg");
    byte[] diagnosticKitImage = ImageUtil.readImage(inputStream);

    private void enqueueError(int status, String message) {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(status)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{\"message\":\"" + message + "\"}")
        );
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductsField() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Flux<ProductResponseDTO> productResponseDTOFlux = inventoryServiceClient
                .getProductsInInventoryByInventoryIdAndProductsField(productResponseDTO.getInventoryId(),
                        productResponseDTO.getProductName(), productResponseDTO.getProductPrice(), productResponseDTO.getProductQuantity(), productResponseDTO.getProductSalePrice());
        StepVerifier.create(productResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addProductsToInventory() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOFlux = inventoryServiceClient
                .addProductToInventory(new ProductRequestDTO(), productResponseDTO.getInventoryId());
        StepVerifier.create(productResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addInventoryType() throws JsonProcessingException {
        InventoryTypeResponseDTO inventoryTypeResponseDTO = new InventoryTypeResponseDTO(
                "142f383f-9fbf-479b-95e3-6c928f6a290b",
                "Internal"
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(inventoryTypeResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<InventoryTypeResponseDTO> inventoryTypeResponseDTOMono = inventoryServiceClient
                .addInventoryType(new InventoryTypeRequestDTO());
        StepVerifier.create(inventoryTypeResponseDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getInventoryTypes() throws JsonProcessingException{
        InventoryTypeResponseDTO inventoryTypeResponseDTO = new InventoryTypeResponseDTO(
                "142f383f-9fbf-479b-95e3-6c928f6a290b",
                "Internal"
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(inventoryTypeResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Flux<InventoryTypeResponseDTO> inventoryTypeResponseDTOFlux = inventoryServiceClient
                .getAllInventoryTypes();
        StepVerifier.create(inventoryTypeResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldPagination() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );
        ProductResponseDTO productResponseDTO1 = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        Flux<ProductResponseDTO> productFlux = Flux.just(productResponseDTO, productResponseDTO1);
        final String body = objectMapper.writeValueAsString(productFlux.collectList().block());
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(body));
        final Flux<ProductResponseDTO> productResponseDTOFlux = inventoryServiceClient.
                getProductsInInventoryByInventoryIdAndProductFieldPagination("1",null,
                        null,null, Optional.of(0),Optional.of(2));
        Long fluxSize = productResponseDTOFlux.count().block();
        Long predictedSize = (long) 2;
        assertEquals(predictedSize, fluxSize);
    }

    @Test
    void getTotalNumberOfProductsWithRequestParams() throws JsonProcessingException {
        long expected = 0;
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(String.valueOf(expected)));
        final Mono<Long> productResponseDTOFlux = inventoryServiceClient.getTotalNumberOfProductsWithRequestParams("1",null,
                null,null);
        assertEquals(expected, productResponseDTOFlux.block());
    }

    @Test
    void getQuantityOfProductsInInventory_withValidInventoryId_shouldReturnQuantity() throws JsonProcessingException {
        // Arrange
        int expectedQuantity = 10;
        String inventoryId = "validInventoryId";

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(String.valueOf(expectedQuantity)));

        // Act
        Mono<Integer> result = inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedQuantity)
                .verifyComplete();
    }

    @Test
    void getQuantityOfProductsInInventory_withEmptyInventory_shouldReturnZero() throws JsonProcessingException {
        // Arrange
        int expectedQuantity = 0;
        String inventoryId = "emptyInventoryId";

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(String.valueOf(expectedQuantity)));

        // Act
        Mono<Integer> result = inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedQuantity)
                .verifyComplete();
    }

    @Test
    void getQuantityOfProductsInInventory_withInvalidInventoryId_shouldThrowInventoryNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Mono<Integer> result = inventoryServiceClient.getQuantityOfProductsInInventory(invalidInventoryId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("Inventory not found"))
                .verify();
    }


    @Test
    void restockLowStockProduct_ValidRequest_ShouldReturnProduct() throws JsonProcessingException {
        // Arrange
        String inventoryId = "inventoryId_1";
        String productId = "productId_1";
        Integer productQuantity = 10;

        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                productId,
                inventoryId,
                "Restocked Product",
                "Restocked Description",
                100.00,
                productQuantity,
                15.99,
                Status.AVAILABLE,
                LocalDateTime.now()
        );

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(productResponseDTO)));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.restockLowStockProduct(inventoryId, productId, productQuantity);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertEquals(productId, response.getProductId());
                    assertEquals(inventoryId, response.getInventoryId());
                    assertEquals(productQuantity, response.getProductQuantity());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void restockLowStockProduct_withInvalidInventoryIdOrProductId_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "invalidInventoryId";
        String productId = "invalidProductId";
        Integer productQuantity = 10;

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Product not found\"}"));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.restockLowStockProduct(inventoryId, productId, productQuantity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("Product: " + productId + " not found in inventory: " + inventoryId))
                .verify();
    }






    @Test
    void updateProductInventoryId_withValidIds_shouldReturnUpdatedProduct() throws JsonProcessingException {
        // Arrange
        String currentInventoryId = "currentInventoryId";
        String productId = "productId";
        String newInventoryId = "newInventoryId";

        ProductResponseDTO updatedProductResponseDTO = new ProductResponseDTO(
                productId,
                newInventoryId,
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(updatedProductResponseDTO))
                .addHeader("Content-Type", "application/json"));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.updateProductInventoryId(currentInventoryId, productId, newInventoryId);

        // Assert
        StepVerifier.create(result)
                .expectNext(updatedProductResponseDTO)
                .verifyComplete();
    }

    @Test
    void updateProductInventoryId_withInvalidProductId_shouldThrowNotFoundException() {
        // Arrange
        String currentInventoryId = "currentInventoryId";
        String productId = "invalidProductId";
        String newInventoryId = "newInventoryId";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Product not found in inventory: " + currentInventoryId + "\"}"));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.updateProductInventoryId(currentInventoryId, productId, newInventoryId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable != null
                        && throwable.getMessage().contains("Product not found in inventory: " + currentInventoryId))
                .verify();
    }

    @Test
    void getAllInventories_shouldReturnAllInventories() throws JsonProcessingException {
        // Arrange
        ProductResponseDTO productResponseDTO1 = new ProductResponseDTO(
                "productId1",
                "inventoryId1",
                "name1",
                "desc1",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        ProductResponseDTO productResponseDTO2 = new ProductResponseDTO(
                "productId2",
                "inventoryId2",
                "name2",
                "desc2",
                12.00,
                3,
                17.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        // Create lists of products for inventories
        List<ProductResponseDTO> productResponseDTOList1 = new ArrayList<>(Arrays.asList(productResponseDTO1));
        List<ProductResponseDTO> productResponseDTOList2 = new ArrayList<>(Arrays.asList(productResponseDTO2));

        InventoryResponseDTO inventoryResponseDTO1 = new InventoryResponseDTO("inventoryId1","INVT-1001", "Medication", "Medications", "desc1", "", "", diagnosticKitImage, false, productResponseDTOList1, "No recent updates.");
        InventoryResponseDTO inventoryResponseDTO2 = new InventoryResponseDTO("inventoryId2","INVT-1002", "Vaccine", "Vaccines", "desc2", "", "", diagnosticKitImage, false, productResponseDTOList2, "No recent updates.");

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(Arrays.asList(inventoryResponseDTO1, inventoryResponseDTO2)))
                .addHeader("Content-Type", "application/json"));

        // Act
        Flux<InventoryResponseDTO> result = inventoryServiceClient.getAllInventories();

        // Assert
        StepVerifier.create(result)
                .expectNext(inventoryResponseDTO1, inventoryResponseDTO2)
                .verifyComplete();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescriptionAndStatus() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Flux<ProductResponseDTO> productResponseDTOFlux = inventoryServiceClient
                .searchProducts(productResponseDTO.getInventoryId(),
                        productResponseDTO.getProductName(),
                        productResponseDTO.getProductDescription(),
                        productResponseDTO.getStatus());
        StepVerifier.create(productResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescriptionAndStatus_withInvalidInventoryId_shouldThrowNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String productName = "productName";
        String productDescription = "productDescription";
        Status status = Status.OUT_OF_STOCK;

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(invalidInventoryId, productName, productDescription, status);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + invalidInventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescriptionAndStatus_withInvalidProductName_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId";
        String invalidProductName = "invalidProductName";
        String productDescription = "productDescription";
        Status status = Status.OUT_OF_STOCK;

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(inventoryId, invalidProductName, productDescription, status);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + inventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescriptionAndStatus_withInvalidProductDescription_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId";
        String productName = "productName";
        String invalidProductDescription = "invalidProductDescription";
        Status status = Status.OUT_OF_STOCK;

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(inventoryId, productName, invalidProductDescription, status);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + inventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescription() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Flux<ProductResponseDTO> productResponseDTOFlux = inventoryServiceClient
                .searchProducts(productResponseDTO.getInventoryId(),
                        productResponseDTO.getProductName(),
                        productResponseDTO.getProductDescription(),
                        null);
        StepVerifier.create(productResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescription_withInvalidInventoryId_shouldThrowNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String productName = "productName";
        String productDescription = "productDescription";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(invalidInventoryId, productName, productDescription, null);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + invalidInventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescription_withInvalidProductName_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId";
        String invalidProductName = "invalidProductName";
        String productDescription = "productDescription";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(inventoryId, invalidProductName, productDescription, null);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + inventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void searchProductsInInventoryByInventoryIdAndProductNameAndProductDescription_withInvalidProductDescription_shouldThrowNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId";
        String productName = "productName";
        String invalidProductDescription = "invalidProductDescription";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("{\"message\": \"Inventory not found\"}"));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts(inventoryId, productName, invalidProductDescription, null);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("No products found in inventory: " + inventoryId + " that match the search criteria"))
                .verify();
    }

    @Test
    void consumeProduct_withValidInventoryIdAndProductId_shouldReturnProduct() throws JsonProcessingException {
        // Arrange
        ProductResponseDTO product = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(product)));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.consumeProduct("inventoryId", "productId");

        // Assert
        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void consumeProduct_withInvalidInventoryId_shouldThrowInventoryNotFoundException() {
        // Arrange
        String invalidInventoryId = "invalidInventoryId";
        String productId = "productId";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\": \"Product not found in inventory: " + invalidInventoryId + "\"}"));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.consumeProduct(invalidInventoryId, productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("Product not found in inventory: " + invalidInventoryId))
                .verify();
    }

    @Test
    void consumeProduct_withInvalidProductId_shouldThrowProductListNotFoundException() {
        // Arrange
        String inventoryId = "inventoryId";
        String invalidProductId = "invalidProductId";

        // Mock a 404 Not Found response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\": \"Product not found in inventory: " + inventoryId + "\"}"));

        // Act
        Mono<ProductResponseDTO> result = inventoryServiceClient.consumeProduct(inventoryId, invalidProductId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InventoryNotFoundException
                        && throwable.getMessage().contains("Product not found in inventory: " + inventoryId))
                .verify();
    }

    @Test
    void getInventoryById_shouldReturnInventoryWithRecentUpdateMessage() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        List<ProductResponseDTO> products = new ArrayList<>(Arrays.asList(productResponseDTO));

        InventoryResponseDTO inventoryResponseDTO = new InventoryResponseDTO(
                "inventoryId1",
                "INV-0001",
                "Medication",
                "Medications",
                "desc1",
                "",
                "",
                diagnosticKitImage,
                false,
                products,
                "1 supplies updated in the last 15 min."
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(inventoryResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<InventoryResponseDTO> result = inventoryServiceClient.getInventoryById("inventoryId1");

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assertNotNull(response.getRecentUpdateMessage());
                    assertEquals("1 supplies updated in the last 15 min.", response.getRecentUpdateMessage());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void searchInventory_shouldReturnInventoriesWithRecentUpdateMessages() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "inventoryId1",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );

        List<ProductResponseDTO> products = new ArrayList<>(Arrays.asList(productResponseDTO));

        InventoryResponseDTO inventory1 = new InventoryResponseDTO(
                "inventoryId1",
                "INV-0001",
                "Medication",
                "Medications",
                "desc1",
                "",
                "",
                diagnosticKitImage,
                false,
                products,
                "2 supplies updated in the last 15 min."
        );

        InventoryResponseDTO inventory2 = new InventoryResponseDTO(
                "inventoryId2",
                "INV-0002",
                "Vaccine",
                "Vaccines",
                "desc2",
                "",
                "",
                diagnosticKitImage,
                false,
                new ArrayList<>(),
                "No recent updates."
        );

        Flux<InventoryResponseDTO> inventoryFlux = Flux.just(inventory1, inventory2);
        final String body = objectMapper.writeValueAsString(inventoryFlux.collectList().block());

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(body));

        Flux<InventoryResponseDTO> result = inventoryServiceClient.searchInventory(
                Optional.of(0),
                Optional.of(2),
                null,
                null,
                null,
                null,
                null
        );

        StepVerifier.create(result)
                .expectNextMatches(inv -> {
                    assertNotNull(inv.getRecentUpdateMessage());
                    return inv.getRecentUpdateMessage().contains("supplies updated");
                })
                .expectNextMatches(inv -> {
                    assertNotNull(inv.getRecentUpdateMessage());
                    return inv.getRecentUpdateMessage().equals("No recent updates.");
                })
                .verifyComplete();
    }

    @Test
    void getLowStockProducts_shouldReturnFlux_onSuccess() throws Exception {
        // arrange
        ProductResponseDTO p1 = new ProductResponseDTO(
                "p1",
                "inv1",
                "A",
                "d",
                10.0,
                1,
                12.0,
                Status.OUT_OF_STOCK,
                LocalDateTime.now()
        );
        ProductResponseDTO p2 = new ProductResponseDTO(
                "p2",
                "inv1",
                "B",
                "d",
                20.0,
                2,
                22.0,
                Status.AVAILABLE,
                LocalDateTime.now()
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(List.of(p1, p2))));

        // act
        Flux<ProductResponseDTO> result = inventoryServiceClient.getLowStockProducts("inv1", 5);

        // assert
        StepVerifier.create(result)
                .expectNext(p1)
                .expectNext(p2)
                .verifyComplete();
    }

    @Test
    void getInventoryById_shouldMap404_toInventoryNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Inventory not found\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.getInventoryById("invX"))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

    @Test
    void getLowStockProducts_shouldMap404_toNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"No products below threshold\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.getLowStockProducts("invX", 5))
                .expectError(org.webjars.NotFoundException.class)
                .verify();
    }

    @Test
    void addInventory_shouldReturnInventory_onSuccess() throws Exception {
        // arrange
        InventoryResponseDTO inv = new InventoryResponseDTO(
                "inv1",
                "INV-1",
                "Medication",
                "Type",
                "desc",
                "",
                "",
                diagnosticKitImage,
                false,
                List.of(),
                "No recent updates."
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(inv)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.addInventory(new InventoryRequestDTO()))
                .expectNext(inv)
                .verifyComplete();
    }

    @Test
    void updateInventory_shouldReturnInventory_onSuccess() throws Exception {
        // arrange
        InventoryResponseDTO inv = new InventoryResponseDTO(
                "inv1",
                "INV-1",
                "Medication",
                "Type",
                "updated",
                "",
                "",
                diagnosticKitImage,
                false,
                List.of(),
                "No recent updates."
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(inv)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.updateInventory(new InventoryRequestDTO(), "inv1"))
                .expectNext(inv)
                .verifyComplete();
    }

    @Test
    void updateInventory_shouldMap400_toInvalidInputsInventoryException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"bad update\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.updateInventory(new InventoryRequestDTO(), "inv1"))
                .expectErrorMatches(ex -> ex instanceof InvalidInputsInventoryException
                        && ex.getMessage().contains("bad update"))
                .verify();
    }

    @Test
    void updateProductInInventory_shouldReturnProduct_onSuccess() throws Exception {
        // arrange
        ProductResponseDTO p = new ProductResponseDTO(
                "p1",
                "inv1",
                "name",
                "d",
                10.0,
                5,
                15.0,
                Status.AVAILABLE,
                LocalDateTime.now()
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(p)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.updateProductInInventory(new ProductRequestDTO(), "inv1", "p1"))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    void updateProductInInventory_shouldMap400_toInvalidInputsInventoryException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"invalid product update\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.updateProductInInventory(new ProductRequestDTO(), "inv1", "p1"))
                .expectErrorMatches(ex -> ex instanceof InvalidInputsInventoryException
                        && ex.getMessage().contains("invalid product update"))
                .verify();
    }

    @Test
    void addSupplyToInventory_shouldReturnProduct_onSuccess() throws Exception {
        // arrange
        ProductResponseDTO p = new ProductResponseDTO(
                "p1",
                "inv1",
                "Supply",
                "d",
                10.0,
                1,
                11.0,
                Status.AVAILABLE,
                LocalDateTime.now()
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(p)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(new ProductRequestDTO(), "inv1"))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    // arrange
    void addSupplyToInventory_shouldMap400_toInvalidInputsInventoryException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"invalid supply\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(new ProductRequestDTO(), "inv1"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void addInventory_shouldMap400_toInvalidInputsInventoryException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"invalid payload\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.addInventory(new InventoryRequestDTO()))
                .expectErrorMatches(ex -> ex instanceof InvalidInputsInventoryException
                        && ex.getMessage().contains("invalid payload"))
                .verify();
    }

    @Test
    void getProductByProductIdInInventory_shouldReturn_onSuccess() throws Exception {
        // arrange
        ProductResponseDTO p = new ProductResponseDTO(
                "p1",
                "inv1",
                "N",
                "d",
                10.0,
                1,
                12.0,
                Status.AVAILABLE,
                LocalDateTime.now()
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(p)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.getProductByProductIdInInventory("inv1","p1"))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    void getProductByProductIdInInventory_shouldMap404_toProductListNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"not found\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.getProductByProductIdInInventory("inv1","missing"))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void deleteProductInInventory_shouldComplete_onSuccess() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteProductInInventory("inv1","p1"))
                .verifyComplete();
    }

    @Test
    void deleteProductInInventory_shouldMap404_toProductListNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"missing\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteProductInInventory("inv1","pX"))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void deleteAllProductsInInventory_shouldComplete_onSuccess() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteAllProductsInInventory("inv1"))
                .verifyComplete();
    }

    @Test
    void deleteAllProductsInInventory_shouldMap404_toProductListNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"inventory missing\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteAllProductsInInventory("invMissing"))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void deleteInventoryByInventoryId_shouldComplete_onSuccess() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteInventoryByInventoryId("inv1"))
                .verifyComplete();
    }

    @Test
    void deleteInventoryByInventoryId_shouldMap404_toNotFoundException() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"no such inventory\"}"));

        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteInventoryByInventoryId("invX"))
                .expectError(org.webjars.NotFoundException.class)
                .verify();
    }

    @Test
    void updateImportantStatus_shouldComplete_onSuccess() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        // act & assert
        StepVerifier.create(inventoryServiceClient.updateImportantStatus("inv1", true))
                .verifyComplete();
    }

    @Test
    void deleteAllInventories_shouldComplete_onSuccess() {
        // arrange
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        // act & assert
        StepVerifier.create(inventoryServiceClient.deleteAllInventories())
                .verifyComplete();
    }

    @Test
    void getProductsByInventoryName_shouldReturnProducts() throws Exception {
        // arrange
        ProductResponseDTO p = new ProductResponseDTO(
                "p1",
                "inv1",
                "Alpha",
                "d",
                10.0,
                1,
                12.0,
                Status.AVAILABLE,
                LocalDateTime.now()
        );
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(List.of(p))));

        // act & assert
        StepVerifier.create(inventoryServiceClient.getProductsByInventoryName("Medication"))
                .expectNext(p)
                .verifyComplete();
    }

    @Test
    void createSupplyPdf_shouldReturnBytes() {
        // arrange
        byte[] pdf = new byte[] {37, 80, 68, 70}; // "%PDF"
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .setBody(new okio.Buffer().write(pdf)));

        // act & assert
        StepVerifier.create(inventoryServiceClient.createSupplyPdf("inv1"))
                .expectNextMatches(bytes -> bytes.length == 4 && bytes[0] == 37)
                .verifyComplete();
    }

    @Test
    void getInventoryById_404_ShouldMapToInventoryNotFoundException() {
        enqueueError(404, "Inventory not found");
        StepVerifier.create(inventoryServiceClient.getInventoryById("inv-404"))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

    @Test
    void getInventoryById_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Bad inventory id");
        StepVerifier.create(inventoryServiceClient.getInventoryById("bad"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }



    @Test
    void getProductInInventory_404_ShouldMapToProductNotFound() {
        enqueueError(404, "Product not found in inventory");
        StepVerifier.create(inventoryServiceClient.getProductByProductIdInInventory("inv-1", "prod-x"))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void getProductInInventory_400_ShouldMapToInvalidInputs() {
        enqueueError(400, "Invalid product id");
        StepVerifier.create(inventoryServiceClient.getProductByProductIdInInventory("inv-1", "bad"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }


    @Test
    void addInventory_422_ShouldMapToInvalidInputsInventoryException() throws JsonProcessingException {
        enqueueError(422, "Inventory name already exists.");
        InventoryRequestDTO req = InventoryRequestDTO.builder()
                .inventoryName("internal")
                .inventoryType("Internal")
                .inventoryDescription("desc")
                .build();

        StepVerifier.create(inventoryServiceClient.addInventory(req))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void addInventory_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Missing required field");
        InventoryRequestDTO req = InventoryRequestDTO.builder()
                .inventoryName("")   // bad
                .inventoryType("Internal")
                .build();

        StepVerifier.create(inventoryServiceClient.addInventory(req))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void updateInventory_404_ShouldMapToInventoryNotFoundException() {
        enqueueError(404, "Inventory not found");
        InventoryRequestDTO req = InventoryRequestDTO.builder()
                .inventoryName("name")
                .inventoryType("Internal")
                .build();

        StepVerifier.create(inventoryServiceClient.updateInventory(req, "missing-id"))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

    @Test
    void updateInventory_422_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(422, "Inventory name already exists.");
        InventoryRequestDTO req = InventoryRequestDTO.builder()
                .inventoryName("dup")
                .inventoryType("Internal")
                .build();

        StepVerifier.create(inventoryServiceClient.updateInventory(req, "inv-1"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }



    @Test
    void addSupply_422_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(422, "A product with the name already exists in this inventory.");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("Benzodiazepines")
                .productDescription("Sedative")
                .productPrice(100.0)
                .productQuantity(10)
                .productSalePrice(15.99)
                .build();

        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(req, "inv-1"))
                .expectError(InventoryProductUnprocessableEntityException.class)
                .verify();
    }

    @Test
    void addSupply_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Invalid product body");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("") // bad
                .productPrice(0.0)
                .build();

        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(req, "inv-1"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }


    @Test
    void getProductsByFilters_404_ShouldMapToProductListNotFoundException() {
        enqueueError(404, "No products match filters");
        StepVerifier.create(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(
                        "inv-1", "needle", null, null, null))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void getProductsByFiltersPaginated_404_ShouldMapToProductListNotFoundException() {
        enqueueError(404, "No products");
        StepVerifier.create(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination(
                        "inv-1", null, null, null, Optional.of(0), Optional.of(10)))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }



    @Test
    void getTotalProductsCount_404_ShouldMapToProductListNotFoundException() {
        enqueueError(404, "Inventory not found or empty");
        StepVerifier.create(inventoryServiceClient.getTotalNumberOfProductsWithRequestParams(
                        "inv-1", null, null, null))
                .expectError(ProductListNotFoundException.class)
                .verify();
    }

    @Test
    void getQuantityOfProducts_404_ShouldMapToInventoryNotFoundException() {
        enqueueError(404, "Inventory not found");
        StepVerifier.create(inventoryServiceClient.getQuantityOfProductsInInventory("missing"))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }


    @Test
    void addInventoryType_422_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(422, "Type already exists");
        InventoryTypeRequestDTO dto = new InventoryTypeRequestDTO("Internal");
        StepVerifier.create(inventoryServiceClient.addInventoryType(dto))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void getAllInventoryTypes_404_ShouldMapToNotFoundException() {
        enqueueError(404, "No inventory types");
        StepVerifier.create(inventoryServiceClient.getAllInventoryTypes())
                .expectError(NotFoundException.class)
                .verify();
    }


    @Test
    void getAllInventories_404_ShouldMapToNotFoundException() {
        enqueueError(404, "No inventories exist");
        StepVerifier.create(inventoryServiceClient.getAllInventories())
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void createSupplyPdf_422_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(422, "Invalid inventory ID provided.");
        StepVerifier.create(inventoryServiceClient.createSupplyPdf("bad-id"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }


    @Test
    void addProduct_422_ShouldMapToInventoryProductUnprocessableEntityException() {
        enqueueError(422, "Product already exists in this inventory.");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("Aspirin")
                .productDescription("Painkiller")
                .productPrice(10.0)
                .productQuantity(5)
                .productSalePrice(12.0)
                .build();

        StepVerifier.create(inventoryServiceClient.addProductToInventory(req, "inv-1"))
                .expectError(InventoryProductUnprocessableEntityException.class)
                .verify();
    }

    @Test
    void addProduct_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Bad product request");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("") // invalid
                .productPrice(0.0)
                .build();

        StepVerifier.create(inventoryServiceClient.addProductToInventory(req, "inv-1"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void updateProductInInventory_422_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(422, "Duplicate product name.");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("Dup")
                .productDescription("desc")
                .productPrice(1.0)
                .productQuantity(1)
                .productSalePrice(1.5)
                .build();

        StepVerifier.create(inventoryServiceClient.updateProductInInventory(req, "inv-1", "prod-1"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void updateProductInInventory_404_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(404, "Product not found in inventory.");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("X")
                .productDescription("Y")
                .productPrice(2.0)
                .productQuantity(2)
                .productSalePrice(2.5)
                .build();

        StepVerifier.create(inventoryServiceClient.updateProductInInventory(req, "inv-1", "missing-prod"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void deleteInventoryById_404_ShouldMapToNotFoundException() {
        enqueueError(404, "Inventory not found");
        StepVerifier.create(inventoryServiceClient.deleteInventoryByInventoryId("inv-missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void deleteInventoryById_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Bad inventory id");
        StepVerifier.create(inventoryServiceClient.deleteInventoryByInventoryId("bad"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void addSupply_404_ShouldMapToNotFoundException() {
        enqueueError(404, "Inventory not found");
        ProductRequestDTO req = ProductRequestDTO.builder()
                .productName("NewProd")
                .productDescription("desc")
                .productPrice(3.0)
                .productQuantity(3)
                .productSalePrice(3.5)
                .build();

        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(req, "inv-missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void createSupplyPdf_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Bad inventory id");
        StepVerifier.create(inventoryServiceClient.createSupplyPdf("bad-id"))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void updateProductInventoryId_404_ShouldMapToNotFoundException() {
        enqueueError(404, "Product not found in inventory");
        StepVerifier.create(inventoryServiceClient.updateProductInventoryId("inv-1", "prod-1", "inv-2"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getAllInventories_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Bad request");
        StepVerifier.create(inventoryServiceClient.getAllInventories())
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }

    @Test
    void addInventoryType_400_ShouldMapToInvalidInputsInventoryException() {
        enqueueError(400, "Invalid type payload");
        InventoryTypeRequestDTO dto = new InventoryTypeRequestDTO("");
        StepVerifier.create(inventoryServiceClient.addInventoryType(dto))
                .expectError(InvalidInputsInventoryException.class)
                .verify();
    }




}