package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigVisitService {
    private static final Integer VISIT_SERVICE_PORT = 7001;
    private final ClientAndServer clientAndServer;
    private final MockServerClient mockServerClient_VisitService = new MockServerClient("localhost", VISIT_SERVICE_PORT);


    public MockServerConfigVisitService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(VISIT_SERVICE_PORT);
    }

    public void registerGetAllVisitsEndpoint() {
        mockServerClient_VisitService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/visits")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("[{\"visitId\":\"visit1\"},{\"visitId\":\"visit2\"},{\"visitId\":\"visit3\"}]")));
    }


    public void registerDeleteCompletedVisitsByIdEndpoint() {
        String visitId = "visitId1";
        mockServerClient_VisitService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/visits/completed/" + visitId)
                )
                .respond(
                        response()
                                .withStatusCode(204));
    }

    public void registerDeleteCompletedVisit_ByInvalidIdEndpoint() {
        String visitId = "InvalidId";
        mockServerClient_VisitService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/visits/completed/" + visitId)
                )
                .respond(
                        response()
                                .withStatusCode(404));
    }


    public void stopServer() {
        if (clientAndServer != null) {
            this.clientAndServer.stop();
        }
    }

}
