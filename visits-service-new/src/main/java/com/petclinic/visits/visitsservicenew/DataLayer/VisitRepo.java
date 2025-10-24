package com.petclinic.visits.visitsservicenew.DataLayer;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Interface not implemented. Basically use Mongo for a smart search throughout a database. Calling a function here will have result on the database.
 */
@Repository
public interface VisitRepo extends ReactiveMongoRepository<Visit, String> {

    Flux<Visit> findByPetId(String petId);

    Flux<Visit> findVisitsByPractitionerId(String practitionerId);
    //Flux<Visit> findVisitsByPractitionerIdAndMonth(int practitionerId, int month); replace w query params search

    Mono<Visit> findByVisitId(String visitId);

    Mono<Void> deleteByVisitId(String visitId);

    Mono<Boolean> existsByVisitId(String visitId);

    Flux<Visit> findAllByStatus(String status);

    // In your VisitRepo interface
    Flux<Visit> findByVisitDateAndPractitionerId(LocalDateTime visitDate, String practitionerId);

    Flux<Visit> findVisitsByDescriptionContainingIgnoreCase(String Description);

    Flux<Visit> findByPractitionerIdAndVisitDateBetween(String practitionerId, LocalDateTime startDate, LocalDateTime endDate);

}
