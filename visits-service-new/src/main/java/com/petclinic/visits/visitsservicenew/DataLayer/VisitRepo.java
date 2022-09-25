package com.petclinic.visits.visitsservicenew.DataLayer;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Repository
public interface VisitRepo extends ReactiveMongoRepository<Visit, Integer> {

    Mono<Optional<Visit>> findById(int visitId);

    Flux<Visit> findByPetId(int petId);

    Flux<Visit> findByPetIdIn(Collection<Integer> petIds);

    Flux<Visit> findVisitsByPractitionerId(int practitionerId);
    Flux<Visit> findVisitsByPractitionerIdAndMonth(int practitionerId, int month);

    Flux<Visit> findAllByMonthBetween(int startingDay, int endingDay);

    Mono<Visit> findByVisitId(String visitId);
}
