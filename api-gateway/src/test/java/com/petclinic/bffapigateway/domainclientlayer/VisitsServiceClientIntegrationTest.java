package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();
        visitsServiceClient = new VisitsServiceClient(
                WebClient.builder(),
                "http://visits-service",
                "7000"
        );
        visitsServiceClient.setHostname(server.url("/").toString());
        objectMapper = new ObjectMapper();
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
 
    @Test
    void shouldGetPreviousVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .id(1)
                .petId(21)
                .practitionerId(2)
                .date("2021-12-7")
                .description("Cat is sick")
                .status(false)
                .build();

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final VisitDetails previousVisits = visitsServiceClient.getPreviousVisitsForPet(21).blockFirst();

        assertEquals(visit.getId(), previousVisits.getId());
        assertEquals(visit.getPetId(), previousVisits.getPetId());
        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
        assertEquals(visit.getDate(), previousVisits.getDate());
        assertEquals(visit.getDescription(), previousVisits.getDescription());
        assertEquals(visit.getStatus(), previousVisits.getStatus());
    }
 
    @Test
    void shouldGetScheduledVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .id(1)
                .petId(21)
                .practitionerId(2)
                .date("2021-12-7")
                .description("Cat is sick")
                .status(true)
                .build();

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final VisitDetails scheduledVisits = visitsServiceClient.getScheduledVisitsForPet(21).blockFirst();

        assertEquals(visit.getId(), scheduledVisits.getId());
        assertEquals(visit.getPetId(), scheduledVisits.getPetId());
        assertEquals(visit.getPractitionerId(), scheduledVisits.getPractitionerId());
        assertEquals(visit.getDate(), scheduledVisits.getDate());
        assertEquals(visit.getDescription(), scheduledVisits.getDescription());
        assertEquals(visit.getStatus(), scheduledVisits.getStatus());
      
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
    
    @Test
    void getVisitById() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":5,\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1, \"practitionerId\":1,\"status\":false}"));
        
        Mono<VisitDetails> visit = visitsServiceClient.getVisitById(5);
        
        assertEquals(5, visit.block().getId());
    }

}

