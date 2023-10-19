package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
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
                "1",
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99

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
                "1",
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99
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
                "1",
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99
        );
        ProductResponseDTO productResponseDTO1 = new ProductResponseDTO(
                "1",
                "productId",
                "inventoryId",
                "name",
                "desc",
                10.00,
                2,
                15.99
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

}