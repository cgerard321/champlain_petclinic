package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Products.DeliveryType;
import com.petclinic.bffapigateway.dtos.Products.DeliveryType;
import com.petclinic.bffapigateway.dtos.Products.ProductRequestDTO;

import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductStatus;
import com.petclinic.bffapigateway.dtos.Products.ProductType;
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


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null,null,null,null,null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductName().equals("Rabbit Hutch"))
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductName().equals("Flea Collar"))
                .verifyComplete();
    }
    @Test
    void getAllProducts_WithRatingFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":4.5}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":3.0}\n\n" +
                        "data:{\"productId\":\"1234567\",\"productName\":\"Cheap Collar\",\"productDescription\":\"Cheap flea and tick prevention\",\"productSalePrice\":4.99,\"averageRating\":1.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );

        Double minRating = 3.0;
        Double maxRating = 5.0;


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, minRating, maxRating, null,null,null);

        // Verify the results
        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") &&
                        product.getAverageRating() >= minRating && product.getAverageRating() <= maxRating)
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") &&
                        product.getAverageRating() >= minRating && product.getAverageRating() <= maxRating)
                .expectComplete()
                .verify();
    }


    @Test
    void getAllProducts_ThenReturnEmptyResponse() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null,null,null,null,null,null);

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
        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(minPrice, maxPrice,null,null,null,null, null);


            // Verify the results using StepVerifier
        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .verifyComplete();
    }
    @Test
    void getAllProducts_WithDeliveryTypeFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"productId\":\"1\",\"productName\":\"Product A\",\"productDescription\":\"Description A\",\"productSalePrice\":29.99,\"averageRating\":4.0,\"deliveryType\":\"DELIVERY\"}\n\n" +
                                "data:{\"productId\":\"2\",\"productName\":\"Product B\",\"productDescription\":\"Description B\",\"productSalePrice\":49.99,\"averageRating\":4.5,\"deliveryType\":\"DELIVERY\"}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );

        String deliveryType = "DELIVERY";


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, null, null, null, deliveryType,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getDeliveryType() == DeliveryType.DELIVERY)
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getDeliveryType() == DeliveryType.DELIVERY)
                .verifyComplete();
    }

    @Test
    void getAllProducts_WithSortAscending_ThenReturnProductsInAscendingOrder() {

        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"productId\":\"1\",\"productName\":\"Alpha\",\"productDescription\":\"Description A\",\"productSalePrice\":19.99,\"averageRating\":4.0}\n\n" +
                                "data:{\"productId\":\"2\",\"productName\":\"Bravo\",\"productDescription\":\"Description B\",\"productSalePrice\":29.99,\"averageRating\":3.5}\n\n" +
                                "data:{\"productId\":\"3\",\"productName\":\"Charlie\",\"productDescription\":\"Description C\",\"productSalePrice\":39.99,\"averageRating\":4.5}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );
        String sort = "asc";

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, null, null, sort, null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getProductName().equals("Alpha"))
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getProductName().equals("Bravo"))
                .expectNextMatches(product -> product.getProductId().equals("3") && product.getProductName().equals("Charlie"))
                .verifyComplete();
    }



    @Test
    void whenAddProduct_thenReturnProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                false,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.DELIVERY
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
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                false,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.PICKUP
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
    void whenPatchListingStatus_thenReturnUpdatedProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                true,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.DELIVERY_AND_PICKUP
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus(productResponseDTO.getProductId(), new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenPatchListingStatusWithNonExistingProductId_thenThrowNotFoundException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Product not found for ProductId: 691e6945-0d4a-4b20-85cc-afd251faccfd")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("691e6945-0d4a-4b20-85cc-afd251faccfd", new ProductRequestDTO(
                        null, null, null, null, null,
                        null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Product not found for ProductId: 691e6945-0d4a-4b20-85cc-afd251faccfd"))
                .verify();
    }

    @Test
    void whenPatchListingStatusWithInvalidProductId_thenThrowInvalidInputException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setBody("Invalid input for ProductId: invalid-product-id")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("invalid-product-id", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Invalid input for ProductId: invalid-product-id"))
                .verify();
    }

    @Test
    void whenPatchListingStatus_andCauseServerFailure_thenThrowIllegalArgumentException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Invalid input for ProductId: productId")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("productId", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Something went wrong with the server"))
                .verify();
    }

    @Test
    void whenPatchListingStatus_andCauseClientFailure_thenThrowIllegalArgumentException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Invalid input for ProductId: productId")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("productId", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Client error"))
                .verify();
    }

    @Test
    void whenDeleteProduct_thenDeleteProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                false,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.DELIVERY_AND_PICKUP
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