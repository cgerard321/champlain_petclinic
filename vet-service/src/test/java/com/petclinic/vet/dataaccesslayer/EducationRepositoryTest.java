package com.petclinic.vet.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class EducationRepositoryTest {

    @Autowired
    EducationRepository educationRepository;

    Education e1 = Education.builder()
            .educationId("1")
            .vetId("1")
            .degree("Doctor of Veterinary Medicine")
            .fieldOfStudy("Veterinary Medicine")
            .schoolName("University of Montreal")
            .startDate("2010")
            .endDate("2014")
            .build();
    Education e2 = Education.builder()
            .educationId("2")
            .vetId("2")
            .degree("Doctor of Veterinary Medicine")
            .fieldOfStudy("Veterinary Medicine")
            .schoolName("University of Veterinary Sciences")
            .startDate("2008")
            .endDate("2013")
            .build();

    @BeforeEach
    void setUp() {
        Publisher<Education> setUp = educationRepository.deleteAll()
                .thenMany(educationRepository.save(e1));

        StepVerifier
                .create(setUp)
                .expectNextCount(1)
                .verifyComplete();

        Publisher<Education> setUp2 = educationRepository.save(e2);

        StepVerifier
                .create(setUp2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void getAllEducationsOfAVet_ShouldSucceed(){
        Publisher<Education> find = educationRepository.findAllByVetId("1");

        StepVerifier
                .create(find)
                .consumeNextWith(found -> {
                    assertEquals(e1.getEducationId(), found.getEducationId());
                })
                .verifyComplete();
    }
}