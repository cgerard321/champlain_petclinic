package com.petclinic.cartsservice.domainclientlayer;

import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ProductClientTest {

    private ClientAndServer mockServer;
    private ProductClient productClient;

    private static final String EXISTING_PRODUCT_ID = "06a7d573-bcab-4db3-956f-773324b92a88";
    private static final String NON_EXISTING_PRODUCT_ID = "06a7d573-bcab-4db3-956f-773324b92a89";
    private static final String INVALID_PRODUCT_ID = "invalid-product-id";

    private static final String EXISTING_IMAGE_ID = "12345-image-id";

    @BeforeEach
    public void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer();
        int port = mockServer.getLocalPort();

        productClient = new ProductClient("localhost", String.valueOf(port));
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    public void getProductByProductId_Success() {
        // Mocking a successful product retrieval including imageId
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/products/" + EXISTING_PRODUCT_ID))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"productId\":\"06a7d573-bcab-4db3-956f-773324b92a88\",\"imageId\":\"12345-image-id\"," +
                                "\"productName\":\"Dog Food\",\"productDescription\":\"Dog Food\"," +
                                "\"productSalePrice\":10.0,\"productQuantity\":12}"));

        // Test the successful case
        Mono<ProductResponseModel> product = productClient.getProductByProductId(EXISTING_PRODUCT_ID);

        StepVerifier.create(product)
                .expectNextMatches(response -> response.getProductId().equals(EXISTING_PRODUCT_ID)
                        && response.getImageId().equals(EXISTING_IMAGE_ID)
                        && response.getProductName().equals("Dog Food")
                        && Integer.valueOf(12).equals(response.getProductQuantity()))
                .verifyComplete();
    }



    @Test
    public void getProductByProductId_NotFound() {
        // Mocking a 404 Not Found response
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/products/" + NON_EXISTING_PRODUCT_ID))
                .respond(response()
                        .withStatusCode(404));

        // Test the 404 case
        Mono<ProductResponseModel> product = productClient.getProductByProductId(NON_EXISTING_PRODUCT_ID);

        StepVerifier.create(product)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("ProductId not found: " + NON_EXISTING_PRODUCT_ID))
                .verify();
    }

    @Test
    public void getProductByProductId_InvalidInput() {
        // Mocking a 422 Invalid Input response
        mockServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/products/" + INVALID_PRODUCT_ID))
                .respond(response()
                        .withStatusCode(422));

        // Test the 422 case
        Mono<ProductResponseModel> product = productClient.getProductByProductId(INVALID_PRODUCT_ID);

        StepVerifier.create(product)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException
                        && throwable.getMessage().equals("ProductId invalid: " + INVALID_PRODUCT_ID))
                .verify();
    }

    @Test
    public void testProductResponseModelConstructor() {
        // Arrange
        String productId = "06a7d573-bcab-4db3-956f-773324b92a88";
        String imageId = "12345-image-id";
        String productName = "Dog Food";
        String productDescription = "Dog Food Description";
        Double productSalePrice = 10.0;

        // Act
        ProductResponseModel product = new ProductResponseModel(productId, imageId, productName, productDescription, productSalePrice);

        // Assert
        assertEquals(productId, product.getProductId());
        assertEquals(imageId, product.getImageId());
        assertEquals(productName, product.getProductName());
        assertEquals(productDescription, product.getProductDescription());
        assertEquals(productSalePrice, product.getProductSalePrice());
    }

    @Test
    public void testProductResponseModelSettersAndGetters() {
        // Arrange
        ProductResponseModel product = new ProductResponseModel();
        String productId = "06a7d573-bcab-4db3-956f-773324b92a88";
        String imageId = "12345-image-id";
        String productName = "Dog Food";
        String productDescription = "Dog Food Description";
        Double productSalePrice = 10.0;

        // Act
        product.setProductId(productId);
        product.setImageId(imageId);
        product.setProductName(productName);
        product.setProductDescription(productDescription);
        product.setProductSalePrice(productSalePrice);

        // Assert
        assertEquals(productId, product.getProductId());
        assertEquals(imageId, product.getImageId());
        assertEquals(productName, product.getProductName());
        assertEquals(productDescription, product.getProductDescription());
        assertEquals(productSalePrice, product.getProductSalePrice());
    }
}