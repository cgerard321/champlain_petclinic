package com.petclinic.visits.visitsservicenew.DataLayer;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Repository
public interface VisitRepo extends ReactiveCrudRepository<Visit, Integer> {

    Mono<Optional<Visit>> findById(int visitId);

    Flux<Visit> findByPetId(int petId);

    Flux<Visit> findByPetIdIn(Collection<Integer> petIds);

    Flux<Visit> findVisitsByPractitionerId(int practitionerId);

    Flux<Visit> findAllByDateBetween(Date startingDate, Date EndDate);

    Mono<Visit> findByVisitId(UUID visitId);
}
