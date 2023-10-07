package com.petclinic.vet.dataaccesslayer.badges;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BadgeRepository extends ReactiveCrudRepository<Badge, String> {
    Mono<Badge> findByVetId(String vetId);
}
