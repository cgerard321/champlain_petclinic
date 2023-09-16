package com.petclinic.visits.visitsservicenew.DataLayer;

import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
                    assertEquals(visit1.getDay(), gotVisit.getDay());
                    assertEquals(visit1.getMonth(), gotVisit.getMonth());
                    assertEquals(visit1.getYear(), gotVisit.getYear());
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

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit1));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Flux<Visit> getPract = visitRepo.findVisitsByPractitionerId(visit1.getPractitionerId());

        Publisher<Visit> comp = Mono.from(visitPublisher).thenMany(getPract);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit1.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit1.getPetId(), gotVisit.getPetId());
                    assertEquals(visit1.getDay(), gotVisit.getDay());
                    assertEquals(visit1.getMonth(), gotVisit.getMonth());
                    assertEquals(visit1.getYear(), gotVisit.getYear());
                    assertEquals(visit1.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit1.isStatus(), gotVisit.isStatus());
                }).verifyComplete();

    }
    @Test
    void findVisitsByPractitionerIdAndMonth(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit1));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Flux<Visit> getPractAndMonth = visitRepo.findVisitsByPractitionerIdAndMonth(visit1.getPractitionerId(), visit1.getMonth());

        Publisher<Visit> comp = Mono.from(visitPublisher).thenMany(getPractAndMonth);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit1.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit1.getPetId(), gotVisit.getPetId());
                    assertEquals(visit1.getDay(), gotVisit.getDay());
                    assertEquals(visit1.getMonth(), gotVisit.getMonth());
                    assertEquals(visit1.getYear(), gotVisit.getYear());
                    assertEquals(visit1.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit1.isStatus(), gotVisit.isStatus());
                }).verifyComplete();

    }
    @Test
    void deleteVisitByVisitId(){
        StepVerifier
                .create(visitRepo.findByVisitId(visit1.getVisitId()))
                .consumeNextWith(gotVisit -> {
                    //todo fix asserts and assert getAll size changes
                    assertEquals(visit1.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit1.getPetId(), gotVisit.getPetId());
                    assertEquals(visit1.getDay(), gotVisit.getDay());
                    assertEquals(visit1.getMonth(), gotVisit.getMonth());
                    assertEquals(visit1.getYear(), gotVisit.getYear());
                    assertEquals(visit1.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit1.isStatus(), gotVisit.isStatus());
                }).then(this::deleteVisitByVisitId).verifyComplete();
    }

    private Visit buildVisit(String visitId, int petId){
        return Visit.builder()
                .visitId(visitId)
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(petId)
                .practitionerId(2)
                .status(true).build();
    }


}
