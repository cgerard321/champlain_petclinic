package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigVetService {

    private static final Integer VET_SERVICE_SERVER_PORT = 7002;

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient_VetService = new MockServerClient("localhost", VET_SERVICE_SERVER_PORT);

    public MockServerConfigVetService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(VET_SERVICE_SERVER_PORT);
    }

    public void registerAddVetEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vet")
                                .withBody(json("{"
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "],"
                                        + "\"photoDefault\":false"
                                        + "}"))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{"
                                        + "\"vetId\":\"2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "]"
                                        + "}"))
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
