package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.businesslayer.photos.PhotoService;
import com.petclinic.vet.businesslayer.ratings.RatingService;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;
import com.petclinic.vet.presentationlayer.vets.VetAverageRatingDTO;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class RatingServiceImplTest {

    @Autowired
    RatingService ratingService;
    @Autowired
    PhotoService photoService;

    @MockBean
    RatingRepository ratingRepository;
    @MockBean
    VetRepository vetRepository;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    Vet existingVet=buildVet();

    @MockBean
    ObjectMapper objectMapper;

    String VET_ID = "vetId";
    Rating rating = buildRating();
    Rating rating2 = buildRating2();
    Rating rating3 = buildRating3();
    RatingRequestDTO ratingRequestDTO = buildRatingRequestDTO();

    VetAverageRatingDTO vetAverageRatingDTO1=buildVetAverageRatingDTO();
    @Test
    void getAllRatingsByVetId_ShouldReturnFormattedRatings() {
        String expectedVetId = "vetId";
        String expectedRatingId = "ratingId";
        Double expectedRateScore = 5.0;
        String expectedRateDescription = "Great experience!";
        PredefinedDescription predefinedDescription = PredefinedDescription.EXCELLENT;
        String expectedCustomerName = "John Doe";
        String expectedRateDate = "2023-09-01";

        Rating rating = Rating.builder()
                .ratingId(expectedRatingId)
                .vetId(expectedVetId)
                .rateScore(expectedRateScore)
                .rateDescription(expectedRateDescription)
                .predefinedDescription(predefinedDescription)
                .rateDate(expectedRateDate)
                .customerName(expectedCustomerName)
                .build();

        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));

        Flux<RatingResponseDTO> ratingResponseDTO = ratingService.getAllRatingsByVetId(expectedVetId);

        StepVerifier.create(ratingResponseDTO)
                .consumeNextWith(foundRating -> {
                    assertEquals(expectedRatingId, foundRating.getRatingId());
                    assertEquals(expectedVetId, foundRating.getVetId());
                    assertEquals(expectedRateScore, foundRating.getRateScore());
                    assertEquals(expectedRateDescription, foundRating.getRateDescription());
                    assertEquals(predefinedDescription, foundRating.getPredefinedDescription());
                    assertEquals(expectedRateDate, foundRating.getRateDate());  // Verify the date format matches
                    assertEquals(expectedCustomerName, foundRating.getCustomerName());
                })
                .verifyComplete();
    }

    @Test
    void getAllRatingsByVetId_WithInvalidVetId_ShouldReturnNotFound() {

        String invalidVetId = "invalidVetId";

        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.empty());

        Flux<RatingResponseDTO> ratingResponseDTO = ratingService.getAllRatingsByVetId(invalidVetId);

        StepVerifier.create(ratingResponseDTO)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("vetId not found: " + invalidVetId))
                .verify();
    }


    @Test
    void deleteRatingByRatingId() {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findByVetIdAndRatingId(anyString(), anyString())).thenReturn(Mono.just(rating));
        when(ratingRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedRating = ratingService.deleteRatingByRatingId(rating.getVetId(), rating.getRatingId());

        StepVerifier
                .create(deletedRating)
                .verifyComplete();
    }

    @Test
    void deleteRatingByVetIdAndCustomerName_ShouldSucceed() {
        String customerName = "Test Customer";
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));
        when(ratingRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedRating = ratingService.deleteRatingByVetIdAndCustomerName(rating.getVetId(), customerName);

        StepVerifier
                .create(deletedRating)
                .verifyComplete();
    }

    @Test
    void deleteRatingByVetIdAndCustomerName_WithInvalidVetId_ShouldReturnNotFound() {
        String invalidVetId = "invalidVetId";
        String customerName = "Test Customer";

        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.empty());
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.empty());

        Mono<Void> deletedRating = ratingService.deleteRatingByVetIdAndCustomerName(invalidVetId, customerName);

        StepVerifier
                .create(deletedRating)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("vetId not found: " + invalidVetId))
                .verify();
    }

    @Test
    void deleteRatingByVetIdAndCustomerName_WithNoMatchingCustomer_ShouldReturnNotFound() {
        String customerName = "Non Existent Customer";
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.empty());

        Mono<Void> deletedRating = ratingService.deleteRatingByVetIdAndCustomerName(rating.getVetId(), customerName);

        StepVerifier
                .create(deletedRating)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No rating found for customer: " + customerName))
                .verify();
    }

    @Test
    void addRatingToVet() {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));

        ratingService.addRatingToVet(rating.getVetId(), Mono.just(ratingRequestDTO))
                .map(ratingResponseDTO -> {
                    assertEquals(ratingResponseDTO.getVetId(), ratingRequestDTO.getVetId());
                    assertEquals(ratingResponseDTO.getRateScore(), ratingRequestDTO.getRateScore());
                    assertNotNull(ratingResponseDTO.getRatingId());
                    return ratingResponseDTO;
                });
    }

    @Test
    void updateRatingOfVet(){
        when(ratingRepository.save(any())).thenReturn(Mono.just(rating));
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findByVetIdAndRatingId(anyString(), anyString())).thenReturn(Mono.just(rating));

        Mono<RatingResponseDTO> ratingResponseDTO=ratingService.updateRatingByVetIdAndRatingId(existingVet.getVetId(), rating.getRatingId(), Mono.just(ratingRequestDTO));

        StepVerifier
                .create(ratingResponseDTO)
                .consumeNextWith(existingRating -> {
                    assertNotNull(rating.getId());
                    assertEquals(rating.getRatingId(), existingRating.getRatingId());
                    assertEquals(rating.getVetId(), existingRating.getVetId());
                    assertEquals(rating.getRateScore(), existingRating.getRateScore());
                    assertEquals(rating.getRateDate(), existingRating.getRateDate());
                    assertEquals(rating.getRateDescription(), existingRating.getRateDescription());
                })
                .verifyComplete();
    }

    @Test
    void getNumberOfRatingsByVetId() {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(1L));

        Mono<Integer> numberOfRatings = ratingService.getNumberOfRatingsByVetId(rating.getVetId());

        StepVerifier
                .create(numberOfRatings)
                .consumeNextWith(foundRating -> {
                    assertEquals(1, foundRating);
                })

                .verifyComplete();
    }

    @Test
    void getAverageRatingByVetId() {
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(1L));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));

        Mono<Double> averageRating = ratingService.getAverageRatingByVetId(rating.getVetId());

        StepVerifier
                .create(averageRating)
                .consumeNextWith(foundRating -> {
                    assertEquals(5.0, foundRating);
                })

                .verifyComplete();
    }


    @Test
    void getTopThreeVetsWithHighestRating() {

        rating.setRateScore(4.0);
        rating.setVetId("68790");
        rating2.setRateScore(1.0);
        rating2.setVetId("68792");
        rating3.setRateScore(2.0);
        rating3.setVetId("68793");

        Vet vet1 = Vet.builder()
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        Vet vet2 = Vet.builder()
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
        Vet vet3 = Vet.builder()
                .id("2")
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();


        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(5L));
        when(vetRepository.findVetByVetId(rating.getVetId())).thenReturn(Mono.just(vet1));
        when(vetRepository.findVetByVetId(rating2.getVetId())).thenReturn(Mono.just(vet2));
        when(vetRepository.findVetByVetId(rating3.getVetId())).thenReturn(Mono.just(vet3));



        when(ratingRepository.findAll()).thenReturn(Flux.just(rating,rating2,rating3));

        when(ratingRepository.findAllByVetId(rating.getVetId())).thenReturn(Flux.just(rating));
        when(ratingRepository.findAllByVetId(rating2.getVetId())).thenReturn(Flux.just(rating2));
        when(ratingRepository.findAllByVetId(rating3.getVetId())).thenReturn(Flux.just(rating3));


        Flux<VetAverageRatingDTO> averageRatingDTOFlux = ratingService.getTopThreeVetsWithHighestAverageRating();

        StepVerifier
                .create(averageRatingDTOFlux)
                .expectNextCount(3)
                .verifyComplete();

    }
    @Test
    void getRatingBasedOnDate()throws JsonProcessingException{
        rating.setRateScore(4.0);
        rating.setDate("2021");
        rating.setVetId("68790");
        rating2.setRateScore(1.0);
        rating2.setVetId("68792");
        rating2.setDate("2022");
        rating3.setRateScore(2.0);
        rating3.setVetId("68793");
        rating3.setDate("2023");


        Vet vet1 = Vet.builder()
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();



        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("year", "2023");

        when(vetRepository.findVetByVetId(vet1.getVetId())).thenReturn(Mono.just(vet1));

        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating, rating2, rating3));

        Flux<RatingResponseDTO> ratingResponseDTOFlux = ratingService.getRatingsOfAVetBasedOnDate(vet1.getVetId(),queryParams);

        StepVerifier
                .create(ratingResponseDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getRatingPercentagesByVetId() throws JsonProcessingException {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}");
        Mono<String> ratingPercent = ratingService.getRatingPercentagesByVetId(rating.getVetId());

        StepVerifier
                .create(ratingPercent)
                .consumeNextWith(foundRating -> {
                    assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}", foundRating);
                })

                .verifyComplete();
    }

    // get rating percentage error handling test
    @Test
    void getRatingPercentagesByVetIdError() throws JsonProcessingException {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.empty());
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
        Mono<String> ratingPercent = ratingService.getRatingPercentagesByVetId(rating.getVetId());

        StepVerifier
                .create(ratingPercent)
                .expectError(RuntimeException.class)
                .verify();
    }

    private Rating buildRating() {
        return Rating.builder()
                .id("1")
                .ratingId("ratingId")
                .vetId("vetId")
                .rateScore(5.0)
                .rateDescription("Vet is the best vet in the wooooorld!")
                .rateDate("16/09/2023")
                .customerName("Test Customer")
                .build();
    }
    private Rating buildRating2() {
        return Rating.builder()
                .id("2")
                .ratingId("ratingId2")
                .vetId("vetId2")
                .rateScore(1.0)
                .rateDescription("Vet is the worst vet in the wooooorld!")
                .rateDate("10/08/2023")
                .build();
    }
    private Rating buildRating3() {
        return Rating.builder()
                .id("3")
                .ratingId("ratingId3")
                .vetId("vetId3")
                .rateScore(2.0)
                .rateDescription("Vet is the almost worst vet in the wooooorld!")
                .rateDate("05/18/2023")
                .build();
    }
    private RatingRequestDTO buildRatingRequestDTO() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId("vetId")
                .rateScore(5.0)
                .build();
        return ratingRequestDTO;
    }

    private Vet buildVet() {
        return Vet.builder()
                .id("1")
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    private VetAverageRatingDTO buildVetAverageRatingDTO(){
        return VetAverageRatingDTO.builder()
                .averageRating(5.0)
                .vetId("57385").build();
    }
}