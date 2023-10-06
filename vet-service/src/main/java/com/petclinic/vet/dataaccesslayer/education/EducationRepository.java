package com.petclinic.vet.dataaccesslayer.education;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EducationRepository extends ReactiveMongoRepository<Education, String> {

    Flux<Education> findAllByVetId(String vetId);
    Mono<Education> findByVetIdAndEducationId(String vetId, String educationId);
}
