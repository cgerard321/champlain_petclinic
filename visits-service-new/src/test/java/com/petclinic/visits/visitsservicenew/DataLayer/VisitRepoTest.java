package com.petclinic.visits.visitsservicenew.DataLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.when;

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
                .expectNextCount(2)
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

    @Test
    void findVisitsByDescriptionContainingIgnoreCase() {
       StepVerifier.create(visitRepo.findVisitsByDescriptionContainingIgnoreCase(visit1.getDescription().toString()))
               .expectNextCount(2)
               .verifyComplete();
    }

    //@Test
    //void findVisitsByStatus(){
    //    StepVerifier.create(visitRepo.findAllByStatus(visit1.getStatus().toString()))
    //            .expectNextCount(2)
    //            .verifyComplete();
    //}


    private Visit buildVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING).build();
    }

    @Test
    void getVisitsByReminderFalse_ShouldReturnVisitsWithoutReminder() {
        // Prepare test data with reminder set to false
        Visit visit1 = Visit.builder()
                .visitId("visit1")
                .visitDate(LocalDateTime.now())
                .description("Visit with reminder false")
                .petId("1")
                .practitionerId("vet1")
                .status(Status.UPCOMING)
                .reminder(false)  // Reminder is false
                .visitEndDate(LocalDateTime.now().plusHours(1))
                .reminder(false)
                .ownerEmail("ownerEmail@gmail.com")
                .build();

        Visit visit2 = Visit.builder()
                .visitId("visit2")
                .visitDate(LocalDateTime.now().plusDays(1))
                .description("Another visit with reminder false")
                .petId("2")
                .practitionerId("vet2")
                .status(Status.COMPLETED)
                .visitEndDate(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminder(false)  // Reminder is false
                .ownerEmail("ownerEmail@gmail.com")
                .build();

        // Save the visits to the repository
        visitRepo.saveAll(Flux.just(visit1, visit2)).blockLast();

        // Execute the method to get visits with reminder = false
        Flux<Visit> visitsByReminderFalse = visitRepo.getVisitsByReminderFalse();

        // Verify that both visits are returned
        StepVerifier.create(visitsByReminderFalse)
                .expectNextMatches(visit -> !visit.isReminder())  // First visit
                .expectNextMatches(visit -> !visit.isReminder())  // Second visit
                .thenCancel()  // Cancel the verification upon receiving the first visit
                .verify();
    }

    @Test
    void getVisitsByReminderTrue_ShouldReturnVisitsWithReminder() {
        // Prepare test data with reminder set to true
        Visit visit1 = Visit.builder()
                .visitId("visit1")
                .visitDate(LocalDateTime.now())
                .description("Visit with reminder true")
                .petId("1")
                .practitionerId("vet1")
                .status(Status.UPCOMING)
                .reminder(true)  // Reminder is true
                .visitEndDate(LocalDateTime.now().plusHours(1))
                .ownerEmail("ownerEmail@gmail.com")
                .build();

        Visit visit2 = Visit.builder()
                .visitId("visit2")
                .visitDate(LocalDateTime.now().plusDays(1))
                .description("Another visit with reminder true")
                .petId("2")
                .practitionerId("vet2")
                .status(Status.COMPLETED)
                .visitEndDate(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminder(true)  // Reminder is true
                .ownerEmail("ownerEmail@gmail.com")
                .build();

        // Save the visits to the repository
        visitRepo.saveAll(Flux.just(visit1, visit2)).blockLast();

        // Execute the method to get visits with reminder = true
        Flux<Visit> visitsByReminderTrue = visitRepo.getVisitsByReminderTrue();

        // Verify that both visits are returned
        StepVerifier.create(visitsByReminderTrue)
                .expectNextMatches(visit -> visit.isReminder())  // First visit
                .expectNextMatches(visit -> visit.isReminder())  // Second visit
                .thenCancel()  // Cancel the verification upon receiving the first visit
                .verify();
    }

}