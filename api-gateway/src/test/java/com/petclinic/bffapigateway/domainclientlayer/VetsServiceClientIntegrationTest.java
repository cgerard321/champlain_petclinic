package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.EducationResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.RatingRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.RatingResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static org.assertj.core.api.Assertions.assertThat;
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
    void getAllRatingsByVetId_ValidId() throws JsonProcessingException {
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
    void getNumberOfRatingsByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("5"));

        final Integer numberOfRatings = vetsServiceClient.getNumberOfRatingsByVetId("678910").block();
        assertEquals(5, numberOfRatings);
    }


    @Test
    void getAverageRatingsByVetId() throws JsonProcessingException {

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("4.5"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetDTO.getVetId()).block();
        assertEquals(4.5, averageRating);

    }


    @Test
    void getRatingPercentagesByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}"));

        final String ratingPercentages = vetsServiceClient.getPercentageOfRatingsByVetId("678910").block();
        assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}", ratingPercentages);
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

        final Mono<Void> empty = vetsServiceClient.deleteRating(vetDTO.getVetId(), ratingId);

        assertEquals(empty.block(), null);
    }

    @Test
    void addRatingToVet() throws JsonProcessingException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("678910")
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"rateScore\": 3.5,\n" +
                        "        \"rateDescription\": \"The vet was decent but lacked table manners.\",\n" +
                        "        \"rateDate\": \"16/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet("678910", Mono.just(ratingRequestDTO)).block();
        assertNotNull(rating.getRatingId());
        assertEquals(ratingRequestDTO.getVetId(), rating.getVetId());
        assertEquals(ratingRequestDTO.getRateScore(), rating.getRateScore());
        assertEquals(ratingRequestDTO.getRateDescription(), rating.getRateDescription());
        assertEquals(ratingRequestDTO.getRateDate(), rating.getRateDate());
    }

    @Test
    void updateRatingByVetIdAndRatingId() throws JsonProcessingException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("678910")
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"rateScore\": 2.0,\n" +
                        "        \"rateDescription\": \"Vet cancelled last minute.\",\n" +
                        "        \"rateDate\": \"20/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO ratingResponseDTO = vetsServiceClient.updateRatingByVetIdAndByRatingId("678910","123456", Mono.just(updatedRating)).block();
        assertNotNull(ratingResponseDTO);
        assertNotNull(ratingResponseDTO.getRatingId());
        assertThat(ratingResponseDTO.getVetId()).isEqualTo(updatedRating.getVetId());
        assertThat(ratingResponseDTO.getRateScore()).isEqualTo(updatedRating.getRateScore());
        assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(updatedRating.getRateDescription());
        assertThat(ratingResponseDTO.getRateDate()).isEqualTo(updatedRating.getRateDate());
    }

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

    @Test void getAllEducationsByVetId_ValidId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "    \"educationId\": \"123456\",\n" +
                        "    \"vetId\": \"678910\",\n" +
                        "    \"schoolName\": \"University of Toronto\",\n" +
                        "    \"degree\": \"Doctor of Veterinary Medicine\",\n" +
                        "    \"fieldOfStudy\": \"Veterinary Medicine\",\n" +
                        "    \"startDate\": \"2015\",\n" +
                        "    \"endDate\": \"2019\"\n" +
                        "    }"));

        final EducationResponseDTO education = vetsServiceClient.getEducationsByVetId("678910").blockFirst();
        assertEquals("123456", education.getEducationId());
        assertEquals("678910", education.getVetId());
        assertEquals("University of Toronto", education.getSchoolName());
        assertEquals("Doctor of Veterinary Medicine", education.getDegree());
        assertEquals("Veterinary Medicine", education.getFieldOfStudy());
        assertEquals("2015", education.getStartDate());
        assertEquals("2019", education.getEndDate());
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