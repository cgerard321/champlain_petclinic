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

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}