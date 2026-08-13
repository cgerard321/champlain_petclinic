package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.badges.BadgeService;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.badges.BadgeResponseDTO;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BadgeServiceImplTest {
    @Autowired
    BadgeService badgeService;
    @MockBean
    BadgeRepository badgeRepository;
    @MockBean
    RatingRepository ratingRepository;
    @MockBean
    FilesServiceClient filesServiceClient;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String VET_ID="123456";
    ClassPathResource cpr=new ClassPathResource("images/full_food_bowl.png");
    ClassPathResource cpr2=new ClassPathResource("images/half-full_food_bowl.png");
    ClassPathResource cpr3=new ClassPathResource("images/empty_food_bowl.png");

    Badge badge1=buildBadge(BadgeTitle.HIGHLY_RESPECTED, cpr);
    Badge badge2=buildBadge(BadgeTitle.MUCH_APPRECIATED, cpr2);
    Badge badge3=buildBadge(BadgeTitle.VALUED, cpr3);

    BadgeServiceImplTest() throws IOException {}

    @Test
    void getBadgeByValidVetId(){
        Rating rating1 = buildRating("12346", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 4.0);
        Rating rating2 = buildRating("12347", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 5.0);

        when(badgeRepository.findByVetId(anyString())).thenReturn(Mono.just(badge1));
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(2L));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating1, rating2));

        Mono<BadgeResponseDTO> badgeResponseDTO=badgeService.getBadgeByVetId(VET_ID);

        StepVerifier
                .create(badgeResponseDTO)
                .consumeNextWith(responseDTO->{
                    assertNotNull(responseDTO);
                    assertEquals(badge1.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badge1.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badge1.getVetId(), responseDTO.getVetId());
                    assertEquals(Base64.getEncoder().encodeToString(badge1.getData()), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }

    @Test
    void getBadgeByValidVetId_with0Rating_shouldSucceed(){
        when(badgeRepository.findByVetId(anyString())).thenReturn(Mono.just(badge3));
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(0L));

        Mono<BadgeResponseDTO> badgeResponseDTO=badgeService.getBadgeByVetId(VET_ID);

        StepVerifier
                .create(badgeResponseDTO)
                .consumeNextWith(responseDTO->{
                    assertNotNull(responseDTO);
                    assertEquals(badge3.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badge3.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badge3.getVetId(), responseDTO.getVetId());
                    assertEquals(Base64.getEncoder().encodeToString(badge3.getData()), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }

    @Test
    void getBadge_withRatingInvalidVetId_shouldNotSucceed(){
        String invalidVetId="123";

        when(badgeRepository.findByVetId(anyString())).thenReturn(Mono.just(badge1));
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(3L));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.error(new NotFoundException("vetId is Not Found" + invalidVetId)));

        Mono<BadgeResponseDTO> badgeResponseDTO=badgeService.getBadgeByVetId(VET_ID);

        StepVerifier
                .create(badgeResponseDTO)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException && throwable.getMessage().contains("vetId is Not Found" + invalidVetId))
                .verify();
    }

    @Test
    void getBadge_withAvgRatingOver2_shouldSucceed(){
        String validVetId="123456";

        Rating rating1 = buildRating("12346", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 4.0);
        Rating rating2 = buildRating("12347", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 3.0);

        when(badgeRepository.findByVetId(anyString())).thenReturn(Mono.just(badge2));
        when(ratingRepository.countAllByVetId(anyString())).thenReturn(Mono.just(2L));
        when(ratingRepository.findAllByVetId(anyString())).thenReturn(Flux.just(rating1, rating2));

        Mono<BadgeResponseDTO> badgeResponseDTO=badgeService.getBadgeByVetId(VET_ID);

        StepVerifier
                .create(badgeResponseDTO)
                .consumeNextWith(responseDTO->{
                    assertNotNull(responseDTO);
                    assertEquals(badge2.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badge2.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badge2.getVetId(), responseDTO.getVetId());
                    assertEquals(Base64.getEncoder().encodeToString(badge2.getData()), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }

    private Badge buildBadge(BadgeTitle b, ClassPathResource c) throws IOException {
        return Badge.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .badgeTitle(b)
                .badgeDate("2017")
                .data(StreamUtils.copyToByteArray(c.getInputStream()))
                .build();
    }

    private Rating buildRating(String ratingId, String vetId, Double rateScore) {
        return Rating.builder()
                .ratingId(ratingId)
                .vetId(vetId)
                .rateScore(rateScore)
                .build();
    }
}