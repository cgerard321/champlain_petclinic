package com.petclinic.vet.dataaccesslayer.badges;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BadgeRepository extends ReactiveCrudRepository<Badge, Integer> {
    Mono<Badge> findByVetId(String vetId);
}
