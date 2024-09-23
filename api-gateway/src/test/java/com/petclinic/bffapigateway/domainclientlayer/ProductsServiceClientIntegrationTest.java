package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Products.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class ProductsServiceClientIntegrationTest {

    @MockBean
    private ProductsServiceClient productsServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        productsServiceClient = new ProductsServiceClient(webClientBuilder, "localhost",
                String.valueOf(mockWebServer.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void whenAddProduct_thenReturnProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "Product 1",
                "desc",
                10.00,
                0.00
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .createProduct(new ProductRequestDTO());

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenUpdateProduct_thenReturnUpdatedProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "Product 1",
                "desc",
                10.00,
                0.00
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .updateProduct(productResponseDTO.getProductId(), new ProductRequestDTO());

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenDeleteProduct_thenDeleteProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "Product 1",
                "desc",
                10.00,
                0.00
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .deleteProduct("productId");

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

}