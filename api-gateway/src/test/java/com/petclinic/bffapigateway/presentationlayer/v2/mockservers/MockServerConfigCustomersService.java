package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
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

    public void registerUpdateCustomerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/customers/" + "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                                .withBody(json("{\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    //TODO Change name
    public void registerAddCustomerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/customers")
                                .withBody(json("{\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    public void registerGetAllCustomersEndpoint() {
        String responseBody = "["
                + "{\"customerId\":\"customer1\",\"firstName\":\"John\",\"lastName\":\"Does\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"province\":\"Chicago\",\"telephone\":\"1234567890\"},"
                + "{\"customerId\":\"customer2\",\"firstName\":\"Jane\",\"lastName\":\"Doew\",\"address\":\"456 Maple St\",\"city\":\"Shelbyville\",\"province\":\"Illinois\",\"telephone\":\"0987654321\"},"
                + "{\"customerId\":\"customer3\",\"firstName\":\"Jim\",\"lastName\":\"Doee\",\"address\":\"789 Oak St\",\"city\":\"Capital City\",\"province\":\"Longueuil\",\"telephone\":\"1122334455\"}"
                + "]";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/customers")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                );
    }

    public void registerDeleteCustomerEndpoint() {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        customerResponseDTO.setFirstName("Betty");
        customerResponseDTO.setLastName("Davis");
        customerResponseDTO.setAddress("638 Cardinal Ave.");
        customerResponseDTO.setCity("Sun Prairie");
        customerResponseDTO.setProvince("Quebec");
        customerResponseDTO.setTelephone("6085551749");

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/customers/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)  // Change to 200 OK since we are returning a response body
                                .withBody(json(customerResponseDTO))  // Return the CustomerResponseDTO as JSON
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerDeleteCustomerEmptyResponseEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/customers/12345678-1234-1234-1234-123456789012")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("")
                );
    }

    public void registerGetCustomerByIdEndpoint() {
        String customerResponseJson = "{"
                + "\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\","
                + "\"firstName\":\"Betty\","
                + "\"lastName\":\"Davis\","
                + "\"address\":\"638 Cardinal Ave.\","
                + "\"city\":\"Sun Prairie\","
                + "\"province\":\"Quebec\","
                + "\"telephone\":\"6085551749\","
                + "\"pets\":[]"
                + "}";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/customers/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(customerResponseJson))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerUpdatePetEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/api/v2/gateway/pets/123")
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
                                .withPath("/pets/53163352-8398-4513-bdff-b7715c056d1d/v2")
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
                                .withPath("/pets/53163352-8398-4513-bdff-b7715c056d1d")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"petId\":\"53163352-8398-4513-bdff-b7715c056d1d\",\"name\":\"Buddy\",\"birthDate\":\"1999-11-01T00:00:00.000+00:00\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"1.3\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\"}"))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }

    public void registerUpdatePetEndpoint(String petId, String customerId, String newName) {
        String responseBody = String.format("{\"petId\":\"%s\",\"name\":\"%s\",\"birthDate\":\"2025-10-23T00:00:00.000+00:00\",\"petTypeId\":\"1\",\"isActive\":\"true\",\"weight\":\"5.0\",\"customerId\":\"%s\"}",
                petId, newName, customerId);

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/pet/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                                .withHeader("Content-Type", "application/json")
                );
    }
}
