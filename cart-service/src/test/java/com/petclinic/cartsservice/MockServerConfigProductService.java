package com.petclinic.cartsservice;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerConfigProductService {
    private final ClientAndServer clientAndServer;

    public static final String NON_EXISTING_PRODUCT_ID = "06a7d573-bcab-4db3-956f-773324b92a89";

    // You still need to connect to the MockServer running on localhost with a specific port
    private final MockServerClient mockServerClient;

    public MockServerConfigProductService(int i) {
        this.clientAndServer = ClientAndServer.startClientAndServer(0); // dynamic port
        this.mockServerClient = new MockServerClient("localhost", clientAndServer.getLocalPort());
    }

    public void registerGetProduct1ByProductIdEndpoint() {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/products/06a7d573-bcab-4db3-956f-773324b92a88")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"productId\":\"06a7d573-bcab-4db3-956f-773324b92a88\",\"productName\":\"Dog Food\"," +
                                        "\"productDescription\":\"Premium dry food for adult dogs\",\"productSalePrice\":10.0}")
                );
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

    public void stopServer() {
        if (this.clientAndServer != null) {
            this.clientAndServer.stop();
        }
    }

    public int getPort() {
        return clientAndServer.getLocalPort();
    }

}