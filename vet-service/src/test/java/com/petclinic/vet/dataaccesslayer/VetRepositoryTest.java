package com.petclinic.vet.dataaccesslayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataR2dbcTest
class VetRepositoryTest {

    @Autowired
    VetRepository vetRepository;

    @Test
    public void getVetByVetId (){
        Vet vet = buildVet();


        Publisher<Vet> find = vetRepository.findVetByVetId(234568);

        StepVerifier
                .create(find)
                .consumeNextWith(foundVet -> {
                    assertNotNull(foundVet.getVetId());
                        })
                .verifyComplete();

//        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(buildVet()));
//        StepVerifier
//                .create(setup)
//                .consumeNextWith(foundVet -> {
//                    assertEquals(vet.getVetId(), foundVet.getVetId());
//                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
//                    assertEquals(vet.getLastName(), foundVet.getLastName());
//                    assertEquals(vet.getEmail(), foundVet.getEmail());
//                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
//                    assertEquals(vet.getResume(), foundVet.getResume());
//                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
//                })
//
//                .verifyComplete();
    }
    @Test
    public void deleteVetByVetId () {
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(buildVet()));
        StepVerifier
                .create(setup)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .then(this::deleteVetByVetId)
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