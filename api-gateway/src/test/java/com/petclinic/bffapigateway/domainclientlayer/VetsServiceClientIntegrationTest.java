package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.mock.web.MockMultipartFile;
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

        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("123456")
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .rateScore(4.5)
                .rateDescription("Excellent service!")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2023-10-15")
                .customerName("John Doe")
                .build();

        String jsonResponse = new ObjectMapper().writeValueAsString(ratingResponseDTO);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").blockFirst();

        assertNotNull(rating);
        assertEquals("123456", rating.getRatingId());
        assertEquals("deb1950c-3c56-45dc-874b-89e352695eb7", rating.getVetId());
        assertEquals(Optional.of(4.5), Optional.ofNullable(rating.getRateScore()));
        assertEquals("Excellent service!", rating.getRateDescription());
        assertEquals("EXCELLENT", rating.getPredefinedDescription().name());
        assertEquals("2023-10-15", rating.getRateDate());
        assertEquals("John Doe", rating.getCustomerName());
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
        byte[] testData = new byte[]{12, 24, 52, 87};
        PhotoResponseDTO photoResponseDTO = PhotoResponseDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .filename("vet_photo.jpg")
                .imgType("image/jpeg")
                .resource(testData)
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"vetId\": \"" + photoResponseDTO.getVetId() + "\"," +
                        "\"filename\": \"" + photoResponseDTO.getFilename() + "\"," +
                        "\"imgType\": \"" + photoResponseDTO.getImgType() + "\"," +
                        "\"resource\": " + java.util.Arrays.toString(testData) + "," +
                        "\"resourceBase64\": \"" + Base64.getEncoder().encodeToString(testData) + "\"" +
                        "}"));

        final Resource photo = vetsServiceClient.getPhotoByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        byte[] photoBytes = FileCopyUtils.copyToByteArray(photo.getInputStream());

        assertNotNull(photoBytes);
        assertArrayEquals(testData, photoBytes);
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
    void addPhotoToVet_multipart_upload_succeeds() throws Exception {
        byte[] bytes = new byte[]{12, 24, 52, 87};

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = factory.wrap(bytes);

        FilePart filePart = Mockito.mock(FilePart.class);
        Mockito.when(filePart.name()).thenReturn("file");
        Mockito.when(filePart.filename()).thenReturn("photo.jpg");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        Mockito.when(filePart.headers()).thenReturn(headers);
        Mockito.when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        // Mock the response with PhotoResponseDTO
        PhotoResponseDTO responseDTO = PhotoResponseDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .filename("photo.jpg")
                .imgType("image/jpeg")
                .resource(bytes)
                .build();

        String jsonResponse = "{" +
                "\"vetId\": \"" + responseDTO.getVetId() + "\"," +
                "\"filename\": \"" + responseDTO.getFilename() + "\"," +
                "\"imgType\": \"" + responseDTO.getImgType() + "\"," +
                "\"resourceBase64\": \"" + Base64.getEncoder().encodeToString(bytes) + "\"" +
                "}";

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        Resource photo = vetsServiceClient
                .addPhotoToVet("deb1950c-3c56-45dc-874b-89e352695eb7", "photo.jpg", filePart)
                .block();

        assertNotNull(photo);
        byte[] returned = FileCopyUtils.copyToByteArray(photo.getInputStream());
        assertArrayEquals(bytes, returned);

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType);
        assertEquals("application/json", contentType);

        // The request body should contain the PhotoRequestDTO as JSON
        byte[] requestBody = req.getBody().readByteArray();
        assertNotNull(requestBody);
    }

    @Test
    void updatePhotoByValidVetId_shouldSucceed() throws IOException {
        byte[] testData = new byte[]{12, 24, 52, 87};
        Mono<Resource> photoResource = Mono.just(new ByteArrayResource(testData));

        PhotoResponseDTO photoResponseDTO = PhotoResponseDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .filename("image.jpeg")
                .imgType("image/jpeg")
                .resource(testData)
                .build();

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"vetId\": \"" + photoResponseDTO.getVetId() + "\"," +
                        "\"filename\": \"" + photoResponseDTO.getFilename() + "\"," +
                        "\"imgType\": \"" + photoResponseDTO.getImgType() + "\"," +
                        "\"resourceBase64\": \"" + Base64.getEncoder().encodeToString(testData) + "\"" +
                        "}"));

        final Resource photo = vetsServiceClient.updatePhotoOfVet("deb1950c-3c56-45dc-874b-89e352695eb7", "image.jpeg", photoResource).block();
        byte[] photoBytes = FileCopyUtils.copyToByteArray(photo.getInputStream());

        assertNotNull(photoBytes);
        assertArrayEquals(testData, photoBytes);
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
    public void testGetAllEducationsByVetId_NotFound() {
        String validVetId = "678910";

        // Prepare mock 404 response
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: " + validVetId));

        // Simulate the service call and handle the exception in the same way as in your method
        final EducationResponseDTO education = vetsServiceClient.getEducationsByVetId(validVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof NotFoundException && throwable.getMessage().equals("Education not found for vetId: " + validVetId)) {
                        return Mono.empty();  // Handle not found scenario
                    } else {
                        return Mono.error(throwable);  // Propagate other errors
                    }
                })
                .blockFirst();  // blockFirst() used since it's a reactive stream

        // Assert that the result is null because the vetId wasn't found
        assertNull(education, "The education should be null when vetId is not found");
    }

    @Test
    public void getAllEducationsOfVetId_ClientError() throws IllegalArgumentException {
        String validVetId="678910";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Something went wrong with the client"));

        final EducationResponseDTO education = vetsServiceClient.getEducationsByVetId(validVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the client")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .blockFirst();

        assertNull(education);
    }
    @Test
    public void getAllEducationsOfVetId_ServerError() throws IllegalArgumentException {
        String validVetId="678910";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong with the server"));

        final EducationResponseDTO education = vetsServiceClient.getEducationsByVetId(validVetId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong with the server")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .blockFirst();

        assertNull(education);
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
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteSpecialtyBySpecialtyId(vetId, specialtyId);

        StepVerifier.create(result)
                .verifyComplete();

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
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteSpecialtyBySpecialtyId(vetId, specialtyId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("Vet not found: " + vetId))
                .verify();

        // Validating the request was sent correctly
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/deb1950c-3c56-45dc-874b-89e352695eb7/specialties/794ac37f-1e07-43c2-93bc-61839e61d9890000", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void addPhotoToVetFromBytes_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO mockResponse = PhotoResponseDTO.builder()
                .vetId(vetId)
                .filename(photoName)
                .imgType("image/jpeg")
                .resource(photoData)
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(mockResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/photos", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void addPhotoToVetFromBytes_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "invalid-vet-id";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Photo for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error"))
                .verify();
    }

    @Test
    void getVetByVetBillId_shouldSucceed() throws JsonProcessingException, InterruptedException {
        String vetBillId = "bill-123";
        VetResponseDTO expectedResponse = buildVetResponseDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetBillId(vetBillId);

        StepVerifier.create(result)
                .assertNext(vet -> {
                    assertThat(vet).isNotNull();
                    assertThat(vet.getVetId()).isEqualTo(expectedResponse.getVetId());
                    assertThat(vet.getFirstName()).isEqualTo(expectedResponse.getFirstName());
                    assertThat(vet.getLastName()).isEqualTo(expectedResponse.getLastName());
                })
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/vetBillId/" + vetBillId, recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void getVetByVetBillId_shouldThrowNotFoundException_when404() throws Exception {
        String vetBillId = "invalid-bill-id";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetBillId(vetBillId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("vet with this vetBillId not found: " + vetBillId))
                .verify();
    }

    @Test
    void getVetByVetBillId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetBillId = "bill-123";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetBillId(vetBillId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void deletePhotoByVetId_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(204)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deletePhotoByVetId(vetId);

        StepVerifier.create(result)
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/photo", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void deletePhotoByVetId_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "invalid-vet-id";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deletePhotoByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().equals("Photo not found for vetId: " + vetId))
                .verify();
    }

    @Test
    void deletePhotoByVetId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deletePhotoByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error occurred while deleting photo for vetId: " + vetId))
                .verify();
    }

    @Test
    void deleteAlbumPhotoById_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        Integer photoId = 123;

        server.enqueue(new MockResponse()
                .setResponseCode(204)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteAlbumPhotoById(vetId, photoId);

        StepVerifier.create(result)
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/albums/" + photoId, recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void deleteAlbumPhotoById_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "invalid-vet-id";
        Integer photoId = 123;

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteAlbumPhotoById(vetId, photoId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Album photo not found: " + photoId))
                .verify();
    }

    @Test
    void deleteAlbumPhotoById_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        Integer photoId = 123;

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteAlbumPhotoById(vetId, photoId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error occurred while deleting album photo with ID: " + photoId))
                .verify();
    }

    @Test
    void getAllAlbumsByVetId_shouldSucceed() throws JsonProcessingException, InterruptedException {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        
        Album album1 = Album.builder()
                .id(1)
                .vetId(vetId)
                .filename("photo1.jpg")
                .imgType("image/jpeg")
                .data("photo1data".getBytes())
                .build();
        
        Album album2 = Album.builder()
                .id(2)
                .vetId(vetId)
                .filename("photo2.jpg")
                .imgType("image/jpeg")
                .data("photo2data".getBytes())
                .build();
        
        List<Album> albums = Arrays.asList(album1, album2);

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(albums)));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .assertNext(album -> {
                    assertThat(album).isNotNull();
                    assertThat(album.getId()).isEqualTo(1);
                    assertThat(album.getVetId()).isEqualTo(vetId);
                    assertThat(album.getFilename()).isEqualTo("photo1.jpg");
                })
                .assertNext(album -> {
                    assertThat(album).isNotNull();
                    assertThat(album.getId()).isEqualTo(2);
                    assertThat(album.getVetId()).isEqualTo(vetId);
                    assertThat(album.getFilename()).isEqualTo("photo2.jpg");
                })
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/albums", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void getAllAlbumsByVetId_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "invalid-vet-id";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("Albums for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void getAllAlbumsByVetId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Flux<Album> result = vetsServiceClient.getAllAlbumsByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error"))
                .verify();
    }

    @Test
    void addVet_shouldSucceed() throws JsonProcessingException, InterruptedException {
        VetRequestDTO requestDTO = buildVetRequestDTO();
        VetResponseDTO expectedResponse = buildVetResponseDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.addVet(Mono.just(requestDTO));

        StepVerifier.create(result)
                .assertNext(vet -> {
                    assertThat(vet).isNotNull();
                    assertThat(vet.getVetId()).isEqualTo(expectedResponse.getVetId());
                    assertThat(vet.getFirstName()).isEqualTo(expectedResponse.getFirstName());
                    assertThat(vet.getLastName()).isEqualTo(expectedResponse.getLastName());
                })
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void addVet_shouldThrowIllegalArgumentException_when400() throws Exception {
        VetRequestDTO requestDTO = buildVetRequestDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.addVet(Mono.just(requestDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong"))
                .verify();
    }

    @Test
    void addVet_shouldThrowIllegalArgumentException_when500() throws Exception {
        VetRequestDTO requestDTO = buildVetRequestDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.addVet(Mono.just(requestDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void addSpecialtiesByVetId_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        SpecialtyDTO specialtyDTO = new SpecialtyDTO();
        specialtyDTO.setSpecialtyId("specialty-123");
        specialtyDTO.setName("Test Specialty");
        
        VetResponseDTO expectedResponse = buildVetResponseDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.addSpecialtiesByVetId(vetId, Mono.just(specialtyDTO));

        StepVerifier.create(result)
                .assertNext(vet -> {
                    assertThat(vet).isNotNull();
                    assertThat(vet.getVetId()).isEqualTo(expectedResponse.getVetId());
                })
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/" + vetId + "/specialties", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void addSpecialtiesByVetId_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        SpecialtyDTO specialtyDTO = new SpecialtyDTO();
        specialtyDTO.setSpecialtyId("specialty-123");
        specialtyDTO.setName("Test Specialty");

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.addSpecialtiesByVetId(vetId, Mono.just(specialtyDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("Vet not found: " + vetId))
                .verify();
    }

    @Test
    void addSpecialtiesByVetId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        SpecialtyDTO specialtyDTO = new SpecialtyDTO();
        specialtyDTO.setSpecialtyId("specialty-123");
        specialtyDTO.setName("Test Specialty");

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.addSpecialtiesByVetId(vetId, Mono.just(specialtyDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void getPhotoByVetId_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.getPhotoByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("Photo for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void getPhotoByVetId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.getPhotoByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }




    @Test
    void addPhotoToVetFromBytes_withNullFilename_shouldUseDefaultContentType() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = null;
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename("default.jpg");
        expectedResponse.setImgType("image/jpeg");
        expectedResponse.setResource(photoData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }

    @Test
    void addPhotoToVetFromBytes_withGifExtension_shouldUseGifContentType() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.gif";
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename(photoName);
        expectedResponse.setImgType("image/gif");
        expectedResponse.setResource(photoData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }

    @Test
    void addPhotoToVetFromBytes_withUnknownExtension_shouldUseDefaultContentType() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.xyz";
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename(photoName);
        expectedResponse.setImgType("image/jpeg");
        expectedResponse.setResource(photoData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }
   

    @Test
    void updateVet_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        
        VetRequestDTO vetRequest = VetRequestDTO.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phoneNumber("514-123-4567")
                .active(true)
                .build();
        
        VetResponseDTO expectedResponse = VetResponseDTO.builder()
                .vetId(vetId)
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phoneNumber("514-123-4567")
                .active(true)
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.updateVet(vetId, Mono.just(vetRequest));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getVetId()).isEqualTo(vetId);
                    assertThat(response.getFirstName()).isEqualTo("John");
                    assertThat(response.getLastName()).isEqualTo("Smith");
                    assertThat(response.getEmail()).isEqualTo("john.smith@example.com");
                    assertThat(response.getPhoneNumber()).isEqualTo("514-123-4567");
                    assertThat(response.isActive()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void updateVet_shouldThrowExistingVetNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        
        VetRequestDTO vetRequest = VetRequestDTO.builder()
                .firstName("John")
                .lastName("Smith")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.updateVet(vetId, Mono.just(vetRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("vetId not found: " + vetId))
                .verify();
    }

    @Test
    void updateVet_shouldThrowIllegalArgumentException_when400() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        
        VetRequestDTO vetRequest = VetRequestDTO.builder()
                .firstName("John")
                .lastName("Smith")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.updateVet(vetId, Mono.just(vetRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the client"))
                .verify();
    }

    @Test
    void updateVet_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        
        VetRequestDTO vetRequest = VetRequestDTO.builder()
                .firstName("John")
                .lastName("Smith")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.updateVet(vetId, Mono.just(vetRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void updateEducationByVetIdAndByEducationId_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String educationId = "ed123456";

        EducationRequestDTO educationRequest = EducationRequestDTO.builder()
                .vetId(vetId)
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

        EducationResponseDTO expectedResponse = EducationResponseDTO.builder()
                .educationId(educationId)
                .vetId(vetId)
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<EducationResponseDTO> result = vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, Mono.just(educationRequest));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getEducationId()).isEqualTo(educationId);
                    assertThat(response.getVetId()).isEqualTo(vetId);
                    assertThat(response.getSchoolName()).isEqualTo("Harvard University");
                    assertThat(response.getDegree()).isEqualTo("Doctor of Veterinary Medicine");
                    assertThat(response.getFieldOfStudy()).isEqualTo("Veterinary Medicine");
                    assertThat(response.getStartDate()).isEqualTo("2015");
                    assertThat(response.getEndDate()).isEqualTo("2019");
                })
                .verifyComplete();
    }

    @Test
    void updateEducationByVetIdAndByEducationId_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        String educationId = "nonexistent-education";

        EducationRequestDTO educationRequest = EducationRequestDTO.builder()
                .vetId(vetId)
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<EducationResponseDTO> result = vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, Mono.just(educationRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Education not found for vetId: " + vetId + " and educationId: " + educationId))
                .verify();
    }

    @Test
    void updateEducationByVetIdAndByEducationId_shouldThrowIllegalArgumentException_when400() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String educationId = "ed123456";

        EducationRequestDTO educationRequest = EducationRequestDTO.builder()
                .vetId(vetId)
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Mono<EducationResponseDTO> result = vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, Mono.just(educationRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the client"))
                .verify();
    }

    @Test
    void updateEducationByVetIdAndByEducationId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String educationId = "ed123456";

        EducationRequestDTO educationRequest = EducationRequestDTO.builder()
                .vetId(vetId)
                .schoolName("Harvard University")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .startDate("2015")
                .endDate("2019")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<EducationResponseDTO> result = vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, Mono.just(educationRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_comprehensive_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename(photoName);
        expectedResponse.setImgType("image/jpeg");
        expectedResponse.setResource(photoData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }

    @Test
    void addPhotoToVetFromBytes_withBase64Response_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();
        String base64Data = java.util.Base64.getEncoder().encodeToString(photoData);

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename(photoName);
        expectedResponse.setImgType("image/jpeg");
        expectedResponse.setResourceBase64(base64Data);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }

    @Test
    void addPhotoToVetFromBytes_withNullPhotoName_comprehensive_shouldUseDefaultContentType() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = null;
        byte[] photoData = "test photo data".getBytes();

        PhotoResponseDTO expectedResponse = new PhotoResponseDTO();
        expectedResponse.setVetId(vetId);
        expectedResponse.setFilename("default.jpg");
        expectedResponse.setImgType("image/jpeg");
        expectedResponse.setResource(photoData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .assertNext(resource -> {
                    assertThat(resource).isNotNull();
                    assertThat(resource).isInstanceOf(ByteArrayResource.class);
                })
                .verifyComplete();
    }

    @Test
    void addPhotoToVetFromBytes_comprehensive_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Photo for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_comprehensive_shouldThrowIllegalArgumentException_when400() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Client error"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_comprehensive_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error"))
                .verify();
    }

    @Test
    void updateRatingByVetIdAndByRatingId_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String ratingId = "rating-123";
        
        RatingRequestDTO ratingRequest = RatingRequestDTO.builder()
                .vetId(vetId)
                .rateScore(4.0)
                .rateDescription("Good service")
                .rateDate("2024-10-02")
                .customerName("John Doe")
                .build();

        RatingResponseDTO expectedResponse = RatingResponseDTO.builder()
                .ratingId(ratingId)
                .vetId(vetId)
                .rateScore(4.0)
                .rateDescription("Good service")
                .rateDate("2024-10-02")
                .customerName("John Doe")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<RatingResponseDTO> result = vetsServiceClient.updateRatingByVetIdAndByRatingId(
                vetId, ratingId, Mono.just(ratingRequest));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getRatingId()).isEqualTo(ratingId);
                    assertThat(response.getVetId()).isEqualTo(vetId);
                    assertThat(response.getRateScore()).isEqualTo(4.0);
                })
                .verifyComplete();
    }

   

    @Test
    void getVetByVetId_shouldSucceed() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        
        VetResponseDTO expectedResponse = VetResponseDTO.builder()
                .vetId(vetId)
                .vetBillId("vet-bill-123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("123-456-7890")
                .resume("Experienced veterinarian")
                .active(true)
                .workday(Set.of())
                .workHoursJson("{}")
                .specialties(Set.of())
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getVetId()).isEqualTo(vetId);
                    assertThat(response.getFirstName()).isEqualTo("John");
                    assertThat(response.getLastName()).isEqualTo("Doe");
                    assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
                })
                .verifyComplete();
    }

    @Test
    void getVetByVetId_shouldThrowExistingVetNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("vetId not found: " + vetId))
                .verify();
    }

    @Test
    void getVetByVetId_shouldThrowIllegalArgumentException_when400() throws Exception {
        String vetId = "invalid-vet-id";

        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the client"))
                .verify();
    }

    @Test
    void getVetByVetId_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    // Tests for highest value methods to reach 90%+ coverage
    @Test
    void createVet_successTest() throws JsonProcessingException {
        VetRequestDTO vetRequest = buildVetRequestDTO();
        VetResponseDTO expectedResponse = buildVetResponseDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<VetResponseDTO> result = vetsServiceClient.createVet(Mono.just(vetRequest));

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getVetId()).isEqualTo(expectedResponse.getVetId());
                    assertThat(response.getFirstName()).isEqualTo(expectedResponse.getFirstName());
                    assertThat(response.getLastName()).isEqualTo(expectedResponse.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void deleteVet_successTest() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";

        server.enqueue(new MockResponse()
                .setResponseCode(204));

        Mono<Void> result = vetsServiceClient.deleteVet(vetId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    // Additional tests to cover red (0% coverage) methods
    @Test
    void getVets_shouldThrowExistingVetNotFoundException_when404() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<VetResponseDTO> result = vetsServiceClient.getVets();

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("No vets found"))
                .verify();
    }

    @Test
    void getActiveVets_shouldThrowExistingVetNotFoundException_when404() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<VetResponseDTO> result = vetsServiceClient.getActiveVets();

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("No active vets found"))
                .verify();
    }

    @Test
    void getInactiveVets_shouldThrowExistingVetNotFoundException_when404() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<VetResponseDTO> result = vetsServiceClient.getInactiveVets();

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("No inactive vets found"))
                .verify();
    }

    @Test
    void getRatingsOfAVetBasedOnDate_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        Map<String, String> queryParams = Map.of("year", "2023");
        
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<RatingResponseDTO> result = vetsServiceClient.getRatingsOfAVetBasedOnDate(vetId, queryParams);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("No ratings found for vetId: " + vetId))
                .verify();
    }

    @Test
    void getTopThreeVetsWithHighestAverageRating_shouldThrowNotFoundException_when404() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Flux<VetAverageRatingDTO> result = vetsServiceClient.getTopThreeVetsWithHighestAverageRating();

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("No vets found"))
                .verify();
    }

    @Test
    void addAlbumPhotoFromBytes_shouldSucceed() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        Album expectedAlbum = new Album(1, vetId, photoName, "image/jpeg", fileData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedAlbum)));

        Mono<Album> result = vetsServiceClient.addAlbumPhotoFromBytes(vetId, photoName, fileData);

        StepVerifier.create(result)
                .expectNext(expectedAlbum)
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/" + vetId + "/albums/photos/" + photoName, recordedRequest.getPath());
        assertEquals("application/octet-stream", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    void addAlbumPhotoFromBytes_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Album> result = vetsServiceClient.addAlbumPhotoFromBytes(vetId, photoName, fileData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Album source not found for vet " + vetId))
                .verify();
    }

    @Test
    void addAlbumPhotoFromBytes_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Album> result = vetsServiceClient.addAlbumPhotoFromBytes(vetId, photoName, fileData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error while adding album photo"))
                .verify();
    }

    @Test
    void addAlbumPhoto_withFilePart_shouldSucceed() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileData);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        Album expectedAlbum = new Album(1, vetId, photoName, "image/jpeg", fileData);

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedAlbum)));

        Mono<Album> result = vetsServiceClient.addAlbumPhoto(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectNext(expectedAlbum)
                .verifyComplete();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/" + vetId + "/albums/photos/" + photoName, recordedRequest.getPath());
        assertEquals("application/octet-stream", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    void addAlbumPhoto_withFilePart_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileData);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Album> result = vetsServiceClient.addAlbumPhoto(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Album source not found for vet " + vetId))
                .verify();
    }

    @Test
    void addAlbumPhoto_withFilePart_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "album_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileData);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Album> result = vetsServiceClient.addAlbumPhoto(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Server error while adding album photo"))
                .verify();
    }

    @Test
    void createVet_shouldThrowNotFoundException_when404() throws Exception {
        VetRequestDTO vetRequestDTO = buildVetRequestDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.createVet(Mono.just(vetRequestDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ExistingVetNotFoundException &&
                                throwable.getMessage().contains("vetId not found"))
                .verify();
    }

    @Test
    void createVet_shouldThrowIllegalArgumentException_when500() throws Exception {
        VetRequestDTO vetRequestDTO = buildVetRequestDTO();

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<VetResponseDTO> result = vetsServiceClient.createVet(Mono.just(vetRequestDTO));

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Something went wrong with the server"))
                .verify();
    }

    @Test
    void addPhotoToVet_withFilePart_shouldThrowNotFoundException_when404() throws Exception {
        String vetId = "nonexistent-vet";
        String photoName = "test_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileData);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.IMAGE_JPEG);

        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVet(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Photo for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void addPhotoToVet_withFilePart_shouldThrowIllegalArgumentException_when500() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "test_photo.jpg";
        byte[] fileData = "mockImageData".getBytes();

        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(fileData);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.IMAGE_JPEG);

        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(""));

        Mono<Resource> result = vetsServiceClient.addPhotoToVet(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Server error"))
                .verify();
    }

    @Test
    void determineContentType_shouldReturnCorrectTypes() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":\"test.png\",\"imgType\":\"image/png\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, "test.png", "data".getBytes()).block();
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":\"test.jpg\",\"imgType\":\"image/jpeg\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, "test.jpg", "data".getBytes()).block();
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":\"test.jpeg\",\"imgType\":\"image/jpeg\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, "test.jpeg", "data".getBytes()).block();
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":\"test.gif\",\"imgType\":\"image/gif\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, "test.gif", "data".getBytes()).block();
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":\"test.unknown\",\"imgType\":\"application/octet-stream\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, "test.unknown", "data".getBytes()).block();
        
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"filename\":null,\"imgType\":\"application/octet-stream\"}"));
                
        vetsServiceClient.addPhotoToVetFromBytes(vetId, null, "data".getBytes()).block();
    }

    @Test
    void addAlbumPhoto_withMultipleDataBuffers_shouldCombineData() throws Exception {
        String vetId = "69f852ca-625b-11ee-8c99-0242ac120002";
        String photoName = "album_photo.jpg";
        
        byte[] data1 = "first".getBytes();
        byte[] data2 = "second".getBytes();
        
        FilePart filePart = Mockito.mock(FilePart.class);
        DataBuffer dataBuffer1 = new DefaultDataBufferFactory().wrap(data1);
        DataBuffer dataBuffer2 = new DefaultDataBufferFactory().wrap(data2);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer1, dataBuffer2));

        Album expectedAlbum = new Album(1, vetId, photoName, "image/jpeg", "firstsecond".getBytes());

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedAlbum)));

        Mono<Album> result = vetsServiceClient.addAlbumPhoto(vetId, photoName, filePart);

        StepVerifier.create(result)
                .expectNext(expectedAlbum)
                .verifyComplete();
    }

    @Test
    void deleteRatingByCustomerName_ValidId_ShouldSucceed() {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String customerName = "John Doe";

        prepareResponse(response -> response
                .setResponseCode(204)
                .setBody(""));

        Mono<Void> result = vetsServiceClient.deleteRatingByCustomerName(vetId, customerName);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteRatingByCustomerName_NotFound_ShouldThrowNotFoundException() {
        String vetId = "invalid-vet-id";
        String customerName = "John Doe";

        prepareResponse(response -> response
                .setResponseCode(404)
                .setBody("Not found"));

        Mono<Void> result = vetsServiceClient.deleteRatingByCustomerName(vetId, customerName);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("vetId not found " + vetId + " or no rating found for customer: " + customerName))
                .verify();
    }

    @Test
    void deleteRatingByCustomerName_ClientError_ShouldThrowIllegalArgumentException() {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String customerName = "John Doe";

        prepareResponse(response -> response
                .setResponseCode(400)
                .setBody("Bad request"));

        Mono<Void> result = vetsServiceClient.deleteRatingByCustomerName(vetId, customerName);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Something went wrong with the client"))
                .verify();
    }

    @Test
    void deleteRatingByCustomerName_ServerError_ShouldThrowIllegalArgumentException() {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String customerName = "John Doe";

        prepareResponse(response -> response
                .setResponseCode(500)
                .setBody("Internal server error"));

        Mono<Void> result = vetsServiceClient.deleteRatingByCustomerName(vetId, customerName);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Something went wrong with the server"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_NotFound_ShouldThrowNotFoundException() throws Exception {
        String vetId = "invalid-vet-id";
        String photoName = "test_photo.jpg";
        byte[] photoData = "mockImageData".getBytes();

        prepareResponse(response -> response
                .setResponseCode(404)
                .setBody("Photo for vet " + vetId + " not found"));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().contains("Photo for vet " + vetId + " not found"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_ClientError_ShouldThrowIllegalArgumentException() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test_photo.jpg";
        byte[] photoData = "mockImageData".getBytes();

        prepareResponse(response -> response
                .setResponseCode(400)
                .setBody("Bad request"));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Client error"))
                .verify();
    }

    @Test
    void addPhotoToVetFromBytes_ServerError_ShouldThrowIllegalArgumentException() throws Exception {
        String vetId = "deb1950c-3c56-45dc-874b-89e352695eb7";
        String photoName = "test_photo.jpg";
        byte[] photoData = "mockImageData".getBytes();

        prepareResponse(response -> response
                .setResponseCode(500)
                .setBody("Internal server error"));

        Mono<Resource> result = vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Server error"))
                .verify();
    }


}

