package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import okhttp3.mockwebserver.MockResponse;

import java.io.IOException;
import java.util.function.Consumer;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceClientIntegrationTest {

    private MockWebServer mockWebServer;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private ProductsServiceClient productsServiceClient;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        productsServiceClient = new ProductsServiceClient(webClientBuilder,
                "localhost", String.valueOf(mockWebServer.getPort()));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.mockWebServer.enqueue(response);
    }

    @Test
    void getAllProducts_ShouldReturnProductListStream() {

        prepareResponse(response -> { response
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream");
        });

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductName().equals("Rabbit Hutch"))
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductName().equals("Flea Collar"))
                .verifyComplete();
    }
    @Test
    void getAllProducts_ShouldHandleEmptyResponse() {

        prepareResponse(response -> response
                .setBody("")
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null);

        StepVerifier.create(productsFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    void getAllProducts_ShouldThrowServerError() {

        prepareResponse(response -> response
                .setResponseCode(500)
                .setBody("Internal Server Error")
        );

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null);

        StepVerifier.create(productsFlux)
                .expectErrorMatches(error -> error.getMessage().contains("500 Internal Server Error"))
                .verify();
    }

}
