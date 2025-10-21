package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
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
                                .withBody(json("{\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
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

    public void registerDeleteOwnerEndpoint() {
        OwnerResponseDTO ownerResponse = new OwnerResponseDTO();
        ownerResponse.setOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        ownerResponse.setFirstName("Betty");
        ownerResponse.setLastName("Davis");
        ownerResponse.setAddress("638 Cardinal Ave.");
        ownerResponse.setCity("Sun Prairie");
        ownerResponse.setProvince("Quebec");
        ownerResponse.setTelephone("6085551749");

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/owners/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)  // Change to 200 OK since we are returning a response body
                                .withBody(json(ownerResponse))  // Return the OwnerResponseDTO as JSON
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetOwnerByIdEndpoint() {
        String ownerResponseJson = "{"
                + "\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\","
                + "\"firstName\":\"Betty\","
                + "\"lastName\":\"Davis\","
                + "\"address\":\"638 Cardinal Ave.\","
                + "\"city\":\"Sun Prairie\","
                + "\"province\":\"Quebec\","
                + "\"telephone\":\"6085551749\","
                + "\"pets\":null"
                + "}";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(ownerResponseJson))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerUpdatePetEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/api/v2/gateway/pet/123")
                                .withBody(json("{\"petId\":\"123\",\"name\":\"Buddy\",\"birthDate\":\"2020-01-01\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"10\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"petId\":\"123\",\"name\":\"Buddy\",\"birthDate\":\"2020-01-01\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"10\"}"))
                );
    }

    public void registerDeletePetEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/pet/53163352-8398-4513-bdff-b7715c056d1d/v2")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"petId\":\"53163352-8398-4513-bdff-b7715c056d1d\",\"name\":\"Buddy\",\"birthDate\":\"1999-11-01T00:00:00.000+00:00\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"1.3\"}"))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetPetByIdEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pet/53163352-8398-4513-bdff-b7715c056d1d")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"petId\":\"53163352-8398-4513-bdff-b7715c056d1d\",\"name\":\"Buddy\",\"birthDate\":\"1999-11-01T00:00:00.000+00:00\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"1.3\",\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\"}"))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
