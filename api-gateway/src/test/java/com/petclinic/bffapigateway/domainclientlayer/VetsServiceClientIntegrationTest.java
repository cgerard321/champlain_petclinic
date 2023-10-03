package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
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
                .setBody("Something went wrong"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final RatingResponseDTO rating = vetsServiceClient.getRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                        "        \"vetId\": \"678910\",\n" +
                        "        \"averageRating\": 4.5\n" +
                        "    }"));

        final VetAverageRatingDTO averageRatingDTO = vetsServiceClient.getTopThreeVetsWithHighestAverageRating().blockFirst();
        assertEquals("678910",vetDTO.getVetId());
        assertEquals(4.5, averageRatingDTO.getAverageRating(),0.1);
        //its 0.0 because the vet doesn't have any ratings
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
                .setBody("Something went wrong"));

        final Integer ratingNumber = vetsServiceClient.getNumberOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final Integer ratingNumber = vetsServiceClient.getNumberOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").onErrorResume(throwable -> {
            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                vetsServiceClient.getAverageRatingByVetId(vetDTO.getVetId())
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
                .setBody("Something went wrong"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetDTO.getVetId())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final Double averageRating =
                vetsServiceClient.getAverageRatingByVetId(vetDTO.getVetId())
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final String ratingPercentages =
                vetsServiceClient.getPercentageOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final String ratingPercentages =
                vetsServiceClient.getPercentageOfRatingsByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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

        final Mono<Void> empty = vetsServiceClient.deleteRating(vetDTO.getVetId(), ratingId);

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
                .setBody("Something went wrong"));

        final Void empty = vetsServiceClient.deleteRating(validVetId, validRatingId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final Void empty = vetsServiceClient.deleteRating(validVetId, validRatingId)
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet(validVetId, Mono.just(ratingRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final RatingResponseDTO rating = vetsServiceClient.addRatingToVet(validVetId, Mono.just(ratingRequestDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating))
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final RatingResponseDTO ratingResponseDTO =
                vetsServiceClient.updateRatingByVetIdAndByRatingId("deb1950c-3c56-45dc-874b-89e352695eb7","123456", Mono.just(updatedRating))
                        .onErrorResume(throwable -> {
                            if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
    void getAllVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.getVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
    void getActiveVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.getActiveVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
    void getInactiveVets_IllegalArgumentException500() throws IllegalArgumentException {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.getInactiveVets()
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7").block();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void getVetByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final VetDTO vet = vetsServiceClient.getVetByVetId(invalidVetId)
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
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.getVetByVetId("deb1950c-3c56-45dc-874b-89e352695eb7")
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
    void createVet_IllegalArgumentException500() throws IllegalArgumentException{
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.createVet(Mono.just(vetDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                        "        \"workday\": \"Monday\",\n" +
                        "        \"active\": false\n" +
                        "    }"));

        final VetDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetDTO)).block();
        assertFalse(vet.isActive());
        assertEquals(vetDTO.getFirstName(), vet.getFirstName());
        assertEquals(vetDTO.getLastName(), vet.getLastName());
        assertEquals(vetDTO.getEmail(), vet.getEmail());
        assertEquals(vetDTO.getPhoneNumber(), vet.getPhoneNumber());
        assertEquals(vetDTO.getResume(), vet.getResume());
        assertEquals(vetDTO.getWorkday(), vet.getWorkday());
    }

    @Test
    void updateVetByInvalidVetId_shouldNotSucceed() throws ExistingVetNotFoundException{
        String invalidVetId="123";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("vetId not found: "+invalidVetId));

        final VetDTO vet = vetsServiceClient.updateVet(invalidVetId, Mono.just(vetDTO))
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
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final VetDTO vet = vetsServiceClient.updateVet("deb1950c-3c56-45dc-874b-89e352695eb7", Mono.just(vetDTO))
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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

        final Mono<Void> empty = vetsServiceClient.deleteVet(vetDTO.getVetId());

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
                .setBody("Something went wrong"));

        final Void empty = vetsServiceClient.deleteVet(vetDTO.getVetId())
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
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
                .setBody("Something went wrong"));

        final Void empty = vetsServiceClient.deleteVet(vetDTO.getVetId())
                .onErrorResume(throwable -> {
                    if (throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Something went wrong")) {
                        return Mono.empty();
                    } else {
                        return Mono.error(throwable);
                    }
                })
                .block();

        assertNull(empty);
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


        final Mono<Void> empty = vetsServiceClient.deleteEducation(vetDTO.getVetId(), educationId);

        assertEquals(empty.block(), null);
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

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("deb1950c-3c56-45dc-874b-89e352695eb7")
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