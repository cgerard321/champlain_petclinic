package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.VetDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

class VetsServiceClientIntegrationTest {

    @Test
    void getVets() {
    }

    @Test
    void getVetByVetId() {
    }

    @Test
    void getInactiveVets() {
    }

    @Test
    void getActiveVets() {
    }

    @Test
    void createVet() {
    }

    @Test
    void deleteVet() {
    }

    @Test
    void updateVet() {
    }


    private VetsServiceClient vetsServiceClient;

    private MockWebServer server;

    private ObjectMapper mapper;
    VetDTO vetDTO = buildVetDTO();
    String VET_ID = vetDTO.getVetId();
    String INVALID_VET_ID = "mjbedf";

    @BeforeEach
    void setup() {

        server = new MockWebServer();
        vetsServiceClient = new VetsServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort())
        );
        mapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

//    @Test
//    void getVetByVetId() throws JsonProcessingException {
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody("    {\n" +
//                        "        \"vetId\": \"678910\",\n" +
//                        "        \"firstName\": \"Clementine\",\n" +
//                        "        \"lastName\": \"LeBlanc\",\n" +
//                        "        \"email\": \"skjfhf@gmail.com\",\n" +
//                        "        \"phoneNumber\": \"947-238-2847\",\n" +
////                        "        \"image\": \"\",\n" +
//                        "        \"resume\": \"Just became a vet\",\n" +
//                        "        \"workday\": \"Monday\",\n" +
//                        "        \"active\": false\n" +
//                        "    }"));
//
//        final VetDTO vet = vetsServiceClient.getVetByVetId("678910").block();
//        assertFalse(vet.isActive());
//        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
//        assertEquals(vetDTO.getLastName(), vet.getLastName());
//        assertEquals(vetDTO.getEmail(), vet.getEmail());
//        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
//        assertEquals(vetDTO.getResume(), vet.getResume());
//        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
//    }


    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .image("kjd".getBytes())
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(false)
                .build();
    }
}