package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class VetServiceImplTest {

    @Autowired
    VetService vetService;

    @MockBean
    VetRepository vetRepository;


    @Test
    void getVetByVetId() {
        Vet vet = buildVet();
        Integer VET_ID = vet.getVetId();

        when(vetRepository.findVetByVetId(anyInt()).thenReturn(Mono.just(vet)));

        Mono<VetDTO> vetDTOMono = vetService.getVetByVetId(VET_ID);


        StepVerifier
                .create(vetDTOMono)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    //assertEquals(vet.isActive(), foundVet.getIsActive());
                })

                .verifyComplete();
    }


    @Test
    void deleteVet() {
        Vet vet = buildVet();
        Integer VET_ID = vet.getVetId();

        when(vetRepository.findVetByVetId(anyInt()).thenReturn(Mono.just(vet)));
        Mono<VetDTO> vetDTOMono = vetService.getVetByVetId(VET_ID);

        StepVerifier
                .create(vetDTOMono)
                .expectNextCount(1)
                .verifyComplete();
    }


    private Vet buildVet() {
        return Vet.builder()
                .vetId(678910)
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .isActive(false)
                .build();
    }
}