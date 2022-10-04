package com.petclinic.vet.dataaccesslayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest
class VetRepositoryTest {

    @Autowired
    VetRepository vetRepository;

    @Test
    public void getAllVets() {
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Vet> find = vetRepository.findAll();
        Publisher<Vet> composite = Mono
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(foundVet -> {
                    assertTrue(foundVet.isActive());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .verifyComplete();
    }

    @Test
    public void createVet (){
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));
        StepVerifier
                .create(setup)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .verifyComplete();
    }

    @Test
    void getVetByVetId() {
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Mono<Vet> find = vetRepository.findVetByVetId(vet.getVetId());
        Publisher<Vet> composite = Mono
                .from(setup)
                .then(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .verifyComplete();
    }

    @Test
    public void deleteVetByVetId () {
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(buildVet()));
        StepVerifier
                .create(setup)
                .consumeNextWith(foundVet -> {
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

    @Test
    public void getVetByActive (){
        Vet vet = buildVet();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Vet> find = vetRepository.findVetsByIsActive(vet.isActive());
        Publisher<Vet> composite = Mono
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(foundVet -> {
                    assertTrue(foundVet.isActive());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .verifyComplete();
    }
    @Test
    public void getVetByInactive (){
        Vet vet = buildVet2();

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Flux<Vet> find = vetRepository.findVetsByIsActive(vet.isActive());
        Publisher<Vet> composite = Mono
                .from(setup)
                .thenMany(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(foundVet -> {
                    assertFalse(foundVet.isActive());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                })
                .verifyComplete();
    }




    private Vet buildVet() {
        return Vet.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .image("kjd".getBytes())
                .workday("Monday")
                .isActive(true)
                .build();
    }
    private Vet buildVet2() {
        return Vet.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .image("kjd".getBytes())
                .resume("Just became a vet")
                .workday("Monday")
                .isActive(false)
                .build();
    }

}