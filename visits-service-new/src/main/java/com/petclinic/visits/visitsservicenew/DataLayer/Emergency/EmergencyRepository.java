package com.petclinic.visits.visitsservicenew.DataLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EmergencyRepository extends ReactiveMongoRepository<Emergency, String> {
    Mono<Emergency> findEmergenciesByVisitEmergencyId(String EmergencyId);

    Flux<Emergency> findByPetId(String petId);
}
