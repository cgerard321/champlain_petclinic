package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
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
                                .withPath("/vets")
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
    public void registerGetVetByFirstNameEndpoint(String firstName, VetResponseDTO responseDTO) throws JsonProcessingException {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/firstName/" + firstName)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(new ObjectMapper().writeValueAsString(responseDTO)))
                );
    }

    public void registerGetVetByFirstNameEndpointNotFound(String firstName) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/firstName/" + firstName)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

    public void registerGetVetByLastNameEndpoint(String lastName, VetResponseDTO responseDTO) throws JsonProcessingException {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/lastName/" + lastName)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(new ObjectMapper().writeValueAsString(responseDTO)))
                );
    }

    public void registerGetVetByLastNameEndpointNotFound(String lastName) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/lastName/" + lastName)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

}
