package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.servicelayer.badges.BadgeResponseDTO;
import com.petclinic.vet.servicelayer.badges.BadgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@SpringBootTest
@AutoConfigureWebTestClient
class BadgeServiceImplTest {
    @Autowired
    BadgeService badgeService;
    @MockBean
    BadgeRepository badgeRepository;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String VET_ID="123456";
    ClassPathResource cpr=new ClassPathResource("images/full_food_bowl.png");
    Badge badge=buildBadge();

    BadgeServiceImplTest() throws IOException {}

    @Test
    void getBadgeByValidVetId(){
        when(badgeRepository.findByVetId(anyString())).thenReturn(Mono.just(badge));

        Mono<BadgeResponseDTO> badgeResponseDTO=badgeService.getBadgeByVetId(VET_ID);

        StepVerifier
                .create(badgeResponseDTO)
                .consumeNextWith(responseDTO->{
                    assertNotNull(responseDTO);
                    assertEquals(badge.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badge.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badge.getVetId(), responseDTO.getVetId());
                    assertEquals(Base64.getEncoder().encodeToString(badge.getData()), responseDTO.getResourceBase64());
                })
                .verifyComplete();
    }

    private Badge buildBadge() throws IOException {
        return Badge.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2017")
                .data(StreamUtils.copyToByteArray(cpr.getInputStream()))
                .build();
    }
}