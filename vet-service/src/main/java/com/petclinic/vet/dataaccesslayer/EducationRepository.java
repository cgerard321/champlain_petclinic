package com.petclinic.vet.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EducationRepository extends ReactiveMongoRepository<Education, String> {

    Flux<Education> findAllByVetId(String vetId);
}
