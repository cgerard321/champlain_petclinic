package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigCustomersService {

    private static final Integer CUSTOMERS_SERVICE_SERVER_PORT = 7003;

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient_CustomersService = new MockServerClient("localhost", CUSTOMERS_SERVICE_SERVER_PORT);

    public MockServerConfigCustomersService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(CUSTOMERS_SERVICE_SERVER_PORT);
    }

    public void registerUpdateOwnerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/owners/" + "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    public void registerAddOwnerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/owners")
                                .withBody(json("{\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    public void registerGetAllOwnersEndpoint() {
        String responseBody = "["
                + "{\"ownerId\":\"owner1\",\"firstName\":\"John\",\"lastName\":\"Does\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"province\":\"Chicago\",\"telephone\":\"1234567890\"},"
                + "{\"ownerId\":\"owner2\",\"firstName\":\"Jane\",\"lastName\":\"Doew\",\"address\":\"456 Maple St\",\"city\":\"Shelbyville\",\"province\":\"Illinois\",\"telephone\":\"0987654321\"},"
                + "{\"ownerId\":\"owner3\",\"firstName\":\"Jim\",\"lastName\":\"Doee\",\"address\":\"789 Oak St\",\"city\":\"Capital City\",\"province\":\"Longueuil\",\"telephone\":\"1122334455\"}"
                + "]";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
