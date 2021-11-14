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
import java.util.UUID;
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
                .setBody("{\"items\":[{\"visitId\":\"773fa7b2-e04e-47b8-98e7-4adf7cfaaeee\"," +
                        "\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1}]}"));

        Mono<Visits> visits = visitsServiceClient.getVisitsForPets(Collections.singletonList(1));

        assertVisitDescriptionEquals(visits.block(), PET_ID,"test visit");
    }

    @Test
    void getVisitsForPet() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"visitId\":\"773fa7b2-e04e-47b8-98e7-4adf7cfaaeee\"," +
                        "\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":1," +
                        " \"practitionerId\":1,\"status\":false}"));

        Flux<VisitDetails> visits = visitsServiceClient.getVisitsForPet(1);

        assertVisitDescriptionEq(visits.blockFirst(), PET_ID,"test visit");
    }

    @Test
    void shouldDeleteVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId(15)
                .practitionerId(2)
                .date("2021-12-12")
                .description("Cat is crazy")
                .status(false)
                .build();

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Mono<Void> empty = visitsServiceClient.deleteVisitByVisitId(visit.getVisitId());

        assertEquals(empty.block(), null);
    }

    @Test
    void shouldCreateVisitsForPet() throws JsonProcessingException {

        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId(21)
                .practitionerId(2)
                .date("2021-12-7")
                .description("Cat is sick")
                .status(false)
                .build();

        visitsServiceClient.createVisitForPet(visit);
        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final VisitDetails petVisit = visitsServiceClient.getVisitsForPet(21).blockFirst();

        assertEquals(visit.getVisitId(), petVisit.getVisitId());
        assertEquals(visit.getPetId(), petVisit.getPetId());
        assertEquals(visit.getPractitionerId(), petVisit.getPractitionerId());
        assertEquals(visit.getDate(), petVisit.getDate());
        assertEquals(visit.getDescription(), petVisit.getDescription());
        assertEquals(visit.getStatus(), petVisit.getStatus());

    }

    @Test
    void shouldUpdateVisitsForPet() throws JsonProcessingException {

        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId(21)
                .practitionerId(2)
                .date("2021-12-7")
                .description("Cat is sick")
                .status(false)
                .build();

        final VisitDetails visit2 = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId(201)
                .practitionerId(22)
                .date("2021-12-7")
                .description("Dog is sick")
                .status(false)
                .build();

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        final String body2 = objectMapper.writeValueAsString(objectMapper.convertValue(visit2, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body2));

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        visitsServiceClient.updateVisitForPet(visit2);
        final VisitDetails petVisit = visitsServiceClient.getVisitsForPet(201).blockFirst();

        assertEquals(visit2.getVisitId(), petVisit.getVisitId());
        assertEquals(visit2.getPetId(), petVisit.getPetId());
        assertEquals(visit2.getPractitionerId(), petVisit.getPractitionerId());
        assertEquals(visit2.getDate(), petVisit.getDate());
        assertEquals(visit2.getDescription(), petVisit.getDescription());
        assertEquals(visit2.getStatus(), petVisit.getStatus());

    }


    @Test
    void shouldGetVisitsForPractitioner() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
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

        final VisitDetails previousVisits = visitsServiceClient.getVisitForPractitioner(21).blockFirst();

        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
        assertEquals(visit.getPetId(), previousVisits.getPetId());
        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
        assertEquals(visit.getDate(), previousVisits.getDate());
        assertEquals(visit.getDescription(), previousVisits.getDescription());
        assertEquals(visit.getStatus(), previousVisits.getStatus());
    }

    @Test
    void shouldGetVisitsByPractitionerIdandMonth() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
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

        final VisitDetails previousVisits = visitsServiceClient.getVisitsByPractitionerIdAndMonth(21,"start","end").blockFirst();

        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
        assertEquals(visit.getPetId(), previousVisits.getPetId());
        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
        assertEquals(visit.getDate(), previousVisits.getDate());
        assertEquals(visit.getDescription(), previousVisits.getDescription());
        assertEquals(visit.getStatus(), previousVisits.getStatus());
    }

    @Test
    void shouldGetPreviousVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
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

        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
        assertEquals(visit.getPetId(), previousVisits.getPetId());
        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
        assertEquals(visit.getDate(), previousVisits.getDate());
        assertEquals(visit.getDescription(), previousVisits.getDescription());
        assertEquals(visit.getStatus(), previousVisits.getStatus());
    }
 
    @Test
    void shouldGetScheduledVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
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

        assertEquals(visit.getVisitId(), scheduledVisits.getVisitId());
        assertEquals(visit.getPetId(), scheduledVisits.getPetId());
        assertEquals(visit.getPractitionerId(), scheduledVisits.getPractitionerId());
        assertEquals(visit.getDate(), scheduledVisits.getDate());
        assertEquals(visit.getDescription(), scheduledVisits.getDescription());
        assertEquals(visit.getStatus(), scheduledVisits.getStatus());
      
    }

    private void assertVisitDescriptionEq(VisitDetails visits, int petId, String description) {
        assertEquals("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee", visits.getVisitId());
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
                .setBody("{\"visitId\":\"773fa7b2-e04e-47b8-98e7-4adf7cfaaeee\"," +
                        "\"date\":\"2018-11-15\",\"description\":\"test visit\"," +
                        "\"petId\":1, \"practitionerId\":1,\"status\":false}"));
        
        Mono<VisitDetails> visit = visitsServiceClient.getVisitByVisitId("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee");
        
        assertEquals("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee", visit.block().getVisitId());
    }

}

