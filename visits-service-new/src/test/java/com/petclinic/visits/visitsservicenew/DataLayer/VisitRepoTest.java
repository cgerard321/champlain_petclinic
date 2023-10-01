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
    String uuidVisit1 = UUID.randomUUID().toString();
    String uuidVisit2 = UUID.randomUUID().toString();
    Visit visit1 = buildVisit(uuidVisit1, "testing 1", "2200332");
    Visit visit2 = buildVisit(uuidVisit2, "testing 1", "2200333");



    @BeforeEach
    void setupDb(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll()
                .thenMany(visitRepo.save(visit1))
                .thenMany(visitRepo.save(visit2));
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
                    assertEquals(visit1.getStatus(), gotVisit.getStatus());
                }).verifyComplete();
    }

    @Test
    void findVisitsByStatus(){
        StepVerifier.create(visitRepo.findAllByStatus(visit1.getStatus().toString()))
                .expectNextCount(3)
                .verifyComplete();
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
                    assertEquals(visit1.getStatus(), gotVisit.getStatus());
                }).then(this::deleteVisitByVisitId).verifyComplete();
    }




    private Visit buildVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING).build();
    }
}