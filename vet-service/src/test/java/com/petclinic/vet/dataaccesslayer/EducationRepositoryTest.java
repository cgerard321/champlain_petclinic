package com.petclinic.vet.dataaccesslayer;

import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class EducationRepositoryTest {
    @Autowired
    EducationRepository educationRepository;

    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

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
    @Test
    public void addEducationToAVet_ShouldSucceed(){
        Education e3 = Education.builder()
                .educationId("3")
                .vetId("1")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();

        StepVerifier.create(educationRepository.save(e3))
                .consumeNextWith(createdEducation -> {
                    assertEquals(e3.getEducationId(), createdEducation.getEducationId());
                    assertEquals(e3.getVetId(), createdEducation.getVetId());
                    assertEquals(e3.getDegree(), createdEducation.getDegree());
                    assertEquals(e3.getSchoolName(), createdEducation.getSchoolName());
                    assertEquals(e3.getFieldOfStudy(), createdEducation.getFieldOfStudy());
                    assertEquals(e3.getStartDate(), createdEducation.getStartDate());
                    assertEquals(e3.getEndDate(), createdEducation.getEndDate());
                });
    }
    @Test
    public void updateEducationOfVet_ShouldSucceed(){
        String existingEducationId="2";

        Publisher<Education> existingEducation = educationRepository.findByEducationId(existingEducationId);

        StepVerifier
                .create(existingEducation)
                .expectNextCount(1)
                .verifyComplete();

        Education education = Education.builder()
                .educationId(existingEducationId)
                .vetId("2")
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        StepVerifier.create(educationRepository.save(education))
                .consumeNextWith(updatedEducation -> {
                    assertEquals(education.getEducationId(), updatedEducation.getEducationId());
                    assertEquals(education.getVetId(), updatedEducation.getVetId());
                    assertEquals(education.getSchoolName(), updatedEducation.getSchoolName());
                    assertEquals(education.getDegree(), updatedEducation.getDegree());
                    assertEquals(education.getFieldOfStudy(), updatedEducation.getFieldOfStudy());
                    assertEquals(education.getStartDate(), updatedEducation.getStartDate());
                    assertEquals(education.getEndDate(), updatedEducation.getEndDate());
                })
                .verifyComplete();
    }
}