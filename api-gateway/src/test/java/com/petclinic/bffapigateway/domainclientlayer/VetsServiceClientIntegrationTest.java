package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.VetDTO;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetsServiceClientIntegrationTest {

//    private WebTestClient client;
//
//    private VetsServiceClient vetsServiceClient;
//
//    private MockWebServer server;
//
//    private ObjectMapper objectMapper;
//    VetDTO vetDTO = buildVetDTO();
//    String VET_ID = vetDTO.getVetId();
//    String INVALID_VET_ID = "mjbedf";
//
//    @BeforeEach
//    void setup() {
//
//        server = new MockWebServer();
//        vetsServiceClient = new VetsServiceClient(
//                WebClient.builder(),
//                server.getHostName(),
//                String.valueOf(server.getPort())
//        );
//        objectMapper = new ObjectMapper();
//    }
//
//    @AfterEach
//    void shutdown() throws IOException {
//        server.shutdown();
//    }

//
//    @Test
//    void getVetByVetId() {
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody("    {\n" +
//                        "        \"firstName\": \"Kam\",\n" +
//                        "        \"lastName\": \"Hatteea\",\n" +
//                        "        \"email\": \"kam.hatteea@hotmail.com\",\n" +
//                        "        \"phoneNumber\": \"3856\",\n" +
//                        "        \"image\": \"\",\n" +
//                        "        \"resume\": \"working for 18 years\",\n" +
//                        "        \"workday\": \"Monday, Sunday\",\n" +
//                        "        \"specialties\": [\n" +
//                        "            {\n" +
//                        "                \"specialtyId\": \"100003\",\n" +
//                        "                \"name\": \"dentistry\"\n" +
//                        "            }\n" +
//                        "            {\n" +
//                        "                \"specialtyId\": \"100001\",\n" +
//                        "                \"name\": \"radiology\"\n" +
//                        "            }\n" +
//                        "        ],\n" +
//                        "        \"active\": false\n" +
//                        "    }"));
//
//        Mono<VetDTO> vetDTOMono = vetsServiceClient.getVetByVetId("2226410");
//
//        assertEquals("2226410", vetDTOMono.block().getVetId());
//    }
//
//
//


//    private void prepareResponse(Consumer<MockResponse> consumer) {
//        MockResponse response = new MockResponse();
//        consumer.accept(response);
//        this.server.enqueue(response);
//    }
//
//
//
//    private VetDTO buildVetDTO() {
//        return VetDTO.builder()
//                .vetId("678910")
//                .firstName("Clementine")
//                .lastName("LeBlanc")
//                .email("skjfhf@gmail.com")
//                .phoneNumber("947-238-2847")
//                .resume("Just became a vet")
//                .image("kjd".getBytes())
//                .workday("Monday")
//                .specialties(new HashSet<>())
//                .isActive(false)
//                .build();
//    }



}