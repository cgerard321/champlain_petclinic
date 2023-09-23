package com.petclinic.visits.visitsservicenew.DataLayer;

import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class VisitRepoTest {
    @Autowired
    private VisitRepo visitRepo;
    private final Visit visit1 = buildVisit("73b5c112-5703-4fb7-b7bc-ac8186811ae1", 2);
    private final Visit visit2 = buildVisit("visitId2", 2);
    private final Visit visit3 = buildVisit("visitId3", 3);

    @BeforeEach
    void setupDb(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll()
                .thenMany(visitRepo.save(visit1))
                .thenMany(visitRepo.save(visit2))
                .thenMany(visitRepo.save(visit3));
        StepVerifier.create(visitPublisher)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByVisitId(){
        StepVerifier.create(visitRepo.findByVisitId(visit1.getVisitId()))
                .consumeNextWith(gotVisit -> {
                    assertEquals(visit1.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit1.getPetId(), gotVisit.getPetId());
                    assertEquals(visit1.getVisitDate(), gotVisit.getVisitDate());
                    assertEquals(visit1.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit1.isStatus(), gotVisit.isStatus());
                }).verifyComplete();
    }
    @Test
    void findByPetId(){
        StepVerifier.create(visitRepo.findByPetId(visit1.getPetId()))
                .expectNextCount(2)
                .verifyComplete();
    }
    @Test
    void findVisitsByPractitionerId(){
        StepVerifier.create(visitRepo.findVisitsByPractitionerId(visit1.getPractitionerId()))
                .expectNextCount(1)
                .verifyComplete();
    }
    /*
    @Test
    void findVisitsByPractitionerIdAndMonth(){
        StepVerifier.create(visitRepo.findVisitsByPractitionerIdAndMonth(visit1.getPractitionerId(), visit1.getMonth()))
                .expectNextCount(3)
                .verifyComplete();
    }
     */

    @Test
    void deleteVisitByVisitId(){
        StepVerifier
                .create(visitRepo.findByVisitId(visit1.getVisitId()))
                .consumeNextWith(gotVisit -> {
                    assertEquals(visit1.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit1.getPetId(), gotVisit.getPetId());
                    assertEquals(visit1.getVisitDate(), gotVisit.getVisitDate());
                    assertEquals(visit1.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit1.isStatus(), gotVisit.isStatus());
                }).then(this::deleteVisitByVisitId).verifyComplete();
    }

    private Visit buildVisit(String visitId, int petId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return Visit.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00"))
                .description("this is a dummy description")
                .petId(petId)
                .practitionerId(UUID.randomUUID().toString())
                .status(true).build();
    }
}