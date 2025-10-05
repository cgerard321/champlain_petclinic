package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Header.header;


public class MockServerConfigProductsService {

    private static final Integer PRODUCTS_SERVICE_SERVER_PORT = 7007;

    private final ClientAndServer clientAndServer;
    private final MockServerClient mockServerClient_ProductsService = new MockServerClient("localhost", PRODUCTS_SERVICE_SERVER_PORT);

    public MockServerConfigProductsService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(PRODUCTS_SERVICE_SERVER_PORT);
    }

    public void registerGetAllProductsEndpoint() {

        String responseBody =
                "data: {\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a\",\"productName\":\"Bird Cage\",\"productDescription\":\"Spacious cage for small birds like parakeets\",\"productSalePrice\":29.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"a6a27433-e7a9-4e78-8ae3-0cb57d756863\",\"productName\":\"Horse Saddle\",\"productDescription\":\"Lightweight saddle for riding horses\",\"productSalePrice\":199.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"06a7d573-bcab-4db3-956f-773324b92a80\",\"productName\":\"Dog Food\",\"productDescription\":\"Premium dry food for adult dogs\",\"productSalePrice\":45.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"1501f30e-1db1-44b2-a555-bca6f64450e4\",\"productName\":\"Fish Tank Heater\",\"productDescription\":\"Submersible heater for tropical fish tanks\",\"productSalePrice\":14.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"4d508fb7-f1f2-4952-829d-10dd7254cf26\",\"productName\":\"Aquarium Filter\",\"productDescription\":\"Filter system for small to medium-sized aquariums\",\"productSalePrice\":19.99,\"averageRating\":0.0}\n\n" +
                "data: {\"productId\":\"98f7b33a-d62a-420a-a84a-05a27c85fc91\",\"productName\":\"Cat Litter\",\"productDescription\":\"Clumping cat litter with odor control\",\"productSalePrice\":12.99,\"averageRating\":0.0}\n\n";

        mockServerClient_ProductsService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/products")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(header("Content-Type", "text/event-stream;charset=UTF-8"))
                                .withBody(responseBody)
                );
    }

    public void stopMockServer() {
        if (clientAndServer != null)
            this.clientAndServer.stop();
    }
}
