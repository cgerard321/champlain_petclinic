package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.Inventory.Status;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.exceptions.ProductListNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceClientIntegrationTest {

    @MockBean
    private InventoryServiceClient inventoryServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize(){
        inventoryServiceClient = new InventoryServiceClient("localhost", String.valueOf(mockWebServer.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException{
        mockWebServer.shutdown();
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
                Status.OUT_OF_STOCK
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
                Status.OUT_OF_STOCK
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
                Status.OUT_OF_STOCK
        );
        ProductResponseDTO productResponseDTO1 = new ProductResponseDTO(
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK
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
    void searchProducts_withValidSearchQuery_shouldReturnProducts() throws JsonProcessingException {
        // Arrange
        ProductResponseDTO product1 = new ProductResponseDTO(
                "productId1",
                "inventoryId",
                "name1",
                "desc1",
                10.00,
                2,
                15.99,
                Status.OUT_OF_STOCK
        );
        ProductResponseDTO product2 = new ProductResponseDTO(
                "productId2",
                "inventoryId",
                "name2",
                "desc2",
                20.00,
                4,
                25.99,
                Status.AVAILABLE
        );
        ArrayList<ProductResponseDTO> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(products)));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts("inventoryId", "name1", "desc1");

        // Assert
        StepVerifier.create(result)
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();
    }

    @Test
    void searchProducts_withEmptySearchQuery_shouldReturnEmptyList() throws JsonProcessingException {
        // Arrange
        ArrayList<ProductResponseDTO> products = new ArrayList<>();

        // Mock the response from the MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(products)));

        // Act
        Flux<ProductResponseDTO> result = inventoryServiceClient.searchProducts("inventoryId", "", "");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
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
                Status.OUT_OF_STOCK
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


}