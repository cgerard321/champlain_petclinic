package com.petclinic.cartsservice;

import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerConfigProductService {
    private final ClientAndServer clientAndServer;

    private static final Integer MOCK_SERVER_PORT = 8080;

    public static final String NON_EXISTING_PRODUCT_ID = "06a7d573-bcab-4db3-956f-773324b92a89";

    private final MockServerClient mockServerClient = new MockServerClient("localhost", MOCK_SERVER_PORT);

    public MockServerConfigProductService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    public void registerGetProduct1ByProductIdEndpoint() {
        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a88")
                .productName("Dog Food")
                .productDescription("Dog Food")
                .productSalePrice(10.0)
                .build();

        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/products/" + "06a7d573-bcab-4db3-956f-773324b92a88")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"productId\":\"06a7d573-bcab-4db3-956f-773324b92a88\",\"productName\":" +
                                        "\"Dog Food\",\"productDescription\":\"Dog Food\",\"productSalePrice\":10.0}"));
    }

    public void registerGetProduct_NonExisting_ByProductIdEndpoint() {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/products/" + NON_EXISTING_PRODUCT_ID)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

    public void stopServer(){
        if (this.clientAndServer != null) {
            this.clientAndServer.stop();
        }
    }
}
