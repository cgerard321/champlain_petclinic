package com.petclinic.visits.visitsservicenew.DataLayer;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
public class VisitRepoTest {

    @Autowired
    private VisitRepo visitRepo;
    Visit visit = buildVisit();



    @Test
    void findByVisitId(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Mono<Visit> getVisit = visitRepo.findByVisitId(visit.getVisitId());

        Publisher<Visit> comp = Mono.from(visitPublisher).then(getVisit);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit.getPetId(), gotVisit.getPetId());
                    assertEquals(visit.getDay(), gotVisit.getDay());
                    assertEquals(visit.getMonth(), gotVisit.getMonth());
                    assertEquals(visit.getYear(), gotVisit.getYear());
                    assertEquals(visit.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit.isStatus(), gotVisit.isStatus());
                }).verifyComplete();
    }
    @Test
    void findByPetId(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Flux<Visit> getPet = visitRepo.findByPetId(visit.getPetId());

        Publisher<Visit> comp = Mono.from(visitPublisher).thenMany(getPet);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit.getPetId(), gotVisit.getPetId());
                    assertEquals(visit.getDay(), gotVisit.getDay());
                    assertEquals(visit.getMonth(), gotVisit.getMonth());
                    assertEquals(visit.getYear(), gotVisit.getYear());
                    assertEquals(visit.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit.isStatus(), gotVisit.isStatus());
                }).verifyComplete();
    }

    @Test
    void findVisitsByPractitionerId(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Flux<Visit> getPract = visitRepo.findVisitsByPractitionerId(visit.getPractitionerId());

        Publisher<Visit> comp = Mono.from(visitPublisher).thenMany(getPract);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit.getPetId(), gotVisit.getPetId());
                    assertEquals(visit.getDay(), gotVisit.getDay());
                    assertEquals(visit.getMonth(), gotVisit.getMonth());
                    assertEquals(visit.getYear(), gotVisit.getYear());
                    assertEquals(visit.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit.isStatus(), gotVisit.isStatus());
                }).verifyComplete();

    }
    @Test
    void findVisitsByPractitionerIdAndMonth(){

        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();

        Flux<Visit> getPractAndMonth = visitRepo.findVisitsByPractitionerIdAndMonth(visit.getPractitionerId(), visit.getMonth());

        Publisher<Visit> comp = Mono.from(visitPublisher).thenMany(getPractAndMonth);

        StepVerifier.create(comp)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit.getPetId(), gotVisit.getPetId());
                    assertEquals(visit.getDay(), gotVisit.getDay());
                    assertEquals(visit.getMonth(), gotVisit.getMonth());
                    assertEquals(visit.getYear(), gotVisit.getYear());
                    assertEquals(visit.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit.isStatus(), gotVisit.isStatus());
                }).verifyComplete();

    }
    @Test
    void deleteVisitByVisitId(){
        Publisher<Visit> visitPublisher = visitRepo.deleteAll().thenMany(visitRepo.save(visit));

        StepVerifier
                .create(visitPublisher)
                .consumeNextWith(gotVisit -> {

                    assertEquals(visit.getVisitId(), gotVisit.getVisitId());
                    assertEquals(visit.getPetId(), gotVisit.getPetId());
                    assertEquals(visit.getDay(), gotVisit.getDay());
                    assertEquals(visit.getMonth(), gotVisit.getMonth());
                    assertEquals(visit.getYear(), gotVisit.getYear());
                    assertEquals(visit.getPractitionerId(), gotVisit.getPractitionerId());
                    assertEquals(visit.isStatus(), gotVisit.isStatus());
                }).then(this::deleteVisitByVisitId).verifyComplete();
    }

    private Visit buildVisit(){

        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }

}
