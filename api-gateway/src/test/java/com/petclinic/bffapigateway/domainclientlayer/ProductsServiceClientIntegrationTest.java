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
import reactor.core.publisher.Flux;
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
    void getAllProducts_ThenReturnProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductName().equals("Rabbit Hutch"))
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductName().equals("Flea Collar"))
                .verifyComplete();
    }
    @Test
    void getAllProducts_ThenReturnEmptyResponse() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null);

        StepVerifier.create(productsFlux)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllProducts_WithPriceFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );

        // Define the minimum and maximum price for filtering
        Double minPrice = 5.00;
        Double maxPrice = 80.00;

        // Call the method with price filters
        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(minPrice, maxPrice);

        // Verify the results using StepVerifier
        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .verifyComplete();
    }

    @Test
    void whenAddProduct_thenReturnProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0
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
                0.00,
                0
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
                0.00,
                0
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