package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.*;
import com.petclinic.vet.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
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
    RatingRequestDTO ratingRequestDTO = buildRatingRequestDTO();

    VetAverageRatingDTO vetAverageRatingDTO1=buildVetAverageRatingDTO();
    @Test
    void getAllRatingsByVetId() {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating));

        Flux<RatingResponseDTO> ratingResponseDTO = ratingService.getAllRatingsByVetId("vetId");

        StepVerifier
                .create(ratingResponseDTO)
                .consumeNextWith(foundRating -> {
                    assertEquals(rating.getRatingId(), foundRating.getRatingId());
                    assertEquals(rating.getVetId(), foundRating.getVetId());
                    assertEquals(rating.getRateScore(), foundRating.getRateScore());
                })

                .verifyComplete();
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


//    @Test
//    void getTopThreeVetsWithHighestRating() {
//
//
//        rating.setRateScore(4.0);
//        rating.setVetId("68790");
//
//        when(ratingRepository.findAll()).thenReturn(Flux.just(rating));
//
//        Flux<VetAverageRatingDTO> averageRatingDTOFlux = ratingService.getTopThreeVetsWithHighestAverageRating();
//
//        StepVerifier
//                .create(averageRatingDTOFlux)
//                .consumeNextWith(highestRating -> {
//                    assertEquals("68790", highestRating.getVetId()); // Vet ID should match
//                    assertEquals(4.0, highestRating.getAverageRating(),0.01); // Correct field name
//                });
//    }

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
                .workday("Monday")
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