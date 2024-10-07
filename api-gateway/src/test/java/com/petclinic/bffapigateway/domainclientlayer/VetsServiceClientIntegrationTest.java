package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpHeaders.setHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetsServiceClientIntegrationTest {

    private VetsServiceClient vetsServiceClient;

    private MockWebServer server;

    private ObjectMapper mapper;

    VetRequestDTO vetRequestDTO = buildVetRequestDTO();
    VetResponseDTO vetResponseDTO = buildVetResponseDTO();

    ClassPathResource cpr=new ClassPathResource("static/images/full_food_bowl.png");
    ClassPathResource cpr2=new ClassPathResource("static/images/vet_default.jpg");


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
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 4.5\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").blockFirst();
        assertEquals("123456", rating.getRatingId());
        assertEquals("deb1950c-3c56-45dc-874b-89e352695eb7", rating.getVetId());
        //Had to make it optional so it could diferentiate between double and object
        assertEquals(Optional.of(4.5), Optional.ofNullable(rating.getRateScore()));
    }

    @Test
    void getAllRatingsByInvalidVetId_shouldNotSucceed() throws JsonProcessingException {
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId(invalidVetId).onErrorResume(throwable -> {
            if (throwable instanceof NotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                return Mono.empty();
            } else {
                return Mono.error(throwable);
            }
        }).blockFirst();

        assertNull(rating);
    }

    @Test
    void getAllRatingsByVetId_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                return Mono.empty();
            } else {
                return Mono.error(throwable);
            }
        }).blockFirst();

        assertNull(rating);
    }

    @Test
    void getAllRatingsByVetId_IllegalArgumentException400() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                return Mono.empty();
            } else {
                return Mono.error(throwable);
            }
        }).blockFirst();

        assertNull(rating);
    }


    @Test
    void getNumberOfRatingsByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("5"));

        final Integer numberOfRatings = vetsServiceClient.getNumberOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        //Had to make it optional so it could diferentiate between double and object
        assertEquals(Optional.of(5), Optional.ofNullable(numberOfRatings));
    }
    @Test
    void getTopThreeVetsWithHighestRating() throws JsonProcessingException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"averageRating\": 4.5\n" +
                        "    }"));

        final VetAverageRatingDTO averageRatingDTO = vetsServiceClient.getTopThreeVetsWithHighestAverageRating().blockFirst();
        assertEquals("deb1950c-3c56-45dc-874b-89e352695eb7", vetResponseDTO.getVetId());
        assertEquals(4.5, averageRatingDTO.getAverageRating(),0.1);
        //its 0.0 because the vet doesn't have any ratings
    }
    @Test
    void getRatingBasedOnDate() throws JsonProcessingException{
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("year", "2023");

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\n" +
                        "    \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                                "        \"ratingId\": \"123456\",\n" +
                                "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                                "        \"rateScore\": 4.5,\n" +
                                "        \"date\": \"2023\" \n" +
                        "}"));

        final RatingResponseDTO ratingResponseDTO = vetsServiceClient.getRatingsOfAVetBasedOnDate(vetResponseDTO.getVetId(), queryParams).blockFirst();

        System.out.print(ratingResponseDTO);
        assertNotNull(ratingResponseDTO);
        Assertions.assertEquals("2023", ratingResponseDTO.getDate());
        Assertions.assertEquals("deb1950c-3c56-45dc-874b-89e352695eb7", ratingResponseDTO.getVetId());

    }

    @Test
    void getNumberOfRatingsByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final Integer numberOfRatings = vetsServiceClient.getNumberOfRatingsByVetId(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(numberOfRatings);
    }

    @Test
    void getNumberOfRatingsByVetId_IllegalArgumentException400() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final Integer ratingNumber = vetsServiceClient.getNumberOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                return Mono.empty();
            } else {
                return Mono.error(throwable);
            }
        }).block();

        assertNull(ratingNumber);
    }

    @Test
    void getNumberOfRatingsByVetId_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final Integer ratingNumber = vetsServiceClient.getNumberOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                return Mono.empty();
            } else {
                return Mono.error(throwable);
            }
        }).block();

        assertNull(ratingNumber);
    }

    @Test
    void getAverageRatingsByVetId() throws JsonProcessingException {

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("4.5"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetResponseDTO.getVetId())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();
        //Had to make it optional so it could diferentiate between double and object
        assertEquals(Optional.of(4.5), Optional.ofNullable(averageRating));
    }

    @Test
    void getAverageRatingsByVetId_shouldNotSucceed()throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(invalidVetId)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(averageRating);
    }

    @Test
    void getAverageRatingsByVetId_IllegalArgument400() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetResponseDTO.getVetId())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();
        assertNull(averageRating);
    }

    @Test
    void getAverageRatingsByVetId_IllegalArgument500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetResponseDTO.getVetId())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();
        assertNull(averageRating);
    }

    @Test
    void getRatingPercentagesByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}"));

        final String ratingPercentages = vetsServiceClient.getPercentageOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}", ratingPercentages);
    }

    @Test
    void getRatingPercentagesByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final String ratingPercentages = vetsServiceClient.getPercentageOfRatingsByVetId(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(ratingPercentages);
    }

    @Test
    void getRatingPercentagesByValidVetId_IllegalException400() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final String ratingPercentages =
                vetsServiceClient.getPercentageOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(ratingPercentages);
    }

    @Test
    void getRatingPercentagesByValidVetId_IllegalException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final String ratingPercentages =
                vetsServiceClient.getPercentageOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(ratingPercentages);
    }

    @Test
    void deleteRatingsByRatingId() throws JsonProcessingException{
        final String ratingId = "794ac37f-1e07-43c2-93bc-61839e61d989";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("   {\n" +
                        "        \"ratingId\":\"" + ratingId + "\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 4.5\n" +
                        "    }"));

        final Mono<Void> empty = vetsServiceClient.deleteRating(vetResponseDTO.getVetId(), ratingId);

        assertEquals(empty.block(), null);
    }

    @Test
    void deleteRatingsByInvalidRatingId_shouldNotSucceed() throws NotFoundException{
        String invalidVetId="123";
        String validRatingId="456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found "+invalidVetId+" or ratingId not found: " + validRatingId));

        final Void empty = vetsServiceClient.deleteRating(invalidVetId, validRatingId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof NotFoundException && throwable.getMessage().equals("vetId not found "+invalidVetId+" or ratingId not found: " + validRatingId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void deleteRatingsByRatingId_IllegalArgumentException400() throws IllegalArgumentException{
        String validVetId="123";
        String validRatingId="456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final Void empty = vetsServiceClient.deleteRating(validVetId, validRatingId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void deleteRatingsByRatingId_IllegalArgumentException500() throws IllegalArgumentException{
        String validVetId="123";
        String validRatingId="456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final Void empty = vetsServiceClient.deleteRating(validVetId, validRatingId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void addRatingToVet_WithRateDescriptionOnly_ShouldSetRateDescriptionToItsValue() throws JsonProcessingException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(null)
                .rateDate("16/09/2023")
                .build();
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 3.5,\n" +
                        "        \"rateDescription\": \"The vet was decent but lacked table manners.\",\n" +
                        "        \"predefinedDescription\": null,\n" +
                        "        \"rateDate\": \"16/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(ratingRequestDTO)).block();
        assertNotNull(rating.getRatingId());
        assertEquals(ratingRequestDTO.getVetId(), rating.getVetId());
        assertEquals(ratingRequestDTO.getRateScore(), rating.getRateScore());
        assertEquals(ratingRequestDTO.getRateDescription(), rating.getRateDescription());
        assertEquals(ratingRequestDTO.getPredefinedDescription(), rating.getPredefinedDescription());
        assertEquals(ratingRequestDTO.getRateDate(), rating.getRateDate());
    }

    @Test
    void addRatingToVet_withPredefinedDescOnly_ShouldSetRateDescriptionToPredefDescription() throws JsonProcessingException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 3.0,\n" +
                        "        \"rateDescription\": \"GOOD\",\n" +
                        "        \"predefinedDescription\": \"GOOD\",\n" +
                        "        \"rateDate\": \"16/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(ratingRequestDTO)).block();

        assertNotNull(rating.getRatingId());
        assertEquals(ratingRequestDTO.getVetId(), rating.getVetId());
        assertEquals(ratingRequestDTO.getRateScore(), rating.getRateScore(), 0.01); // Use delta for comparing double values
        assertEquals(ratingRequestDTO.getPredefinedDescription(), rating.getPredefinedDescription());

        // Assert that rateDescription is set to the predefined description's name
        assertEquals(ratingRequestDTO.getPredefinedDescription().name(), rating.getRateDescription());
    }

    @Test
    void addRatingToVet_withPredefinedDescAndRateDesc_ShouldSetRateDescriptionToPredefDescription() throws JsonProcessingException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.0)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 3.0,\n" +
                        "        \"rateDescription\": \"GOOD\",\n" +
                        "        \"predefinedDescription\": \"GOOD\",\n" +
                        "        \"rateDate\": \"16/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(ratingRequestDTO)).block();
        assertNotNull(rating.getRatingId());
        assertEquals(ratingRequestDTO.getVetId(), rating.getVetId());
        assertEquals(ratingRequestDTO.getRateScore(), rating.getRateScore());
        // Assert that rateDescription is set to the predefined description's name
        assertEquals(ratingRequestDTO.getPredefinedDescription().name(), rating.getRateDescription());
        assertEquals(ratingRequestDTO.getPredefinedDescription(), rating.getPredefinedDescription());
    }
    @Test
    void addRatingToVet_withInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException {
        String invalidVetId="123";

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet(invalidVetId, Mono.just(ratingRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(rating);
    }

    @Test
    void addRatingToVet_IllegalArgumentException400() throws IllegalArgumentException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        String validVetId="deb1950c-3c56-45dc-874b-89e352695eb7";
        String validRatingId="12345";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet(validVetId, Mono.just(ratingRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(rating);
    }

    @Test
    void addRatingToVet_IllegalArgumentException500() throws IllegalArgumentException {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        String validVetId="deb1950c-3c56-45dc-874b-89e352695eb7";
        String validRatingId="12345";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet(validVetId, Mono.just(ratingRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(rating);
    }

    @Test
    void updateRatingByVetIdAndRatingId_withRateDescriptionOnly_ShouldSetRateDescriptionToItsValue() throws JsonProcessingException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription("Vet cancelled last minute.")
                .predefinedDescription(null)
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 2.0,\n" +
                        "        \"rateDescription\": \"Vet cancelled last minute.\",\n" +
                        "        \"predefinedDescription\": null,\n" +
                        "        \"rateDate\": \"20/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating)).block();
        assertNotNull(ratingResponseDTO);
        assertNotNull(ratingResponseDTO.getRatingId());
        assertEquals(updatedRating.getVetId(), ratingResponseDTO.getVetId());
        assertEquals(updatedRating.getRateScore(), ratingResponseDTO.getRateScore());
        assertEquals(updatedRating.getRateDescription(), ratingResponseDTO.getRateDescription());
        assertEquals(updatedRating.getPredefinedDescription(), ratingResponseDTO.getPredefinedDescription());
        assertEquals(updatedRating.getRateDate(), ratingResponseDTO.getRateDate());
    }

    @Test
    void updateRatingByVetIdAndRatingId_withPredefinedDescOnly_ShouldSetRateDescriptionToPredefDescription() throws JsonProcessingException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.POOR)
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 2.0,\n" +
                        "        \"rateDescription\": \"POOR\",\n" +
                        "        \"predefinedDescription\": \"POOR\",\n" +
                        "        \"rateDate\": \"20/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating)).block();
        assertNotNull(ratingResponseDTO);
        assertNotNull(ratingResponseDTO.getRatingId());
        assertEquals(updatedRating.getVetId(), ratingResponseDTO.getVetId());
        assertEquals(updatedRating.getRateScore(), ratingResponseDTO.getRateScore());
        // assert that rateDescription is set to the predefined description's name
        assertEquals(updatedRating.getPredefinedDescription().name(), ratingResponseDTO.getRateDescription());
        assertEquals(updatedRating.getPredefinedDescription(), ratingResponseDTO.getPredefinedDescription());
        assertEquals(updatedRating.getRateDate(), ratingResponseDTO.getRateDate());
    }

    @Test
    void updateRatingByVetIdAndRatingId_withPredefinedDescAndRateDesc_ShouldSetRateDescriptionToPredefDescription() throws JsonProcessingException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription("Vet cancelled last minute.")
                .predefinedDescription(PredefinedDescription.POOR)
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"ratingId\": \"123456\",\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"rateScore\": 2.0,\n" +
                        "        \"rateDescription\": \"POOR\",\n" +
                        "        \"predefinedDescription\": \"POOR\",\n" +
                        "        \"rateDate\": \"20/09/2023\"\n" +
                        "    }"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating)).block();
        assertNotNull(ratingResponseDTO);
        assertNotNull(ratingResponseDTO.getRatingId());
        assertEquals(updatedRating.getVetId(), ratingResponseDTO.getVetId());
        assertEquals(updatedRating.getRateScore(), ratingResponseDTO.getRateScore());
        // assert that rateDescription is set to rateDescription's value
        assertEquals(updatedRating.getPredefinedDescription().name(), ratingResponseDTO.getRateDescription());
        assertEquals(updatedRating.getPredefinedDescription(), ratingResponseDTO.getPredefinedDescription());
        assertEquals(updatedRating.getRateDate(), ratingResponseDTO.getRateDate());
    }

    @Test
    void updateRatingByInvalidVetIdOrInvalidRatingId() throws NotFoundException {
        String invalidVetId="123";
        String invalidRatingId="123";

        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("Rating not found for vetId: " + invalidVetId + " and ratingId: " + invalidRatingId));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId(invalidVetId,invalidRatingId, Mono.just(updatedRating))
                        .onErrorResume(throwable -> {
                            if (throwable instanceof NotFoundException && throwable.getMessage().equals("Rating not found for vetId: " + invalidVetId + " and ratingId: " + invalidRatingId)) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(ratingResponseDTO);
    }

    @Test
    void updateRatingByVetIdOrRatingId_IllegalArgumentException400() throws IllegalArgumentException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating))
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(ratingResponseDTO);
    }

    @Test
    void updateRatingByVetIdOrRatingId_IllegalArgumentException500() throws IllegalArgumentException {
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating))
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(ratingResponseDTO);
    }

    @Test
    void getPhotoByVetId() throws IOException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "image/jpeg")
                .setBody("    {\n" +
                        "        {12, 24, 52, 87}" +
                        "    }"));

        final Resource photo = vetsServiceClient.getPhotoByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        byte[] photoBytes = FileCopyUtils.copyToByteArray(photo.getInputStream());

        assertNotNull(photoBytes);
    }
    @Test
    void getDefaultPhotoByVetId() throws IOException {
        PhotoResponseDTO photoResponseDTO = PhotoResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .filename("vet_default.jpg")
                .imgType("image/jpeg")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr2.getInputStream())))
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"" + photoResponseDTO.getVetId() + "\",\n" +
                        "        \"filename\": \"" + photoResponseDTO.getFilename() + "\",\n" +
                        "        \"imgType\": \"" + photoResponseDTO.getImgType() + "\",\n" +
                        "        \"resourceBase64\": \"" + photoResponseDTO.getResourceBase64() + "\"\n" +
                        "    }"));

        StepVerifier.create(vetsServiceClient.getDefaultPhotoByVetId("cf25e779-548b-4788-aefa-6d58621c2feb"))
                .consumeNextWith(responseDTO -> {
                    assertEquals(photoResponseDTO.getFilename(), responseDTO.getFilename());
                    assertEquals(photoResponseDTO.getImgType(), responseDTO.getImgType());
                    assertEquals(photoResponseDTO.getVetId(), responseDTO.getVetId());
                    assertEquals(photoResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }
    @Test
    void getDefaultPhotoByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("Something went wrong"));

        final PhotoResponseDTO defaultPhoto = vetsServiceClient.getDefaultPhotoByVetId(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("Photo for vet "+invalidVetId + " not found")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(defaultPhoto);
    }
    @Test
    void getDefaultPhotoByVetId_IllegalArgumentException400() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final PhotoResponseDTO photoResponseDTO = vetsServiceClient.getDefaultPhotoByVetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .onErrorResume(throwable->{
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(photoResponseDTO);
    }
    @Test
    void getDefaultPhotoByVetId_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final PhotoResponseDTO photoResponseDTO = vetsServiceClient.getDefaultPhotoByVetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .onErrorResume(throwable->{
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(photoResponseDTO);
    }

    @Test
    void addPhotoToVet() throws IOException {
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(new byte[]{12, 24, 52, 87}));
        prepareResponse(response -> response
                .setHeader("Content-Type", "image/jpeg")
                .setBody("    {\n" +
                        "        {12, 24, 52, 87}" +
                        "    }"));

        final Resource photo = vetsServiceClient.addPhotoToVet("deb1950c-3c56-45dc-874b-89e352695eb7", "image/jpeg", photoResource).block();
        byte[] photoBytes = FileCopyUtils.copyToByteArray(photo.getInputStream());

        assertNotNull(photoBytes);
    }

    @Test
    void updatePhotoByValidVetId_shouldSucceed() throws IOException {
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(new byte[]{12, 24, 52, 87}));
        prepareResponse(response -> response
                .setHeader("Content-Type", "image/jpeg")
                .setBody("    {\n" +
                        "        {12, 24, 52, 87}" +
                        "    }"));

        final Resource photo = vetsServiceClient.updatePhotoOfVet("deb1950c-3c56-45dc-874b-89e352695eb7", "image/jpeg", photoResource).block();
        byte[] photoBytes = FileCopyUtils.copyToByteArray(photo.getInputStream());

        assertNotNull(photoBytes);
    }

    @Test
    void updatePhotoByInvalidVetId_shouldThrowNotFoundException() throws NotFoundException {
        String invalidVetId="123";
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(new byte[]{12, 24, 52, 87}));

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("Photo for vet "+invalidVetId + " not found"));

        final Resource photo = vetsServiceClient.updatePhotoOfVet(invalidVetId, "image/jpeg", photoResource)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof NotFoundException && throwable.getMessage().equals("Photo for vet "+invalidVetId + " not found")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(photo);
    }

    @Test
    void updatePhotoByValidVetId_IllegalArgumentException400() throws IllegalArgumentException {
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(new byte[]{12, 24, 52, 87}));

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final Resource photo = vetsServiceClient.updatePhotoOfVet("deb1950c-3c56-45dc-874b-89e352695eb7", "image/jpeg", photoResource)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(photo);
    }

    @Test
    void updatePhotoByValidVetId_IllegalArgumentException500() throws IllegalArgumentException {
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(new byte[]{12, 24, 52, 87}));


        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final Resource photo = vetsServiceClient.updatePhotoOfVet("deb1950c-3c56-45dc-874b-89e352695eb7", "image/jpeg", photoResource)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                                return Mono.empty();
                            } else {
                                return Mono.error(throwable);
                            }
                        })
                        .block();

        assertNull(photo);
    }

    @Test
    void getBadgeByVetId() throws IOException{
        BadgeResponseDTO badgeResponseDTO = BadgeResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2017")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr.getInputStream())))
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"cf25e779-548b-4788-aefa-6d58621c2feb\",\n" +
                        "        \"badgeTitle\": \"" + badgeResponseDTO.getBadgeTitle() + "\",\n" +
                        "        \"badgeDate\": \"2017\",\n" +
                        "        \"resourceBase64\": \"" + badgeResponseDTO.getResourceBase64() + "\"\n" +
                        "    }"));

        StepVerifier.create(vetsServiceClient.getBadgeByVetId("cf25e779-548b-4788-aefa-6d58621c2feb"))
                .consumeNextWith(responseDTO -> {
                    assertEquals(badgeResponseDTO.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badgeResponseDTO.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badgeResponseDTO.getVetId(), responseDTO.getVetId());
                    assertEquals(badgeResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }

    @Test
    void getBadgeByInvalidVetId_shouldNotSucceed() throws NotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final BadgeResponseDTO badgeResponseDTO = vetsServiceClient.getBadgeByVetId(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof NotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(badgeResponseDTO);
    }

    @Test
    void getBadgeByVetId_IllegalArgumentException400() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final BadgeResponseDTO badgeResponseDTO = vetsServiceClient.getBadgeByVetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .onErrorResume(throwable->{
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(badgeResponseDTO);
    }

    @Test
    void getBadgeByVetId_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final BadgeResponseDTO badgeResponseDTO = vetsServiceClient.getBadgeByVetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .onErrorResume(throwable->{
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(badgeResponseDTO);
    }

    @Test
    void getAllVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false,\n" +
                        "        \"workHoursJson\": \"{\\n" +
                        "            \\\"Monday\\\": [\\\"Hour_8_9\\\",\\\"Hour_9_10\\\",\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\"],\\n" +  // Escaped newline
                        "            \\\"Wednesday\\\": [\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\",\\\"Hour_18_19\\\",\\\"Hour_19_20\\\"],\\n" +  // Escaped newline
                        "            \\\"Thursday\\\": [\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\"]\\n" +  // Escaped newline
                        "        }\"" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.getVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
        assertEquals(vetResponseDTO.getWorkHoursJson(),vet.getWorkHoursJson());
    }

    @Test
    void getAllVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.getVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .blockFirst();

        assertNull(vet);
    }

    @Test
    void getActiveVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false,\n" +
                        "        \"workHoursJson\": \"{\\n" +
                        "            \\\"Monday\\\": [\\\"Hour_8_9\\\",\\\"Hour_9_10\\\",\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\"],\\n" +  // Escaped newline
                        "            \\\"Wednesday\\\": [\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\",\\\"Hour_18_19\\\",\\\"Hour_19_20\\\"],\\n" +  // Escaped newline
                        "            \\\"Thursday\\\": [\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\"]\\n" +  // Escaped newline
                        "        }\"" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.getActiveVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
        assertEquals(vetResponseDTO.getWorkHoursJson(),vet.getWorkHoursJson());
    }

    @Test
    void getActiveVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.getActiveVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .blockFirst();

        assertNull(vet);
    }

    @Test
    void getInactiveVets() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false,\n" +
                        "        \"workHoursJson\": \"{\\n" +
                        "            \\\"Monday\\\": [\\\"Hour_8_9\\\",\\\"Hour_9_10\\\",\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\"],\\n" +  // Escaped newline
                        "            \\\"Wednesday\\\": [\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\",\\\"Hour_18_19\\\",\\\"Hour_19_20\\\"],\\n" +  // Escaped newline
                        "            \\\"Thursday\\\": [\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\"]\\n" +  // Escaped newline
                        "        }\"" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.getInactiveVets().blockFirst();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
        assertEquals(vetResponseDTO.getWorkHoursJson(),vet.getWorkHoursJson());
    }

    @Test
    void getInactiveVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.getInactiveVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .blockFirst();

        assertNull(vet);
    }

    @Test
    void getVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false,\n" +
                        "        \"workHoursJson\": \"{\\n" +
                        "            \\\"Monday\\\": [\\\"Hour_8_9\\\",\\\"Hour_9_10\\\",\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\"],\\n" +  // Escaped newline
                        "            \\\"Wednesday\\\": [\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\",\\\"Hour_18_19\\\",\\\"Hour_19_20\\\"],\\n" +  // Escaped newline
                        "            \\\"Thursday\\\": [\\\"Hour_10_11\\\",\\\"Hour_11_12\\\",\\\"Hour_12_13\\\",\\\"Hour_13_14\\\",\\\"Hour_14_15\\\",\\\"Hour_15_16\\\",\\\"Hour_16_17\\\",\\\"Hour_17_18\\\"]\\n" +  // Escaped newline
                        "        }\"" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
        assertEquals(vetResponseDTO.getWorkHoursJson(),vet.getWorkHoursJson());
    }

    @Test
    void getVetByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final VetResponseDTO vet = vetsServiceClient.getVetByVetId(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void getVetByInvalidVetId_IllegalArgumentException400() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final VetResponseDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void getVetByInvalidVetId_IllegalArgumentException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void createVet() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.createVet(Mono.just(vetRequestDTO)).block();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
    }

    @Test
    void createVet_IllegalArgumentException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.createVet(Mono.just(vetRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void updateVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetResponseDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetRequestDTO)).block();
        assertFalse(vet.isActive());
        assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
        assertEquals(vetResponseDTO.getEmail(), vet.getEmail());
        assertEquals(vetResponseDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetResponseDTO.getResume(), vet.getResume());
    }

    @Test
    void updateVetByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final VetResponseDTO vet = vetsServiceClient.updateVet(invalidVetId, Mono.just(vetRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void updateVetByValidVetId_IllegalArgumentException400() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final VetResponseDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void updateVetByValidVetId_IllegalArgumentException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final VetResponseDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(vet);
    }

    @Test
    void deleteVetByVetId() throws JsonProcessingException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "        \"firstName\": \"Clementine\",\n" +
                        "        \"lastName\": \"LeBlanc\",\n" +
                        "        \"email\": \"skjfhf@gmail.com\",\n" +
                        "        \"phoneNumber\": \"947-238-2847\",\n" +
                        "        \"resume\": \"Just became a vet\",\n" +
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final Mono<Void> empty = vetsServiceClient.deleteVet(vetResponseDTO.getVetId());

        assertEquals(empty.block(), null);
    }

    @Test
    void deleteVetByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final Void empty = vetsServiceClient.deleteVet(invalidVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void deleteVetByVetId_IllegalArgumentException400() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final Void empty = vetsServiceClient.deleteVet(vetResponseDTO.getVetId())
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void deleteVetByVetId_IllegalArgumentException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final Void empty = vetsServiceClient.deleteVet(vetResponseDTO.getVetId())
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
    }

    @Test
    void getAllEducationsOfVetId_ValidId() throws JsonProcessingException {
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
        assertNotNull(education.getEducationId());
        assertEquals("678910", education.getVetId());
        assertEquals("University of Toronto", education.getSchoolName());
        assertEquals("Doctor of Veterinary Medicine", education.getDegree());
        assertEquals("Veterinary Medicine", education.getFieldOfStudy());
        assertEquals("2015", education.getStartDate());
        assertEquals("2019", education.getEndDate());

    }

    @Test
    void addEducationsToAVet() throws JsonProcessingException {
        EducationRequestDTO educationRequestDTO = EducationRequestDTO.builder()
                .vetId("678910")
                .schoolName("University of Toronto")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

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

        final EducationResponseDTO education = vetsServiceClient.addEducationToAVet("678910", Mono.just(educationRequestDTO)).block();
        assertNotNull(education.getEducationId());
        assertEquals(educationRequestDTO.getVetId(), education.getVetId());
        assertEquals(educationRequestDTO.getSchoolName(), education.getSchoolName());
        assertEquals(educationRequestDTO.getDegree(), education.getDegree());
        assertEquals(educationRequestDTO.getFieldOfStudy(), education.getFieldOfStudy());
        assertEquals(educationRequestDTO.getStartDate(), education.getStartDate());
        assertEquals(educationRequestDTO.getEndDate(), education.getEndDate());
    }

    @Test
    void addEducationToVet_withInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException {
        EducationRequestDTO newEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.addEducationToAVet(invalidVetId, Mono.just(newEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExistingVetNotFoundException && throwable.getMessage().equals("vetId not found: "+invalidVetId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void addEducationToVet_IllegalArgumentException400() throws IllegalArgumentException {
        EducationRequestDTO newEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String validVetId="678910";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.addEducationToAVet(validVetId, Mono.just(newEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void addEducationToVet_IllegalArgumentException500() throws IllegalArgumentException {
        EducationRequestDTO newEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String validVetId="678910";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.addEducationToAVet(validVetId, Mono.just(newEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void updateEducationByVetIdAndEducationId() throws JsonProcessingException {
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "        \"educationId\": \"123456\",\n" +
                        "        \"vetId\": \"678910\",\n" +
                        "        \"schoolName\": \"McGill\",\n" +
                        "        \"degree\": \"Bachelor of Medicine\",\n" +
                        "        \"fieldOfStudy\": \"Medicine\",\n" +
                        "        \"startDate\": \"2010\",\n" +
                        "        \"endDate\": \"2015\"\n" +
                        "    }"));

        final EducationResponseDTO educationResponseDTO =
                vetsServiceClient.updateEducationByVetIdAndByEducationId("678910","123456", Mono.just(updatedEducation)).block();
        assertNotNull(educationResponseDTO);
        assertNotNull(educationResponseDTO.getEducationId());
        assertThat(educationResponseDTO.getVetId()).isEqualTo(updatedEducation.getVetId());
        assertThat(educationResponseDTO.getSchoolName()).isEqualTo(updatedEducation.getSchoolName());
        assertThat(educationResponseDTO.getDegree()).isEqualTo(updatedEducation.getDegree());
        assertThat(educationResponseDTO.getFieldOfStudy()).isEqualTo(updatedEducation.getFieldOfStudy());
        assertThat(educationResponseDTO.getStartDate()).isEqualTo(updatedEducation.getStartDate());
        assertThat(educationResponseDTO.getEndDate()).isEqualTo(updatedEducation.getEndDate());
    }

    @Test
    void updateEducationByInvalidVetIdAndValidEducationId_shouldNotSucceed() throws NotFoundException{
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String invalidVetId="123";
        String validEducationId="123456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.updateEducationByVetIdAndByEducationId(invalidVetId, validEducationId, Mono.just(updatedEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof NotFoundException && throwable.getMessage().equals("Education not found for vetId: " + invalidVetId + " and educationId: " + validEducationId)) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void updateEducationByValidVetIdAndValidEducationId_IllegalArgumentException400() throws IllegalArgumentException{
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String validVetId="deb1950c-3c56-45dc-874b-89e352695eb7";
        String validEducationId="123456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.updateEducationByVetIdAndByEducationId(validVetId, validEducationId, Mono.just(updatedEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void updateEducationByValidVetIdAndValidEducationId_IllegalArgumentException500() throws IllegalArgumentException{
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        String validVetId="deb1950c-3c56-45dc-874b-89e352695eb7";
        String validEducationId="123456";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final EducationResponseDTO educationResponseDTO = vetsServiceClient.updateEducationByVetIdAndByEducationId(validVetId, validEducationId, Mono.just(updatedEducation))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(educationResponseDTO);
    }

    @Test
    void deleteEducationsByEducationId() throws JsonProcessingException{
        final String educationId = "794ac37f-1e07-43c2-93bc-61839e61d989";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("    {\n" +
                        "    \"educationId\": \"123456\",\n" +
                        "    \"vetId\": \"deb1950c-3c56-45dc-874b-89e352695eb7\",\n" +
                        "    \"schoolName\": \"University of Toronto\",\n" +
                        "    \"degree\": \"Doctor of Veterinary Medicine\",\n" +
                        "    \"fieldOfStudy\": \"Veterinary Medicine\",\n" +
                        "    \"startDate\": \"2015\",\n" +
                        "    \"endDate\": \"2019\"\n" +
                        "    }"));


        final Mono<Void> empty = vetsServiceClient.deleteEducation(vetResponseDTO.getVetId(), educationId);

        assertEquals(empty.block(), null);
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

    private VetResponseDTO buildVetResponseDTO() {
        return VetResponseDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetRequestDTO buildVetRequestDTO() {
        return VetRequestDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .photoDefault(true)
                .build();
    }

    @Test
    void shouldReturnAlbums_whenValidVetIdIsProvided() throws Exception {

        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        Album album1 = new Album(1, vetId, "photo1.jpg", "image/jpeg", "mockImageData1".getBytes());
        Album album2 = new Album(2, vetId, "photo2.jpg", "image/jpeg", "mockImageData2".getBytes());

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mapper.writeValueAsString(List.of(album1, album2)))
                .addHeader("Content-Type", "application/json"));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectNext(album1)
                .expectNext(album2)
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/69f852ca-625b-11ee-8c99-0242ac120002/albums", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void shouldThrowExistingVetNotFoundException_whenVetIdIsInvalid() throws Exception {

        String vetId = "69f852ca-625b-11ee-8c99-0242ac1200020000";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ExistingVetNotFoundException &&
                        throwable.getMessage().contains("Albums for vet 69f852ca-625b-11ee-8c99-0242ac1200020000 not found"))
                .verify();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/69f852ca-625b-11ee-8c99-0242ac1200020000/albums", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenServerErrorOccurs() throws Exception {

        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Server error"))
                .verify();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/albums", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenClientErrorOccurs() throws Exception {

        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Client error"))
                .verify();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/albums", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void shouldDeleteSpecialty_whenSpecialtyIdIsValid() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String specialtyId = "794ac37f-1e07-43c2-93bc-61839e61d989";

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")); // Assuming successful delete returns an empty body

        Mono<Void> result = vetsServiceClient.deleteSpecialtiesByVetId(vetId, specialtyId);

        StepVerifier.create(result)
                .verifyComplete(); // Verifies that the operation completes successfully

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/deb1950c-3c56-45dc-874b-89e352695eb7/specialties/794ac37f-1e07-43c2-93bc-61839e61d989", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void shouldThrowExistingVetNotFoundException_whenSpecialtyIdIsInvalid() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String specialtyId = "794ac37f-1e07-43c2-93bc-61839e61d9890000";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("")); // Simulating a 404 response

        Mono<Void> result = vetsServiceClient.deleteSpecialtiesByVetId(vetId, specialtyId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("Vet not found: " + vetId))
                .verify(); // Verifying the error

        // Validating the request was sent correctly
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/deb1950c-3c56-45dc-874b-89e352695eb7/specialties/794ac37f-1e07-43c2-93bc-61839e61d9890000", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }


}

