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
                    assertEquals(e1.getVetId(), found.getVetId());
                    assertEquals(e1.getDegree(), found.getDegree());
                    assertEquals(e1.getSchoolName(), found.getSchoolName());
                    assertEquals(e1.getFieldOfStudy(), found.getFieldOfStudy());
                    assertEquals(e1.getStartDate(), found.getStartDate());
                    assertEquals(e1.getEndDate(), found.getEndDate());
                })
                .verifyComplete();
    }
    @Test
    public void deleteEducationOfVet_ShouldSucceed () {
        StepVerifier
                .create(educationRepository.delete(e1))
                .expectNextCount(0)
                .verifyComplete();
    }
}