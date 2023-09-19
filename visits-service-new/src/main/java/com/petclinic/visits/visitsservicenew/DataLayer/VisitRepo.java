package com.petclinic.visits.visitsservicenew.DataLayer;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface VisitRepo extends ReactiveMongoRepository<Visit, String> {

    Flux<Visit> findByPetId(int petId);

    Flux<Visit> findVisitsByPractitionerId(int practitionerId);

    // Flux<Visit> findVisitsByPractitionerIdAndVisitDate(int practitionerId, LocalDateTime visitDate); replace with queryParams

    Mono<Visit> findByVisitId(String visitId);

    Mono<Void> deleteVisitByVisitId(String visitId);
}
