package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VisitsServiceClientIntegrationTest {

    private static final Integer PET_ID = 1;

    private VisitsServiceClient visitsServiceClient;

    private MockWebServer server;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();
        visitsServiceClient = new VisitsServiceClient(
                WebClient.builder(),
                "http://visits-service",
                "7000"
        );
        visitsServiceClient.setHostname(server.url("/").toString());
    }

    @AfterEach
    void shutdown() throws IOException {
        this.server.shutdown();
    }

    @Test
    void getVisitsForPets_withAvailableVisitsService() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"items\":[{\"id\":5,\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1}]}"));

        Mono<Visits> visits = visitsServiceClient.getVisitsForPets(Collections.singletonList(1));

        assertVisitDescriptionEquals(visits.block(), PET_ID,"test visit");
    }

    @Test
    void getVisitsForPet() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":5,\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1, \"practitionerId\":1,\"status\":false}"));

        Flux<VisitDetails> visits = visitsServiceClient.getVisitsForPet(1);

        assertVisitDescriptionEq(visits.blockFirst(), PET_ID,"test visit");
    }

    private void assertVisitDescriptionEq(VisitDetails visits, int petId, String description) {
        assertEquals(5, visits.getId());
        assertEquals(description, visits.getDescription());
    }

    private void assertVisitDescriptionEquals(Visits visits, int petId, String description) {
        assertEquals(1, visits.getItems().size());
        assertNotNull(visits.getItems().get(0));
        assertEquals(petId, visits.getItems().get(0).getPetId());
        assertEquals(description, visits.getItems().get(0).getDescription());
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

}

