package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;


import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigEmailingService {

    private static final Integer EMAILING_SERVICE_SERVER_PORT = 11111;
    private final ClientAndServer clientAndServer;
    private final MockServerClient mockServerClient_EmailingService = new MockServerClient("localhost", EMAILING_SERVICE_SERVER_PORT);

    public MockServerConfigEmailingService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(EMAILING_SERVICE_SERVER_PORT);
    }

    // Mock for getAllEmails endpoint
    public void registerGetAllEmailsEndpoint() {
        String emailResponse = "[" +
                "{\"emailId\":\"1\", \"to\":\"user1@example.com\", \"subject\":\"Test 1\", \"body\":\"Hello 1\", \"status\":\"SENT\"}," +
                "{\"emailId\":\"2\", \"to\":\"user2@example.com\", \"subject\":\"Test 2\", \"body\":\"Hello 2\", \"status\":\"SENT\"}" +
                "]";

        mockServerClient_EmailingService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/emails")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(emailResponse))
                );
    }
    public void registerGetAllReceivedEmailsEndpoint() {
        String receivedEmailResponse = "[" +
                "{" +
                "\"from\": \"\\\"Xilef992\\\" <xilef992@gmail.com>\"," +
                "\"subject\": \"yeah ok\"," +
                "\"dateReceived\": \"2024-10-20T21:52:40.000+00:00\"," +
                "\"plainTextBody\": \"You are done!\\n\"" +
                "}" +
                "]";

        mockServerClient_EmailingService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/received/all")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(receivedEmailResponse))
                );
    }

    // Mock for addHtmlTemplate endpoint
    public void registerAddHtmlTemplateEndpoint() {
        mockServerClient_EmailingService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/templates/.*")
                                .withContentType(org.mockserver.model.MediaType.TEXT_HTML)
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{\"message\":\"Template added successfully.\"}"))
                );
    }

    // Mock for sendEmail endpoint
    public void registerSendEmailEndpoint() {
        mockServerClient_EmailingService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/send")
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"status\":\"OK\"}"))
                );
    }

    // Mock for sendEmailNotification endpoint
    public void registerSendEmailNotificationEndpoint() {
        mockServerClient_EmailingService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/send/notification")
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"status\":\"OK\"}"))
                );
    }


    // Stop the mock server
    public void stopMockServer() {
        if (clientAndServer != null) {
            this.clientAndServer.stop();
        }
    }
}