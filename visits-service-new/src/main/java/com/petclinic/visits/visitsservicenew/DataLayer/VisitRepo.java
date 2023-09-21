package com.petclinic.visits.visitsservicenew.DataLayer;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VisitRepo extends ReactiveMongoRepository<Visit, String> {

    Flux<Visit> findByPetId(int petId);

    Flux<Visit> findAllByStatus(String status);

    Flux<Visit> findVisitsByPractitionerId(int practitionerId);
    //Flux<Visit> findVisitsByPractitionerIdAndMonth(int practitionerId, int month); replace w query params search

    Mono<Visit> findByVisitId(String visitId);

    Mono<Void> deleteVisitByVisitId(String visitId);
}
