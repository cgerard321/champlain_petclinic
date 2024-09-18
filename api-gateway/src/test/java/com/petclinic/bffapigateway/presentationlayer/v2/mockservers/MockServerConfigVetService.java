package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.petclinic.bffapigateway.dtos.Vets.SpecialtyDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.Workday;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.util.Set;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigVetService {

    private static final Integer VET_SERVICE_SERVER_PORT = 7002;
    private final ClientAndServer clientAndServer;
    private final MockServerClient mockServerClient_VetService = new MockServerClient("localhost", VET_SERVICE_SERVER_PORT);

    public MockServerConfigVetService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(VET_SERVICE_SERVER_PORT);

        mockServerClient_VetService
                .when(request()
                        .withMethod("GET")
                        .withPath("/vets/ac9adeb8-625b-11ee-8c99-0242ac120002"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody(json(new VetResponseDTO(
                                "ac9adeb8-625b-11ee-8c99-0242ac120002",
                                "5",
                                "Henry",
                                "Stevens",
                                "stevenshenry@email.com",
                                "(514)-634-8276 #2389",
                                "Practicing since 1 years",
                                Set.of(Workday.Wednesday, Workday.Tuesday, Workday.Thursday, Workday.Monday),
                                "{\"Thursday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\"],\"Monday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\"Wednesday\":[\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"],\"Tuesday\":[\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"]}",
                                false,
                                Set.of(new SpecialtyDTO("surgery", "surgery"), new SpecialtyDTO("radiology", "radiology"))
                        ))));

        mockServerClient_VetService
                .when(request()
                        .withMethod("GET")
                        .withPath("/vets/ac9adeb8-625b-11ee-8c99-0242ac12000200000"))
                .respond(response()
                        .withStatusCode(404)
                        .withBody("{\"statusCode\":404,\"message\":\"vetId not found: invalid-id\",\"timestamp\":\"" + java.time.Instant.now() + "\"}"));
    }

    public void stopMockServer() {
        if (clientAndServer != null) {
            this.clientAndServer.stop();
        }
    }
}