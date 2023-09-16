package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.RatingResponseDTO;
import com.petclinic.bffapigateway.dtos.VetDTO;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;



@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetsServiceClientIntegrationTest {


    private VetsServiceClient vetsServiceClient;

    private MockWebServer server;

    private ObjectMapper mapper;

    VetDTO vetDTO = buildVetDTO();

    @BeforeEach
    void setup() {

        server = new MockWebServer();
        vetsServiceClient = new VetsServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort())
        );
        vetsServiceClient.setVetsServiceUrl(server.url("/").toString());
        mapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    void getRatingsByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"rateScore\": 4.5\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("678910").blockFirst();
        assertEquals("123456", rating.getRatingId());
        assertEquals("678910", rating.getVetId());
        assertEquals(4.5, rating.getRateScore());
    }

    @Test
    void deleteRatingsByRatingId() throws JsonProcessingException{
        final String ratingId = "794ac37f-1e07-43c2-93bc-61839e61d989";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("   {\n" +
                        "        \"ratingId\":\"" + ratingId + "\",\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"rateScore\": 4.5\n" +
                        "    }"));

        final Mono<Void> empty = vetsServiceClient.deleteRating(vetDTO.getVetId(),ratingId);

        assertEquals(empty.block(), null);



    }

//    @Test
//    void getRatingsByVetId_ExistingVetNotFound_ShouldThrowException() {
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setResponseCode(HttpStatus.NOT_FOUND.value())
//                .setBody("Vet with id 678910 not found."));
//
//        StepVerifier.create(vetsServiceClient.getRatingsByVetId("678910"))
//                .expectErrorMatches(throwable -> throwable instanceof ExistingVetNotFoundException
//                        && throwable.getMessage().equals("Vet with id 678910 not found."))
//                .verify();
//    }

    @Test
    void getAllVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.getVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void getActiveVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.getActiveVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void getInactiveVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.getInactiveVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }


    @Test
    void getVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.getVetByVetId("678910").block();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void createVet() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.createVet(Mono.just(vetDTO)).block();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void updateVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.updateVet("678910", Mono.just(vetDTO)).block();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void deleteVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final Mono<Void> empty = vetsServiceClient.deleteVet(vetDTO.getVetId());

        assertEquals(empty.block(), null);
    }

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
                .active(false)
                .build();
    }


}